package com.narciarz.benew.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a requested user is not found.
 * 
 * <p>This exception is typically thrown by the service layer when attempting
 * to retrieve or update a user that doesn't exist in the database.</p>
 */
public class UserNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new UserNotFoundException with a default message.
     * 
     * @param userId the ID of the user that was not found
     */
    public UserNotFoundException(UUID userId) {
        super("User not found with id: " + userId);
    }
    
    /**
     * Constructs a new UserNotFoundException with a custom message.
     * 
     * @param message the detail message
     */
    public UserNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new UserNotFoundException with a message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

