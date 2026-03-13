package com.xdcoder.authApp.security;

import com.xdcoder.authApp.entities.Role;
import com.xdcoder.authApp.entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/*
 * JWTService
 *
 * Central service responsible for creating and validating JWT tokens.
 * JWT (JSON Web Token) is used for stateless authentication in this system.
 *
 * Instead of storing sessions on the server, authentication information
 * is stored inside a signed token which the client sends with each request.
 */
@Service
@Getter
@Setter
public class JWTService {

    // Secret key used to sign and verify JWT tokens
    private final SecretKey key;

    // Access token validity time (in seconds)
    private final long accessTtlSeconds;

    // Refresh token validity time (in seconds)
    private final long refreshTtlSeconds;

    // Token issuer name (used for identifying the system that created the token)
    private final String issuer;

    /*
     * Constructor loads JWT configuration values from application.properties.
     * The secret key must be long enough to ensure strong cryptographic security.
     */
    public JWTService(
            @Value("${security.jwt.secret}") String secretKey,
            @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds,
            @Value("${security.jwt.issuer}") String issuer) {

        if (secretKey == null || secretKey.length() < 64) {
            throw new IllegalArgumentException("JWT Secret Key must be at least 64 characters long");
        }

        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
    }


    /*
     * generateAccessToken
     *
     * Creates a short-lived access token used to authenticate API requests.
     * The token contains user information such as email and roles.
     */
    public String generateAccessToken(User user) {

        Instant now = Instant.now();

        // Extract role names from Role entities
        List<String> roles = user.getRoles() == null
                ? List.of()
                : user.getRoles().stream().map(Role::getName).toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // unique token ID
                .subject(user.getId().toString()) // user identifier
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))

                // Custom claims added inside JWT payload
                .claims(Map.of(
                        "email", user.getEmail(),
                        "roles", roles,
                        "typ", "access" // token type identifier
                ))

                // Sign the token using HS256 algorithm
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    /*
     * generateRefreshToken
     *
     * Creates a long-lived refresh token used to obtain new access tokens
     * without requiring the user to log in again.
     */
    public String generateRefreshToken(User user, String jti) {

        Instant now = Instant.now();

        return Jwts.builder()
                .id(jti) // token identifier used for tracking in database
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .claim("typ", "refresh")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    /*
     * parse
     *
     * Parses and verifies a JWT token.
     * If the signature or structure is invalid, an exception is thrown.
     */
    public Jws<Claims> parse(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }


    /*
     * Checks whether a token is an access token.
     */
    public boolean isAccessToken(String token) {

        Claims c = parse(token).getPayload();
        return "access".equals(c.get("typ"));
    }


    /*
     * Checks whether a token is a refresh token.
     */
    public boolean isRefreshToken(String token) {

        Claims c = parse(token).getPayload();
        return "refresh".equals(c.get("typ"));
    }


    /*
     * Extracts the user ID stored inside the token subject.
     */
    public UUID getUserId(String token) {

        Claims c = parse(token).getPayload();
        return UUID.fromString(c.getSubject());
    }


    /*
     * Extracts the JTI (JWT ID) used for tracking refresh tokens.
     */
    public String getJti(String token) {

        return parse(token).getPayload().getId();
    }


    /*
     * Returns roles stored inside the token.
     * These are used by the security filter to create authorities.
     */
    public List<String> getRoles(String token) {

        Claims c = parse(token).getPayload();
        return (List<String>) c.get("roles");
    }


    /*
     * Extracts the user's email stored inside the token.
     */
    public String getEmail(String token) {

        Claims c = parse(token).getPayload();
        return (String) c.get("email");
    }

}