package com.xdcoder.authApp.auth.repositories;

import com.xdcoder.authApp.auth.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/*
 * RefreshTokenRepository
 *
 * Repository interface used to interact with the refresh_tokens table.
 * It extends JpaRepository, which provides built-in database operations
 * like save(), findById(), delete(), findAll(), etc.
 *
 * Spring Data JPA automatically generates the implementation at runtime,
 * so we only need to declare the interface and method signatures.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /*
     * findByJti
     *
     * Retrieves a refresh token using its JTI (JWT ID).
     *
     * JTI is a unique identifier stored inside the JWT refresh token.
     * It is used to track tokens in the database and allows the system
     * to validate, revoke, or rotate refresh tokens during authentication.
     *
     * Example use case:
     * During token refresh, the backend extracts the JTI from the JWT
     * and searches for the corresponding refresh token in the database.
     */
    Optional<RefreshToken> findByJti(String jti);
}