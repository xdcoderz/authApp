package com.xdcoder.authApp.dtos;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/*
 * ApiError
 *
 * Standard error response object used when an API request fails.
 * Instead of returning raw exceptions, the backend sends this
 * structured error so the frontend can easily understand what went wrong.
 *
 * Example use cases:
 * - Unauthorized access
 * - Resource not found
 * - Invalid request data
 */
public record ApiError(

        // HTTP status code (example: 401, 404, 500)
        Integer status,

        // Short description of the error
        String error,

        // Detailed message explaining the problem
        String message,

        // API endpoint where the error occurred
        String path,

        // Time when the error happened
        OffsetDateTime timestamp
) {

    /*
     * Factory method to create an error response with the current UTC timestamp.
     * UTC is commonly used in backend systems to avoid timezone inconsistencies.
     */
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(
                status,
                error,
                message,
                path,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    /*
     * Alternative factory method used when timestamp is not required.
     * In some cases (like security responses) the timestamp may be omitted.
     */
    public static ApiError of(int status, String error, String message, String path, boolean noDateTime) {
        return new ApiError(
                status,
                error,
                message,
                path,
                null
        );
    }
}