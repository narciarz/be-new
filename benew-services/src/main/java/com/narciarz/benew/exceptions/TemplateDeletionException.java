package com.narciarz.benew.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a template cannot be deleted due to business constraints.
 * 
 * <p>A template cannot be deleted if it has associated template tasks due to the
 * ON DELETE RESTRICT constraint. All template tasks must be deleted first.</p>
 * 
 * <p>This exception can also be thrown for other database-related deletion failures.</p>
 */
public class TemplateDeletionException extends RuntimeException {
    
    /**
     * Constructs a new TemplateDeletionException for templates with associated tasks.
     * 
     * @param templateId the ID of the template that cannot be deleted
     * @param taskCount the number of associated tasks preventing deletion
     */
    public TemplateDeletionException(UUID templateId, long taskCount) {
        super("Cannot delete template with id: " + templateId + 
              ". Template has " + taskCount + " associated task(s). " +
              "Please delete all template tasks first.");
    }
    
    /**
     * Constructs a new TemplateDeletionException with a custom message and cause.
     * Used for general deletion failures (e.g., database errors).
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public TemplateDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}

