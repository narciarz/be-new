package com.narciarz.benew.exceptions;

/**
 * Exception thrown when attempting to create or update a user with an email
 * that already exists in the system.
 * 
 * <p>Email addresses must be unique across all users (case-insensitive check).</p>
 */
public class DuplicateEmailException extends RuntimeException {
    
    /**
     * Constructs a new DuplicateEmailException with a default message.
     * 
     * @param email the duplicate email address
     */
    public DuplicateEmailException(String email) {
        super("User with email already exists: " + email);
    }
    
    /**
     * Constructs a new DuplicateEmailException with a custom message.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}

