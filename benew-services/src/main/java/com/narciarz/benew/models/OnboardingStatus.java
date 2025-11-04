package com.narciarz.benew.models;

/**
 * Onboarding process status enumeration.
 * 
 * <p>Status values are stored in the database as VARCHAR using {@code @Enumerated(EnumType.STRING)}.
 * The application uses these statuses to filter and manage onboarding processes.</p>
 * 
 * <ul>
 *   <li><b>ACTIVE</b>: Onboarding is currently in progress</li>
 *   <li><b>ARCHIVED</b>: Onboarding has been completed and moved to archive</li>
 * </ul>
 */
public enum OnboardingStatus {
    /**
     * Onboarding process is currently active and in progress.
     * Tasks can be updated and progress tracked.
     */
    ACTIVE,
    
    /**
     * Onboarding process has been completed and archived.
     * Used for historical tracking and reporting. Archived processes
     * are not shown in the active dashboard views.
     */
    ARCHIVED
}

