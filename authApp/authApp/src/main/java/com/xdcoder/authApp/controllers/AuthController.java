package com.xdcoder.authApp.controllers;

import com.xdcoder.authApp.dtos.LoginRequest;
import com.xdcoder.authApp.dtos.RefreshTokenRequest;
import com.xdcoder.authApp.dtos.TokenResponse;
import com.xdcoder.authApp.dtos.UserDto;
import com.xdcoder.authApp.entities.RefreshToken;
import com.xdcoder.authApp.entities.User;
import com.xdcoder.authApp.repositories.RefreshTokenRepository;
import com.xdcoder.authApp.repositories.UserRepository;
import com.xdcoder.authApp.security.CookieService;
import com.xdcoder.authApp.security.JWTService;
import com.xdcoder.authApp.services.AuthService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final ModelMapper mapper;
    private final CookieService cookieService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        //authenticate
        Authentication authenticate = authenticate(loginRequest);
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(() -> new BadCredentialsException("Invalid username and password"));
        if(!user.isEnable()){
            throw new DisabledException("User is disabled");
        }

        String jti = UUID.randomUUID().toString();
        var refreshTokenOb = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();
        //refresh token information -- generate and save to db
        refreshTokenRepository.save(refreshTokenOb);

        //access token -- generate
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, refreshTokenOb.getJti());

        // use cookie service to attach refresh token to response as cookie

        cookieService.attachRefreshTokenToResponse(response, refreshToken, (int)jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);

        TokenResponse tokenResponse = TokenResponse.of(accessToken,refreshToken
                ,jwtService.getAccessTtlSeconds(),mapper.map(user, UserDto.class));
        return ResponseEntity.ok(tokenResponse);
    }

    private Authentication authenticate(LoginRequest loginRequest) {
        try{

            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        }catch(Exception e){
            throw new BadCredentialsException("Invalid username and password");
        }

    }

    //access and refresh token renew krne ke lie api

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
           @RequestBody(required = false) RefreshTokenRequest body,
           HttpServletResponse response,
           HttpServletRequest request
    ) {
        String refreshToken = readRefreshTokenFromRequest(body, request).orElseThrow(() -> new BadCredentialsException("Refresh token is missing"));

        if(!jwtService.isRefreshToken(refreshToken)){
            throw new BadCredentialsException("Invalid Refresh Token Type");
        }

        String jti = jwtService.getJti(refreshToken);
        UUID userId = jwtService.getUserId(refreshToken);
        RefreshToken storedRefreshToken = refreshTokenRepository.findByJti(jti).orElseThrow(() -> new BadCredentialsException("Refresh token not recognized"));

        if(storedRefreshToken.isRevoked()){
            throw new BadCredentialsException("Refresh token is revoked");
        }

        if(storedRefreshToken.getExpiresAt().isBefore(Instant.now())){
            throw new BadCredentialsException("Refresh token is expired");
        }

        if(!storedRefreshToken.getUser().getId().equals(userId)){
            throw new BadCredentialsException("Refresh token does not match user");
        }

        // refresh token ko rotate:
        storedRefreshToken.setRevoked(true);
        String newJti = UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJti);
        refreshTokenRepository.save(storedRefreshToken);

        User user = storedRefreshToken.getUser();

        var newRefreshTokenOb = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();
        refreshTokenRepository.save(newRefreshTokenOb);
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user, newJti);

        cookieService.attachRefreshTokenToResponse(response, newRefreshToken, (int)jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);
        return ResponseEntity.ok(TokenResponse.of(newAccessToken, newRefreshToken, jwtService.getAccessTtlSeconds(), mapper.map(user, UserDto.class)));
    }

    //logout
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        readRefreshTokenFromRequest(null, request).ifPresent(token -> {
            try{
                if(jwtService.isRefreshToken(token)){
                    String jti = jwtService.getJti(token);
                    refreshTokenRepository.findByJti(jti).ifPresent(rt -> {
                        rt.setRevoked(true);
                        refreshTokenRepository.save(rt);
                    });
                }
            } catch(JwtException ignored){

            }
        });
        // Use CookieUtil (same behavior)
        cookieService.clearRefreshCookie(response);
        cookieService.addNoStoreHeaders(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

        //this method will read refresh token from request body or cookie and return it
    private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
        // prefer reading refresh token from cookie
        if(request.getCookies()!=null){
            Optional<String> fromCookie = Arrays.stream(request.getCookies())
                    .filter(c -> cookieService.getRefreshTokenCookieName()
                            .equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> !v.isBlank())
                    .findFirst();

            if(fromCookie.isPresent()) {
                return fromCookie;
            }

        }
        // body:
        if(body!=null && body.refreshToken()!=null && !body.refreshToken().isBlank()){
            return Optional.of(body.refreshToken());
        }
        // custom header:
        String refreshHeader = request.getHeader("X-Refresh-Token");
        if(refreshHeader!=null && !refreshHeader.isBlank()){
            return Optional.of(refreshHeader.trim());
        }

        // Authorization = Bearer <token>
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(authHeader!=null && authHeader.regionMatches(true,0,"Bearer ",0,"Bearer".length())){
            String candidate = authHeader.substring(7).trim();
            if(!candidate.isEmpty()){
                try{
                    if(jwtService.isRefreshToken(candidate)){
                        return Optional.of(candidate);
                    }
                }catch(Exception ignored){

                }
            }
        }

        return Optional.empty();

    }


    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto){
        //registration logic will be here

        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
    }
}
