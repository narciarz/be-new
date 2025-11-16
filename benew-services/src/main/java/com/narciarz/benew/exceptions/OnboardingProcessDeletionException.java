package com.narciarz.benew.exceptions;

import java.util.UUID;

/**
 * Exception thrown when an onboarding process cannot be deleted.
 * 
 * <p>This exception is thrown when deletion constraints are violated, such as:</p>
 * <ul>
 *   <li>Process still has associated tasks</li>
 *   <li>Business rules prevent deletion (e.g., active process)</li>
 * </ul>
 */
public class OnboardingProcessDeletionException extends RuntimeException {
    
    /**
     * Constructs a new OnboardingProcessDeletionException with process ID and reason.
     * 
     * @param processId the ID of the process that cannot be deleted
     * @param taskCount the number of associated tasks preventing deletion
     */
    public OnboardingProcessDeletionException(UUID processId, long taskCount) {
        super("Cannot delete onboarding process " + processId + 
              " - still has " + taskCount + " task(s) associated. Delete tasks first.");
    }
    
    /**
     * Constructs a new OnboardingProcessDeletionException with a custom message.
     * 
     * @param message the detail message
     */
    public OnboardingProcessDeletionException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new OnboardingProcessDeletionException with a message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public OnboardingProcessDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}

