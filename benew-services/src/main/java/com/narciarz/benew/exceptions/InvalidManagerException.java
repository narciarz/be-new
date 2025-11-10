package com.narciarz.benew.exceptions;

import java.util.UUID;

/**
 * Exception thrown when an invalid manager assignment is attempted.
 * 
 * <p>This can occur when:</p>
 * <ul>
 *   <li>The specified manager does not exist</li>
 *   <li>A circular manager relationship would be created</li>
 *   <li>The manager reference is invalid for other business reasons</li>
 * </ul>
 */
public class InvalidManagerException extends RuntimeException {
    
    /**
     * Constructs a new InvalidManagerException with a default message.
     * 
     * @param managerId the ID of the invalid manager
     */
    public InvalidManagerException(UUID managerId) {
        super("Invalid manager with id: " + managerId);
    }
    
    /**
     * Constructs a new InvalidManagerException with a custom message.
     * 
     * @param message the detail message
     */
    public InvalidManagerException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new InvalidManagerException with a message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public InvalidManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}

