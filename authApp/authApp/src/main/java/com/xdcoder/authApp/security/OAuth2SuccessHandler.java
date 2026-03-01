package com.xdcoder.authApp.security;

import com.xdcoder.authApp.entities.Provider;
import com.xdcoder.authApp.entities.RefreshToken;
import com.xdcoder.authApp.entities.User;
import com.xdcoder.authApp.repositories.RefreshTokenRepository;
import com.xdcoder.authApp.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

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


@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final CookieService cookieService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.auth.frontend.success-redirect}")
    private String frontEndSuccessUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.info("Successfull authentication");
        logger.info(authentication.toString());

         OAuth2User oAuth2User  = (OAuth2User)authentication.getPrincipal();

         //identify user
        String registrationId="unknown";
        if(authentication instanceof OAuth2AuthenticationToken token)
        {
            registrationId = token.getAuthorizedClientRegistrationId();
        }

        logger.info("registrationId:"+registrationId);
        logger.info("user:"+oAuth2User.getAttributes().toString());


        User user;
        switch(registrationId){
            case "google" ->{
                String googleId = oAuth2User.getAttributes().getOrDefault("sub", "").toString();
                String email = oAuth2User.getAttributes().getOrDefault("email", "").toString();
                String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();
                String picture = oAuth2User.getAttributes().getOrDefault("picture", "").toString();
                User newUser = User.builder()
                        .email(email)
                        .name(name)
                        .image(picture)
                        .enable(true)
                        .provider(Provider.GOOGLE)
                        .providerId(googleId)
                        .build();

                user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(newUser));

            }

            case "github" ->{
                String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();

                String githubId = oAuth2User.getAttributes().getOrDefault("githubId", "").toString();
                String image = oAuth2User.getAttributes().getOrDefault("avatar_url", "").toString();

                String email = (String) oAuth2User.getAttributes().get("email");

                if(email==null){
                    email = name.replaceAll(" ", "")+"@github.com";
                }

                User newUser = User.builder()
                        .email(email)
                        .name(name)
                        .image(image)
                        .enable(true)
                        .provider(Provider.GITHUB)
                        .providerId(githubId)
                        .build();
                user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(newUser));

            }

            default -> {
                throw new RuntimeException("Invalid registration id");
            }
        }

        //jwt token -- token ke saath frontend pe redirect karna hai
        //refresh:
        //user -> refresh token ko revoke krna hai


        //refresh token bana kr dunga

       String jti =  UUID.randomUUID().toString();
        RefreshToken refreshTokenOb = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();

        refreshTokenRepository.save(refreshTokenOb);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, jti);

        cookieService.attachRefreshTokenToResponse(response, refreshToken,(int) jwtService.getAccessTtlSeconds());

//        response.getWriter().write("login success");
        response.sendRedirect(frontEndSuccessUrl);
    }
}
