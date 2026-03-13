package com.xdcoder.authApp.exceptions;

import com.xdcoder.authApp.dtos.ApiError;
import com.xdcoder.authApp.dtos.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*
 * GlobalExceptionHandler
 *
 * This class provides centralized exception handling for the entire application.
 * Instead of handling exceptions separately in each controller, Spring automatically
 * routes exceptions here and returns a structured API error response.
 *
 * This keeps controllers clean and ensures consistent error responses across the API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Logger used to record exceptions for debugging and monitoring
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    /*
     * Handles authentication related exceptions.
     * These typically occur during login or credential validation.
     *
     * Examples:
     * - Invalid username/password
     * - Disabled user account
     * - Expired credentials
     */
    @ExceptionHandler({
            UsernameNotFoundException.class,
            BadCredentialsException.class,
            CredentialsExpiredException.class,
            DisabledException.class
    })
    public ResponseEntity<ApiError> handleAuthException(Exception e, HttpServletRequest request){

        logger.info("exception: {}", e.getClass().getName());

        var apiError = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }


    /*
     * Handles cases where a requested resource does not exist.
     *
     * Example:
     * - User not found
     * - Token not found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception){

        ErrorResponse errorResponse =
                new ErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }


    /*
     * Handles invalid input or bad arguments sent in requests.
     *
     * Example:
     * - Missing required fields
     * - Invalid parameters
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception){

        ErrorResponse errorResponse =
                new ErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}