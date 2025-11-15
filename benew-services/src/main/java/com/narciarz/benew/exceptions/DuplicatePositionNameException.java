package com.narciarz.benew.exceptions;

/**
 * Exception thrown when attempting to create or update a template with a position name
 * that already exists in the system.
 * 
 * <p>Position names must be unique across all templates (case-insensitive check).
 * The uniqueness is enforced by a functional database index on LOWER(TRIM(position_name)).</p>
 */
public class DuplicatePositionNameException extends RuntimeException {
    
    /**
     * Constructs a new DuplicatePositionNameException with a default message.
     * 
     * @param positionName the duplicate position name
     */
    public DuplicatePositionNameException(String positionName) {
        super("Template with position name '" + positionName + "' already exists");
    }
    
    /**
     * Constructs a new DuplicatePositionNameException with a custom message.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public DuplicatePositionNameException(String message, Throwable cause) {
        super(message, cause);
    }
}

