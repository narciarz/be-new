package com.narciarz.benew.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a requested onboarding process is not found.
 * 
 * <p>This exception is typically thrown by the service layer when attempting
 * to retrieve or update an onboarding process that doesn't exist in the database.</p>
 */
public class OnboardingProcessNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new OnboardingProcessNotFoundException with a default message.
     * 
     * @param processId the ID of the process that was not found
     */
    public OnboardingProcessNotFoundException(UUID processId) {
        super("Onboarding process not found with id: " + processId);
    }
    
    /**
     * Constructs a new OnboardingProcessNotFoundException with a custom message.
     * 
     * @param message the detail message
     */
    public OnboardingProcessNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new OnboardingProcessNotFoundException with a message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public OnboardingProcessNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

