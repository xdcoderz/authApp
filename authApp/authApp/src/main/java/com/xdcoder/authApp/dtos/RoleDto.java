package com.xdcoder.authApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/*
 * RoleDto
 *
 * DTO used to transfer role information between layers of the application.
 * Roles are used for authorization, meaning they define what actions a user
 * is allowed to perform in the system (for example ROLE_USER, ROLE_ADMIN).
 *
 * This DTO is typically included inside UserDto so that the frontend
 * can know what permissions a user has.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto {

    // Unique identifier for the role
    private UUID id;

    // Name of the role (example: ROLE_USER, ROLE_ADMIN)
    private String name;
}