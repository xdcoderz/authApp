package com.xdcoder.authApp.auth.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

/*
 * Role Entity
 *
 * Represents user roles used for authorization in the system.
 * Roles define what permissions a user has.
 *
 * Example roles:
 * - ROLE_USER  → normal application user
 * - ROLE_ADMIN → administrator with higher privileges
 *
 * In a typical security setup, users can have one or more roles
 * which are later checked by Spring Security when accessing
 * protected APIs.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    // Primary key for the role.
    // UUID is used to uniquely identify each role record.
    @Id
    private UUID id = UUID.randomUUID();

    // Name of the role.
    // Marked unique so that duplicate roles (like two ROLE_ADMIN)
    // cannot exist in the database.
    @Column(unique = true, nullable = false)
    private String name;
}