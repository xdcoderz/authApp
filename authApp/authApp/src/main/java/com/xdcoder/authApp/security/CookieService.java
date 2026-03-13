package com.xdcoder.authApp.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

/*
 * CookieService
 *
 * Utility service responsible for creating, attaching, and clearing cookies
 * used in authentication. In this application it mainly handles the
 * refresh token cookie used in the JWT authentication flow.
 *
 * Using cookies for refresh tokens helps improve security because the token
 * can be marked HttpOnly and Secure, preventing access from client-side JS.
 */
@Service
@Getter
public class CookieService {

    // Name of the cookie used to store the refresh token
    private final String refreshTokenCookieName;

    // Indicates whether the cookie should only be sent over HTTPS
    private final boolean cookieSecure;

    // If true, JavaScript cannot access the cookie (prevents XSS attacks)
    private final boolean cookieHttpOnly;

    // Domain where the cookie is valid (e.g., example.com)
    private final String cookieDomain;

    // SameSite policy controls cross-site cookie behavior
    private final String cookieSameSite;

    // Logger for debugging cookie operations
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(CookieService.class);


    /*
     * Values are injected from application.properties / application.yml.
     * This allows cookie security settings to be configured without changing code.
     */
    public CookieService(
            @Value("${security.jwt.refresh-token-cookie-name}") String refreshTokenCookieName,
            @Value("${security.jwt.cookie-secure}") boolean cookieSecure,
            @Value("${security.jwt.cookie-http-only}") boolean cookieHttpOnly,
            @Value("${security.jwt.cookie-same-site}") String cookieSameSite,
            @Value("${security.jwt.cookie-domain}") String cookieDomain
    ) {
        this.refreshTokenCookieName = refreshTokenCookieName;
        this.cookieSecure = cookieSecure;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieDomain = cookieDomain;
        this.cookieSameSite = cookieSameSite;
    }


    /*
     * attachRefreshTokenToResponse
     *
     * Creates a cookie containing the refresh token and attaches it
     * to the HTTP response so the browser stores it automatically.
     *
     * maxAge defines how long the cookie should remain valid.
     */
    public void attachRefreshTokenToResponse(HttpServletResponse response, String value, int maxAge) {

        logger.info("Attaching cookie with name: {} and value: {}", refreshTokenCookieName, value);

        var responseCookieBuilder = ResponseCookie.from(refreshTokenCookieName, value)
                .httpOnly(cookieHttpOnly) // prevent JS access
                .secure(cookieSecure)     // send only over HTTPS
                .path("/")                // cookie available for all endpoints
                .maxAge(maxAge)           // expiration time
                .sameSite(cookieSameSite);

        // If domain is configured, attach it
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            responseCookieBuilder.domain(cookieDomain);
        }

        ResponseCookie responseCookie = responseCookieBuilder.build();

        // Add cookie to HTTP response header
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }


    /*
     * clearRefreshCookie
     *
     * Removes the refresh token cookie from the browser by
     * setting its maxAge to 0.
     *
     * Used during logout.
     */
    public void clearRefreshCookie(HttpServletResponse response) {

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(refreshTokenCookieName, "")
                .maxAge(0) // immediately expire cookie
                .httpOnly(cookieHttpOnly)
                .path("/")
                .sameSite(cookieSameSite)
                .secure(cookieSecure);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        ResponseCookie responseCookie = builder.build();

        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }


    /*
     * addNoStoreHeaders
     *
     * Adds HTTP headers to prevent caching of authentication responses.
     * This is important for security so tokens are not stored in browser caches.
     */
    public void addNoStoreHeaders(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
    }
}