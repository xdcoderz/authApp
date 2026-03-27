package com.xdcoder.authApp.auth.services;

import com.xdcoder.authApp.auth.payload.UserDto;

/*
 * UserService
 *
 * This interface defines all user-related operations for the application.
 * In Spring Boot's layered architecture, the Service layer contains the
 * business logic and sits between the Controller (API layer) and the
 * Repository (database layer).
 *
 * Controllers call these methods, and the actual logic is implemented
 * in a class like UserServiceImpl.
 */
public interface UserService {

    // Creates a new user in the system.
    // UserDto is used instead of the entity to transfer user data
    // safely between layers without exposing the database model.
    UserDto createUser(UserDto userDto);

    // Retrieves a user using their email.
    // Email usually acts as a unique identifier in authentication systems
    // and is commonly used during login.
    UserDto getUserByEmail(String email);

    // Updates an existing user's details.
    // userDto contains updated data and userId identifies which user to modify.
    UserDto updateUser(UserDto userDto, String userId);

    // Deletes a user from the system using their unique userId.
    // Typically used for account removal or admin actions.
    void deleteUser(String userId);

    // Fetches a user using their unique ID.
    // Often used when the system already knows the userId
    // (for example from a token or session).
    UserDto getUserById(String userId);

    // Returns all users in the system.
    // Useful for admin dashboards or user management features.
    Iterable<UserDto> getAllUsers();

}