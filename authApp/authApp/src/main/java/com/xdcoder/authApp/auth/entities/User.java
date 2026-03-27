package com.xdcoder.authApp.auth.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;

/*
 * User Entity
 *
 * Represents an application user stored in the database.
 * This class also implements Spring Security's UserDetails interface,
 * which allows Spring Security to directly use this entity during
 * authentication and authorization.
 *
 * Because of UserDetails implementation, Spring Security can read:
 * - username
 * - password
 * - roles (authorities)
 * - account status
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User implements UserDetails {

    // Primary key for the user
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID id;

    // Email used as the login username
    @Column(name = "user_email", unique = true, nullable = false)
    private String email;

    // Display name of the user
    @Column(name = "user_name")
    private String name;

    // Password used for authentication (usually stored hashed using BCrypt)
    private String password;

    // Profile image URL
    private String image;

    // Indicates whether the user account is active
    private boolean enable = true;

    // Timestamp when the account was created
    private Instant createdAt = Instant.now();

    // Timestamp when the account was last updated
    private Instant updatedAt = Instant.now();

    /*
     * Authentication provider used for login.
     * Examples:
     * LOCAL  -> email/password login
     * GOOGLE -> Google OAuth login
     * GITHUB -> GitHub OAuth login
     */
    @Enumerated(EnumType.STRING)
    private Provider provider = Provider.LOCAL;

    // ID provided by OAuth providers (Google/GitHub user id)
    private String providerId;

    /*
     * Many-to-many relationship between users and roles.
     *
     * One user can have multiple roles.
     * Example:
     * - ROLE_USER
     * - ROLE_ADMIN
     *
     * FetchType.EAGER ensures roles are loaded immediately
     * when the user is fetched (required for authorization).
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();


    /*
     * Automatically runs before inserting a new record.
     * Ensures timestamps are set properly.
     */
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    /*
     * Automatically updates the updatedAt timestamp
     * whenever the user entity is modified.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }


    /*
     * Converts roles into Spring Security authorities.
     * Authorities are used by Spring Security to check permissions.
     *
     * Example:
     * ROLE_ADMIN -> SimpleGrantedAuthority("ROLE_ADMIN")
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<SimpleGrantedAuthority> authorities =
                roles.stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .toList();

        return authorities;
    }


    /*
     * Spring Security uses this method as the username field.
     * In this application, email acts as the username.
     */
    @Override
    public String getUsername() {
        return this.email;
    }

    // Account expiration logic (not used here, always valid)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // Account lock logic (not implemented, always unlocked)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // Credential expiration logic (not implemented)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Returns whether the account is enabled
    @Override
    public boolean isEnabled() {
        return this.enable;
    }
}