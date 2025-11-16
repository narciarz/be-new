package com.narciarz.benew.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a requested onboarding task is not found.
 * 
 * <p>This exception is typically thrown by the service layer when attempting
 * to retrieve or update an onboarding task that doesn't exist in the database.</p>
 */
public class OnboardingTaskNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new OnboardingTaskNotFoundException with a default message.
     * 
     * @param taskId the ID of the task that was not found
     */
    public OnboardingTaskNotFoundException(UUID taskId) {
        super("Onboarding task not found with id: " + taskId);
    }
    
    /**
     * Constructs a new OnboardingTaskNotFoundException with process and task IDs.
     * 
     * @param taskId the ID of the task that was not found
     * @param processId the ID of the process
     */
    public OnboardingTaskNotFoundException(UUID taskId, UUID processId) {
        super("Onboarding task " + taskId + " not found in process " + processId);
    }
    
    /**
     * Constructs a new OnboardingTaskNotFoundException with a custom message.
     * 
     * @param message the detail message
     */
    public OnboardingTaskNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new OnboardingTaskNotFoundException with a message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public OnboardingTaskNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

