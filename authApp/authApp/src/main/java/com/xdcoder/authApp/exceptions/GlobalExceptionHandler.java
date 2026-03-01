package com.xdcoder.authApp.exceptions;

import com.xdcoder.authApp.dtos.ApiError;
import com.xdcoder.authApp.dtos.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({
            UsernameNotFoundException.class,
            BadCredentialsException.class,
            CredentialsExpiredException.class,
            DisabledException.class
    })
    public ResponseEntity<ApiError> handleAuthException(Exception e, HttpServletRequest request){
        logger.info("exception: {}", e.getClass().getName());
        var apiError = ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request", e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    //resource not found exception handler
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception){
       ErrorResponse internalServerError = new ErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND);
       return ResponseEntity.status(HttpStatus.NOT_FOUND).body(internalServerError);
    }

    //resource not found exception handler
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception){
        ErrorResponse internalServerError = new ErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(internalServerError);
    }
}
