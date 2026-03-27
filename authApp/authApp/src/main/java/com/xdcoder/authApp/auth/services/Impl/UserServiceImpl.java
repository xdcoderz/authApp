package com.xdcoder.authApp.auth.services.Impl;

import com.xdcoder.authApp.auth.config.AppConstants;
import com.xdcoder.authApp.auth.payload.UserDto;
import com.xdcoder.authApp.auth.entities.Provider;
import com.xdcoder.authApp.auth.entities.Role;
import com.xdcoder.authApp.auth.entities.User;
import com.xdcoder.authApp.exceptions.ResourceNotFoundException;
import com.xdcoder.authApp.auth.helpers.UserHelper;
import com.xdcoder.authApp.auth.repositories.RoleRepository;
import com.xdcoder.authApp.auth.repositories.UserRepository;
import com.xdcoder.authApp.auth.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;

/*
 * Service implementation for UserService.
 *
 * In Spring Boot architecture, the Service layer contains business logic.
 * Controllers call this class to perform operations, and this class
 * interacts with the UserRepository to access the database.
 */
@Service
@RequiredArgsConstructor // Lombok generates a constructor for all final fields (dependency injection)
public class UserServiceImpl implements UserService {

    // Repository handles database operations for User entity
    private final UserRepository userRepository;

    // ModelMapper automatically converts between DTOs and Entities
    // (UserDto <-> User)
    private final ModelMapper modelMapper;

    private final RoleRepository roleRepository;

    @Override
    public UserDto createUser(UserDto userDto) {

        // Basic validation to ensure email is provided
        if(userDto.getEmail() == null || userDto.getEmail().isBlank()){
            throw new IllegalArgumentException("Email is required");
        }

        // Prevent duplicate accounts with the same email
        if(userRepository.existsByEmail(userDto.getEmail())){
            throw new IllegalArgumentException("Email already exists");
        }

        // Convert incoming DTO to database entity
        // DTO is used at API level, entity is used for persistence
        User user = modelMapper.map(userDto, User.class);

        // Set authentication provider (LOCAL for normal signup)
        // If provider is not passed, default to LOCAL
        user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);

        // Enable the account by default after creation
        user.setEnable(true);

        // Role assignment should ideally happen here for authorization
        // Example: ROLE_USER
        // TODO: implement role logic
        //assigning default role to the user can be done here as well
        if(user.getRoles() == null){
            user.setRoles(new HashSet<>());
        }
        Role role = roleRepository
                .findByName("ROLE_" + AppConstants.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        user.getRoles().add(role);

        // Save user to database
        User savedUser = userRepository.save(user);

        // Convert saved entity back to DTO for response
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {

        // Fetch user using email.
        // If not found, throw custom exception instead of returning null.
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with email: " + email));

        // Convert entity to DTO before returning to controller
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {

        // Convert string ID from request into UUID used by the database
        UUID uId = UserHelper.parseUUID(userId);

        // Fetch existing user from database
        User existingUser = userRepository
                .findById(uId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + userId));

        // Update only the fields that are provided in the DTO
        // Email is intentionally not updated in this project

        if (userDto.getName() != null)
            existingUser.setName(userDto.getName());

        // Password update logic should ideally include hashing (e.g., BCrypt)
        // TODO: implement secure password update
        if (userDto.getPassword() != null)
            existingUser.setPassword(userDto.getPassword());

        if (userDto.getImage() != null)
            existingUser.setImage(userDto.getImage());

        if(userDto.getProvider() != null)
            existingUser.setProvider(userDto.getProvider());

        // Enable/disable user account
        existingUser.setEnable(userDto.getEnable());

        // Track last update time
        existingUser.setUpdatedAt(Instant.now());

        // Save updated user back to database
        User updatedUser = userRepository.save(existingUser);

        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public void deleteUser(String userId) {

        // Convert string ID to UUID format
        UUID uId = UserHelper.parseUUID(userId);

        // Fetch user before deleting to ensure it exists
        User user = userRepository
                .findById(uId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + userId));

        // Remove user from database
        userRepository.delete(user);
    }

    @Override
    public UserDto getUserById(String userId) {

        // Retrieve user using ID and convert to DTO
        User user = userRepository
                .findById(UserHelper.parseUUID(userId))
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + userId));

        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public Iterable<UserDto> getAllUsers() {

        /*
         * Fetch all users from the database and convert each entity
         * into a DTO using Java Stream API.
         *
         * Stream API allows processing collections in a functional style
         * (map, filter, collect etc.).
         */
        return userRepository.findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }
}