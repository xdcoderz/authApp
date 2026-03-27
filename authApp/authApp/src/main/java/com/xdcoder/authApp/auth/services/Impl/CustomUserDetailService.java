package com.xdcoder.authApp.auth.services.Impl;

import com.xdcoder.authApp.auth.entities.User;
import com.xdcoder.authApp.exceptions.ResourceNotFoundException;
import com.xdcoder.authApp.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*
 * CustomUserDetailService
 *
 * This service integrates our User entity with Spring Security's
 * authentication mechanism.
 *
 * Spring Security uses the UserDetailsService interface to load
 * user information from the database during login.
 *
 * When a user tries to authenticate, Spring Security automatically
 * calls loadUserByUsername().
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    // Repository used to fetch user data from the database
    private final UserRepository userRepository;

    /*
     * loadUserByUsername
     *
     * This method is called by Spring Security when authentication
     * is attempted (for example during login).
     *
     * In this application the username is actually the user's email.
     * If the user exists, the User entity is returned because it
     * implements the UserDetails interface.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Search for the user by email in the database
        User user = userRepository
                .findByEmail(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invalid email or password"));

        // Returning the user allows Spring Security to check
        // password, roles, and account status
        return user;
    }
}