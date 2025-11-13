package com.narciarz.benew.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a user cannot be deleted due to business constraints.
 * 
 * <p>This typically occurs when attempting to delete a manager who still has
 * employees assigned to them. The database constraint (ON DELETE RESTRICT)
 * prevents such deletions, and this exception provides a user-friendly message.</p>
 */
public class UserDeletionException extends RuntimeException {
    
    /**
     * Constructs a new UserDeletionException with a default message.
     * 
     * @param userId the ID of the user that cannot be deleted
     */
    public UserDeletionException(UUID userId) {
        super("Cannot delete user with id: " + userId + ". User may have associated records.");
    }
    
    /**
     * Constructs a new UserDeletionException with a custom message.
     * 
     * @param message the detail message
     */
    public UserDeletionException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new UserDeletionException indicating manager has employees.
     * 
     * @param userId the manager's ID
     * @param employeeCount the number of employees reporting to the manager
     */
    public UserDeletionException(UUID userId, long employeeCount) {
        super("Cannot delete manager with id: " + userId + 
              ". Manager still has " + employeeCount + " employee(s) assigned.");
    }
    
    /**
     * Constructs a new UserDeletionException with a message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public UserDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}

