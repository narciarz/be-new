package com.narciarz.benew.models;

/**
 * Task owner role enumeration indicating who is responsible for completing a task.
 * 
 * <p>Owner roles are stored in the database as VARCHAR using {@code @Enumerated(EnumType.STRING)}.
 * This applies to both template tasks and onboarding tasks.</p>
 * 
 * <ul>
 *   <li><b>MANAGER</b>: Task must be completed by the employee's manager</li>
 *   <li><b>USER</b>: Task must be completed by the employee being onboarded</li>
 * </ul>
 */
public enum TaskOwnerRole {
    /**
     * Task is assigned to and must be completed by the manager.
     * Typically used for administrative tasks like providing equipment,
     * setting up accounts, or conducting reviews.
     */
    MANAGER,
    
    /**
     * Task is assigned to and must be completed by the user (employee).
     * Typically used for self-service tasks like reading documentation,
     * completing training modules, or signing acknowledgments.
     */
    USER
}


