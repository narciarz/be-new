package com.narciarz.benew.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a template task is not found.
 * 
 * <p>This is a business exception that triggers a HTTP 404 Not Found response
 * via the {@link GlobalExceptionHandler}.</p>
 */
public class TemplateTaskNotFoundException extends RuntimeException {
    
    /**
     * Constructs exception with task ID.
     * 
     * @param taskId the template task ID that was not found
     */
    public TemplateTaskNotFoundException(UUID taskId) {
        super("Template task not found with id: " + taskId);
    }
    
    /**
     * Constructs exception with task ID and template ID for nested resource.
     * 
     * @param taskId the template task ID that was not found
     * @param templateId the parent template ID
     */
    public TemplateTaskNotFoundException(UUID taskId, UUID templateId) {
        super("Template task not found with id: " + taskId + " in template: " + templateId);
    }
    
    /**
     * Constructs exception with custom message.
     * 
     * @param message custom error message
     */
    public TemplateTaskNotFoundException(String message) {
        super(message);
    }
}

