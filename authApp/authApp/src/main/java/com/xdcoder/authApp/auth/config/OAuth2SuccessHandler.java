package com.xdcoder.authApp.auth.config;

import com.xdcoder.authApp.auth.entities.Provider;
import com.xdcoder.authApp.auth.entities.RefreshToken;
import com.xdcoder.authApp.auth.entities.User;
import com.xdcoder.authApp.auth.repositories.RefreshTokenRepository;
import com.xdcoder.authApp.auth.repositories.UserRepository;
import com.xdcoder.authApp.auth.services.Impl.CookieService;
import com.xdcoder.authApp.auth.services.Impl.JWTService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

/*
 * OAuth2SuccessHandler
 *
 * This class is triggered after a user successfully logs in using OAuth2
 * providers like Google or GitHub. Spring Security calls this handler
 * automatically once authentication is completed.
 *
 * Responsibility:
 * 1. Identify which OAuth provider was used
 * 2. Extract user information from OAuth provider response
 * 3. Create or fetch user from the database
 * 4. Generate JWT access + refresh tokens
 * 5. Store refresh token in DB
 * 6. Redirect user to frontend application
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    // Logger used for debugging authentication events
    private final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    // Repository used to read/write users from database
    private final UserRepository userRepository;

    // Service responsible for generating JWT access and refresh tokens
    private final JWTService jwtService;

    // Utility for attaching tokens to HTTP cookies
    private final CookieService cookieService;

    // Repository used to persist refresh tokens for session management
    private final RefreshTokenRepository refreshTokenRepository;

    // URL where the frontend app should be redirected after login
    @Value("${app.auth.frontend.success-redirect}")
    private String frontEndSuccessUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        logger.info("Successful authentication");
        logger.info(authentication.toString());

        // OAuth2User contains all user attributes returned by Google/GitHub
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Determine which OAuth provider authenticated the user
        String registrationId = "unknown";

        if (authentication instanceof OAuth2AuthenticationToken token) {
            registrationId = token.getAuthorizedClientRegistrationId();
        }

        logger.info("registrationId: " + registrationId);
        logger.info("user: " + oAuth2User.getAttributes().toString());

        User user;

        /*
         * Handle different OAuth providers separately because
         * each provider returns attributes in slightly different formats.
         */
        switch (registrationId) {

            // Google OAuth login
            case "google" -> {

                // Extract standard fields returned by Google OAuth
                String googleId = oAuth2User.getAttributes().getOrDefault("sub", "").toString();
                String email = oAuth2User.getAttributes().getOrDefault("email", "").toString();
                String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();
                String picture = oAuth2User.getAttributes().getOrDefault("picture", "").toString();

                // Build a new user object using Lombok builder pattern
                User newUser = User.builder()
                        .email(email)
                        .name(name)
                        .image(picture)
                        .enable(true)
                        .provider(Provider.GOOGLE)
                        .providerId(googleId)
                        .build();

                // If user already exists, return it. Otherwise create new user.
                user = userRepository
                        .findByEmail(email)
                        .orElseGet(() -> userRepository.save(newUser));
            }

            // GitHub OAuth login
            case "github" -> {

                String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();
                String githubId = oAuth2User.getAttributes().getOrDefault("githubId", "").toString();
                String image = oAuth2User.getAttributes().getOrDefault("avatar_url", "").toString();

                // GitHub may not always provide email
                String email = (String) oAuth2User.getAttributes().get("email");

                // If email is missing, create a fallback email
                if (email == null) {
                    email = name.replaceAll(" ", "") + "@github.com";
                }

                User newUser = User.builder()
                        .email(email)
                        .name(name)
                        .image(image)
                        .enable(true)
                        .provider(Provider.GITHUB)
                        .providerId(githubId)
                        .build();

                user = userRepository
                        .findByEmail(email)
                        .orElseGet(() -> userRepository.save(newUser));
            }

            // If provider is not supported
            default -> {
                throw new RuntimeException("Invalid registration id");
            }
        }

        /*
         * After successful login we generate tokens.
         *
         * Access Token  -> short lived, used to access APIs
         * Refresh Token -> longer lived, used to generate new access tokens
         */

        // Unique identifier for refresh token session
        String jti = UUID.randomUUID().toString();

        // Create refresh token entity
        RefreshToken refreshTokenOb = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();

        // Store refresh token in database
        refreshTokenRepository.save(refreshTokenOb);

        // Generate JWT tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, jti);

        /*
         * Attach refresh token to HTTP cookie.
         * Cookies allow the browser to automatically send the token
         * with future requests without exposing it to frontend JS.
         */
        cookieService.attachRefreshTokenToResponse(
                response,
                refreshToken,
                (int) jwtService.getAccessTtlSeconds()
        );

        // Redirect the authenticated user back to the frontend application
        response.sendRedirect(frontEndSuccessUrl);
    }
}