package com.narciarz.benew.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a requested template is not found.
 * 
 * <p>This exception is typically thrown by the service layer when attempting
 * to retrieve, update, or delete a template that doesn't exist in the database.</p>
 */
public class TemplateNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new TemplateNotFoundException with a default message.
     * 
     * @param templateId the ID of the template that was not found
     */
    public TemplateNotFoundException(UUID templateId) {
        super("Template not found with id: " + templateId);
    }
    
    /**
     * Constructs a new TemplateNotFoundException with a custom message.
     * 
     * @param message the detail message
     */
    public TemplateNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new TemplateNotFoundException with a message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public TemplateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

