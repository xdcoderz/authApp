package com.xdcoder.authApp.auth.controllers;

import com.xdcoder.authApp.auth.payload.LoginRequest;
import com.xdcoder.authApp.auth.payload.RefreshTokenRequest;
import com.xdcoder.authApp.auth.payload.TokenResponse;
import com.xdcoder.authApp.auth.payload.UserDto;
import com.xdcoder.authApp.auth.entities.RefreshToken;
import com.xdcoder.authApp.auth.entities.User;
import com.xdcoder.authApp.auth.repositories.RefreshTokenRepository;
import com.xdcoder.authApp.auth.repositories.UserRepository;
import com.xdcoder.authApp.auth.services.Impl.CookieService;
import com.xdcoder.authApp.auth.services.Impl.JWTService;
import com.xdcoder.authApp.auth.services.AuthService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/*
 * AuthController
 *
 * Handles authentication related APIs such as:
 * - Login
 * - Token refresh
 * - Logout
 * - User registration
 *
 * These endpoints are typically used by the frontend (React app)
 * to authenticate users and manage sessions using JWT tokens.
 */
@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    // Service responsible for registering new users
    private final AuthService authService;

    // Repository used to store refresh tokens for session management
    private final RefreshTokenRepository refreshTokenRepository;

    // Spring Security component used to authenticate username/password
    private final AuthenticationManager authenticationManager;

    // Repository for fetching user information
    private final UserRepository userRepository;

    // Service responsible for generating and validating JWT tokens
    private final JWTService jwtService;

    // Utility used to convert entities to DTOs
    private final ModelMapper mapper;

    // Utility for attaching tokens to HTTP cookies
    private final CookieService cookieService;


    /*
     * LOGIN API
     *
     * Authenticates the user using email + password.
     * If successful:
     * 1. Generates access token
     * 2. Generates refresh token
     * 3. Stores refresh token in DB
     * 4. Sends refresh token as secure cookie
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        // Authenticate credentials using Spring Security
        Authentication authenticate = authenticate(loginRequest);

        // Fetch user from database
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid username and password"));

        // Prevent login if account is disabled
        if (!user.isEnable()) {
            throw new DisabledException("User is disabled");
        }

        // Unique ID for refresh token session
        String jti = UUID.randomUUID().toString();

        // Create refresh token entity
        var refreshTokenOb = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();

        // Save refresh token to database
        refreshTokenRepository.save(refreshTokenOb);

        // Generate JWT tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, refreshTokenOb.getJti());

        // Attach refresh token to response cookie
        cookieService.attachRefreshTokenToResponse(response, refreshToken,
                (int) jwtService.getRefreshTtlSeconds());

        // Prevent browser caching of authentication responses
        cookieService.addNoStoreHeaders(response);

        // Build response object
        TokenResponse tokenResponse = TokenResponse.of(
                accessToken,
                refreshToken,
                jwtService.getAccessTtlSeconds(),
                mapper.map(user, UserDto.class)
        );

        return ResponseEntity.ok(tokenResponse);
    }


    /*
     * Helper method that performs authentication using
     * Spring Security AuthenticationManager.
     */
    private Authentication authenticate(LoginRequest loginRequest) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid username and password");
        }
    }


    /*
     * REFRESH TOKEN API
     *
     * Used to generate a new access token when the old one expires.
     * The refresh token must be valid and stored in the database.
     *
     * Security concept: Refresh Token Rotation
     * Old refresh token is revoked and replaced with a new one.
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws InterruptedException {

        Thread.sleep(5000); // artificial delay (possibly for testing)

        // Read refresh token from cookie/body/header
        String refreshToken = readRefreshTokenFromRequest(body, request)
                .orElseThrow(() -> new BadCredentialsException("Refresh token is missing"));

        // Validate token type
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid Refresh Token Type");
        }

        // Extract token information
        String jti = jwtService.getJti(refreshToken);
        UUID userId = jwtService.getUserId(refreshToken);

        // Fetch refresh token from database
        RefreshToken storedRefreshToken = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new BadCredentialsException("Refresh token not recognized"));

        if (storedRefreshToken.isRevoked()) {
            throw new BadCredentialsException("Refresh token is revoked");
        }

        if (storedRefreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token is expired");
        }

        if (!storedRefreshToken.getUser().getId().equals(userId)) {
            throw new BadCredentialsException("Refresh token does not match user");
        }

        // Refresh token rotation (invalidate old token)
        storedRefreshToken.setRevoked(true);

        String newJti = UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJti);

        refreshTokenRepository.save(storedRefreshToken);

        User user = storedRefreshToken.getUser();

        // Create new refresh token
        var newRefreshTokenOb = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();

        refreshTokenRepository.save(newRefreshTokenOb);

        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user, newJti);

        cookieService.attachRefreshTokenToResponse(response,
                newRefreshToken,
                (int) jwtService.getRefreshTtlSeconds());

        cookieService.addNoStoreHeaders(response);

        return ResponseEntity.ok(
                TokenResponse.of(
                        newAccessToken,
                        newRefreshToken,
                        jwtService.getAccessTtlSeconds(),
                        mapper.map(user, UserDto.class)
                )
        );
    }


    /*
     * LOGOUT API
     *
     * Revokes the refresh token and clears authentication cookies.
     * This ensures the user cannot obtain new access tokens.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {

        readRefreshTokenFromRequest(null, request).ifPresent(token -> {
            try {
                if (jwtService.isRefreshToken(token)) {
                    String jti = jwtService.getJti(token);

                    refreshTokenRepository.findByJti(jti).ifPresent(rt -> {
                        rt.setRevoked(true);
                        refreshTokenRepository.save(rt);
                    });
                }
            } catch (JwtException ignored) {
            }
        });

        // Clear refresh token cookie
        cookieService.clearRefreshCookie(response);

        cookieService.addNoStoreHeaders(response);

        // Clear Spring Security context
        SecurityContextHolder.clearContext();

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /*
     * Utility method to read refresh token from multiple locations:
     * - Cookie (preferred)
     * - Request body
     * - Custom header
     * - Authorization header
     */
    private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {

        // Check cookies
        if (request.getCookies() != null) {
            Optional<String> fromCookie = Arrays.stream(request.getCookies())
                    .filter(c -> cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> !v.isBlank())
                    .findFirst();

            if (fromCookie.isPresent()) {
                return fromCookie;
            }
        }

        // Check request body
        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
            return Optional.of(body.refreshToken());
        }

        // Check custom header
        String refreshHeader = request.getHeader("X-Refresh-Token");
        if (refreshHeader != null && !refreshHeader.isBlank()) {
            return Optional.of(refreshHeader.trim());
        }

        // Check Authorization header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null &&
                authHeader.regionMatches(true, 0, "Bearer ", 0, "Bearer".length())) {

            String candidate = authHeader.substring(7).trim();

            if (!candidate.isEmpty()) {
                try {
                    if (jwtService.isRefreshToken(candidate)) {
                        return Optional.of(candidate);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return Optional.empty();
    }


    /*
     * REGISTER API
     *
     * Creates a new user account using AuthService.
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registerUser(userDto));
    }
}