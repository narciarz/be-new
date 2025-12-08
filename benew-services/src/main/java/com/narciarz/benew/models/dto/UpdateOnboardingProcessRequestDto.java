package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.OnboardingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for updating an onboarding process.
 * 
 * <p>Used by PUT /onboarding/{processId} to update process status (e.g., marking as ARCHIVED)
 * or adjust denormalized task counters. All fields are optional.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOnboardingProcessRequestDto {
    
    /**
     * Process status: ACTIVE or ARCHIVED.
     */
    private OnboardingStatus status;
    
    /**
     * Total tasks count (denormalized counter).
     * Typically updated automatically by application.
     */
    private Integer totalTasksCount;
    
    /**
     * Completed tasks count (denormalized counter).
     * Typically updated automatically by application.
     */
    private Integer completedTasksCount;
}


