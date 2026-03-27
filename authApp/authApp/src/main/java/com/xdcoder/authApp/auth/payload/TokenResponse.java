package com.xdcoder.authApp.auth.payload;

/*
 * TokenResponse
 *
 * DTO returned to the client after successful authentication.
 * It contains the JWT access token, refresh token, token type,
 * expiration information, and the authenticated user's details.
 *
 * This response is typically sent after:
 * - Login
 * - Token refresh
 * - OAuth login
 *
 * The frontend uses the access token to call protected APIs.
 */
public record TokenResponse(

        // Short-lived JWT used to access protected APIs
        String accessToken,

        // Long-lived token used to generate new access tokens
        String refreshToken,

        // Expiration time (in seconds) of the access token
        Long expiresIn,

        // Token type used in Authorization header (usually "Bearer")
        String tokenType,

        // Authenticated user's details
        UserDto user
) {

        /*
         * Factory method used to create TokenResponse easily.
         * Automatically sets tokenType to "Bearer", which is the
         * standard prefix used in Authorization headers.
         *
         * Example header used by frontend:
         * Authorization: Bearer <accessToken>
         */
        public static TokenResponse of(String accessToken,
                                       String refreshToken,
                                       long expiresIn,
                                       UserDto user) {

                return new TokenResponse(
                        accessToken,
                        refreshToken,
                        expiresIn,
                        "Bearer",
                        user
                );
        }
}