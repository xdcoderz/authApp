package com.xdcoder.authApp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdcoder.authApp.dtos.ApiError;
import com.xdcoder.authApp.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/*
 * SecurityConfig
 *
 * Central configuration for Spring Security.
 *
 * Responsibilities:
 * - Configure authentication & authorization rules
 * - Enable JWT-based security
 * - Configure OAuth2 login
 * - Disable session-based authentication (stateless API)
 * - Setup CORS for frontend communication
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // Custom JWT filter used to validate JWT tokens on each request
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Handler that runs after successful OAuth login (Google/GitHub)
    private AuthenticationSuccessHandler successHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          AuthenticationSuccessHandler successHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.successHandler = successHandler;
    }

    /*
     * Main Spring Security configuration.
     *
     * SecurityFilterChain defines how HTTP requests are secured.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable) // Disable CSRF for REST APIs
                .cors(Customizer.withDefaults()) // Enable CORS support

                // Disable session creation since we use JWT tokens
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(authorize ->
                        authorize
                                // Public authentication endpoints
                                .requestMatchers(AppConstants.AUTH_PUBLIC_URLS).permitAll()
                                // All other endpoints require authentication
                                .requestMatchers(HttpMethod.GET).hasRole(AppConstants.ROLE_USER)
                                .requestMatchers("/api/v1/users/**").hasRole(AppConstants.ROLE_ADMIN)
                                .anyRequest().authenticated()
                )

                // OAuth login configuration (Google / GitHub)
                .oauth2Login(oauth2 ->
                        oauth2.successHandler(successHandler)
                                .failureHandler(null)
                )

                // Disable default logout (handled manually in controller)
                .logout(AbstractHttpConfigurer::disable)

                // Custom exception handling for unauthorized requests
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint((request, response, authException) -> {

                            authException.printStackTrace();

                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");

                            String message = "Unauthorized Access ! " + authException.getMessage();

                            Object errorAttr = request.getAttribute("error");
                            if (errorAttr != null) {
                                message = errorAttr.toString();
                            }

                            // Custom API error response
                            var apiError = ApiError.of(
                                    HttpStatus.UNAUTHORIZED.value(),
                                    "Unauthorized Access !!",
                                    message,
                                    request.getRequestURI(),
                                    true
                            );

                            var objectMapper = new ObjectMapper();
                            response.getWriter().write(objectMapper.writeValueAsString(apiError));
                        })
                                .accessDeniedHandler((request, response, e) -> {
                                    response.setStatus(403);
                                    response.setContentType("application/json");
                                    String message = e.getMessage();
                                    String error = (String) request.getAttribute("error");
                                    if(error !=null){
                                        message = error;
                                    }
                                    var apiError = ApiError.of(HttpStatus.FORBIDDEN.value(), "Forbidden Access", message, request.getRequestURI(), true);
                                    var objectMapper = new ObjectMapper();
                                    response.getWriter().write(objectMapper.writeValueAsString(apiError));

                                })
                )

                // Add custom JWT filter before default Spring login filter
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    /*
     * PasswordEncoder bean used to hash passwords before storing them.
     *
     * BCrypt is the most commonly used secure hashing algorithm
     * in Spring Security.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /*
     * AuthenticationManager is used to authenticate username/password
     * credentials (used in login API).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }


    /*
     * CORS configuration allows the frontend (React app)
     * to call APIs from a different domain.
     *
     * Example:
     * React frontend -> localhost:3000
     * Spring backend -> localhost:8080
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.frontend-url}") String corsUrls
    ) {

        // Multiple frontend URLs can be provided in properties
        String[] urls = corsUrls.trim().split(",");

        var config = new CorsConfiguration();

        // Allowed frontend origins
        config.setAllowedOrigins(Arrays.asList(urls));

        // Allowed HTTP methods
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));

        // Allow all headers
        config.setAllowedHeaders(List.of("*"));

        // Allow cookies/credentials (required for refresh token cookies)
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();

        // Apply this CORS configuration to all endpoints
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}