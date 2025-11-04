package com.narciarz.benew.models;

/**
 * User role enumeration for role-based access control.
 * 
 * <p>Roles are stored in the database as VARCHAR values using {@code @Enumerated(EnumType.STRING)}.
 * No database-level CHECK constraint is used per architectural decision - validation happens
 * at application level.</p>
 * 
 * <ul>
 *   <li><b>ADMIN</b>: Full system access - can manage users, templates, and all onboarding processes</li>
 *   <li><b>MANAGER</b>: Can view and update onboarding processes for their team members</li>
 *   <li><b>USER</b>: Can view and update their own onboarding tasks</li>
 * </ul>
 */
public enum UserRole {
    /**
     * Administrator with full system access.
     * Can manage users, templates, CSV imports, and all onboarding processes.
     */
    ADMIN,
    
    /**
     * Manager with limited access to their team's onboarding processes.
     * Can view dashboards and update tasks for users they oversee.
     */
    MANAGER,
    
    /**
     * Regular user (employee) with access only to their own onboarding checklist.
     * Can view and mark their assigned tasks as complete.
     */
    USER
}

