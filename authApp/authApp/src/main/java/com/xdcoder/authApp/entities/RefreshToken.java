package com.xdcoder.authApp.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/*
 * RefreshToken Entity
 *
 * Represents refresh tokens stored in the database.
 * Refresh tokens are used in JWT authentication systems to issue
 * new access tokens without requiring the user to log in again.
 *
 * Storing refresh tokens in the database allows the system to:
 * - revoke tokens on logout
 * - detect token reuse
 * - support refresh token rotation
 */
@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                // Index on JTI (JWT ID) for fast lookup when validating refresh tokens
                @Index(name = "refresh_token_jti_idx", columnList = "jti", unique = true),

                // Index on user_id to quickly fetch tokens belonging to a user
                @Index(name = "refresh_token_user_idx", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    // Primary key of the refresh token record
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // JTI (JWT ID) uniquely identifies each refresh token
    // Used to track and revoke tokens individually
    @Column(name = "jti", nullable = false, unique = true, updatable = false)
    private String jti;

    /*
     * Many refresh tokens can belong to a single user.
     * Example: user logged in from multiple devices.
     *
     * FetchType.LAZY means the User entity will only be loaded
     * when it is actually accessed.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Timestamp when the refresh token was created
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    // Expiration time after which the refresh token becomes invalid
    @Column(nullable = false)
    private Instant expiresAt;

    // Indicates whether the refresh token has been revoked
    // Used for logout or refresh token rotation
    @Column(nullable = false)
    private boolean revoked;

    /*
     * Used during refresh token rotation.
     * When a token is refreshed, the old token is revoked
     * and replaced by a new token whose JTI is stored here.
     */
    private String replacedByToken;

}