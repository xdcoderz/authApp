package com.xdcoder.authApp.auth.payload;

import com.xdcoder.authApp.auth.entities.Provider;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/*
 * UserDto
 *
 * DTO (Data Transfer Object) used to transfer user data between
 * different layers of the application (Controller ↔ Service).
 *
 * Instead of exposing the database entity directly, DTOs help control
 * what data is sent to the client and what data is accepted from requests.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    // Unique identifier of the user (UUID used instead of numeric ID for better security)
    private UUID id;

    // User's email address (commonly used as login username)
    private String email;

    // Display name of the user
    private String name;

    // Password used for authentication (should normally be stored hashed)
    private String password;

    // Profile image URL of the user (for UI display)
    private String image;

    // Indicates whether the user account is active or disabled
    // Builder.Default ensures the default value is true when using Lombok builder
    @Builder.Default
    private Boolean enable = true;

    // Timestamp when the user account was created
    private Instant createdAt = Instant.now();

    // Timestamp of the last update to the user record
    private Instant updatedAt = Instant.now();

    // Authentication provider used for login
    // Example: LOCAL (email/password), GOOGLE, GITHUB
    private Provider provider = Provider.LOCAL;

    // Roles assigned to the user for authorization (ROLE_USER, ROLE_ADMIN etc.)
    // Stored as a set to avoid duplicate roles
    private Set<RoleDto> roles = new HashSet<>();
}