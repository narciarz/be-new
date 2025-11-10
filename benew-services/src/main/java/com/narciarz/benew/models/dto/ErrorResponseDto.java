package com.narciarz.benew.models.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Standard error response DTO for API error handling.
 * 
 * <p>Provides consistent error structure across all API endpoints.
 * Used by {@code @ControllerAdvice} exception handlers to return
 * user-friendly error messages.</p>
 */
public record ErrorResponseDto(
        /**
         * Timestamp when the error occurred.
         */
        OffsetDateTime timestamp,
        
        /**
         * HTTP status code (e.g., 400, 404, 500).
         */
        int status,
        
        /**
         * HTTP status reason phrase (e.g., "Bad Request", "Not Found").
         */
        String error,
        
        /**
         * Main error message describing what went wrong.
         */
        String message,
        
        /**
         * Request path where the error occurred.
         */
        String path,
        
        /**
         * Optional list of validation errors.
         * Used for Bean Validation failures (e.g., field-level errors).
         */
        List<ValidationError> validationErrors
) {
    
    /**
     * Constructor for simple errors without validation details.
     * 
     * @param timestamp when the error occurred
     * @param status HTTP status code
     * @param error HTTP status reason phrase
     * @param message error message
     * @param path request path
     */
    public ErrorResponseDto(OffsetDateTime timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message, path, null);
    }
    
    /**
     * Represents a single validation error for a specific field.
     */
    public record ValidationError(
            /**
             * Name of the field that failed validation.
             */
            String field,
            
            /**
             * Validation error message for the field.
             */
            String message
    ) {}
}
