package com.xdcoder.authApp.auth.repositories;

import com.xdcoder.authApp.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/*
 * UserRepository
 *
 * Repository responsible for database operations related to the User entity.
 *
 * By extending JpaRepository, Spring Data JPA automatically provides common
 * CRUD operations such as:
 * - save()
 * - findById()
 * - findAll()
 * - delete()
 *
 * We only define additional query methods that are specific to our application.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

        /*
         * findByEmail
         *
         * Retrieves a user using their email address.
         * Email is typically used as the username in authentication systems.
         *
         * Optional<User> is returned because the user may or may not exist.
         * This forces the caller to handle the "user not found" case safely.
         *
         * Example usage:
         * - During login authentication
         * - During OAuth login when checking if a user already exists
         */
        Optional<User> findByEmail(String email);


        /*
         * existsByEmail
         *
         * Checks if a user with the given email already exists in the database.
         *
         * This is commonly used during user registration to prevent duplicate
         * accounts with the same email.
         *
         * Returns:
         * true  -> user already exists
         * false -> email is available for registration
         */
        boolean existsByEmail(String email);
}