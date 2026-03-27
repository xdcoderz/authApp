package com.xdcoder.authApp.auth.config;

import com.xdcoder.authApp.auth.services.Impl.JWTService;
import com.xdcoder.authApp.auth.helpers.UserHelper;
import com.xdcoder.authApp.auth.repositories.UserRepository;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/*
 * JwtAuthenticationFilter
 *
 * This filter runs for every incoming request and checks whether the request
 * contains a valid JWT access token.
 *
 * If a valid token is found:
 * 1. The token is parsed and validated.
 * 2. The user is fetched from the database.
 * 3. A Spring Security Authentication object is created.
 * 4. The authentication is stored in the SecurityContext.
 *
 * Once the SecurityContext contains authentication, Spring Security treats
 * the request as authenticated and allows access to protected endpoints.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Service responsible for parsing and validating JWT tokens
    private final JWTService jwtService;

    // Repository used to fetch user information
    private final UserRepository userRepository;

    // Logger for debugging authentication flow
    private Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Read Authorization header from the request
        String header = request.getHeader("Authorization");
        logger.info("Authorization header: {}", header);

        // Check if the header contains a Bearer token
        if (header != null && header.startsWith("Bearer ")) {

            // Extract token after "Bearer "
            String token = header.substring(7);

            try {

                // Parse the token using JWTService
                Jws<Claims> parse = jwtService.parse(token);
                Claims payLoad = parse.getPayload();

                // Ensure the token is an access token (not refresh token)
                if (!jwtService.isAccessToken(token)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // Extract user ID stored inside JWT subject
                String userId = payLoad.getSubject();
                UUID userUUID = UserHelper.parseUUID(userId);

                // Fetch user from database
                userRepository.findById(userUUID)
                        .ifPresent(user -> {

                            // Only authenticate if the user account is enabled
                            if (user.isEnable()) {

                                /*
                                 * Convert user roles into Spring Security authorities.
                                 * Authorities represent permissions used during authorization.
                                 */
                                List<GrantedAuthority> authorities =
                                        user.getRoles() == null
                                                ? List.of()
                                                : user.getRoles()
                                                .stream()
                                                .map(role -> new SimpleGrantedAuthority(role.getName()))
                                                .collect(Collectors.toList());

                                /*
                                 * Create authentication object for Spring Security.
                                 * Principal = user email
                                 * Credentials = null (already authenticated via JWT)
                                 */
                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                user.getEmail(),
                                                null,
                                                authorities
                                        );

                                // Attach request details (IP, session info, etc.)
                                authentication.setDetails(
                                        new WebAuthenticationDetailsSource().buildDetails(request)
                                );

                                /*
                                 * Store authentication in SecurityContext.
                                 * After this step the request becomes authenticated
                                 * for the rest of the Spring Security pipeline.
                                 */
                                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                                    SecurityContextHolder.getContext().setAuthentication(authentication);
                                }
                            }
                        });

            } catch (ExpiredJwtException e) {

                // Token is valid but expired
                request.setAttribute("error", "Token Expired");

            } catch (Exception e) {

                // Token is malformed or invalid
                request.setAttribute("error", "Invalid Token");
            }
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }


    /*
     * Skip JWT filtering for public authentication endpoints.
     *
     * These endpoints must remain accessible without authentication:
     * - login
     * - register
     */
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().startsWith("/api/v1/auth/login")
                || request.getRequestURI().startsWith("/api/v1/auth/register");
    }
}