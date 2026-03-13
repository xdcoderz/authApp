package com.xdcoder.authApp.exceptions;

/*
 * ResourceNotFoundException
 *
 * Custom exception used when a requested resource does not exist
 * in the system.
 *
 * Instead of returning null from the service layer, this exception
 * is thrown when something like a user, token, or record cannot be
 * found in the database.
 *
 * Example use cases:
 * - User not found by ID
 * - User not found by email
 * - Refresh token not found
 *
 * This exception is handled by GlobalExceptionHandler which converts
 * it into a proper HTTP response (usually 404 NOT FOUND).
 */
public class ResourceNotFoundException extends RuntimeException {

    // Constructor that allows sending a custom error message
    public ResourceNotFoundException(String message){
        super(message);
    }

    // Default constructor with a standard message
    public ResourceNotFoundException(){
        super("Resource not found!");
    }
}