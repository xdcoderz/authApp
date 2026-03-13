package com.xdcoder.authApp.controllers;

import com.xdcoder.authApp.dtos.UserDto;
import com.xdcoder.authApp.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
 * UserController
 *
 * This controller exposes REST APIs for user management.
 * Controllers in Spring Boot handle HTTP requests from the client
 * (for example a React frontend) and delegate the actual business
 * logic to the Service layer.
 */
@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    // Service layer that contains the business logic for user operations
    private final UserService userService;


    // Create User API
    // POST /api/v1/users
    // Used when a new user needs to be created (for example admin creating a user)
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(userDto));
    }

    // Get All Users API
    // GET /api/v1/users
    // Returns all users in the system (commonly used in admin dashboards)
    @GetMapping
    public ResponseEntity<Iterable<UserDto>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Get User By Email API
    // GET /api/v1/users/email/{email}
    // Useful when the system needs to retrieve user details using email
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email){
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    // Delete User API
    // DELETE /api/v1/users/{userId}
    // Removes a user from the system using their unique ID
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") String userId){
        userService.deleteUser(userId);
    }

    // Update User API
    // PUT /api/v1/users/{userId}
    // Updates user details such as name, password, profile image, etc.
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @RequestBody UserDto userDto,
            @PathVariable("userId") String userId) {

        return ResponseEntity.ok(userService.updateUser(userDto, userId));
    }

    // Get User By ID API
    // GET /api/v1/users/{userId}
    // Retrieves a specific user using their unique identifier
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

}