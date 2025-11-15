package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.OnboardingStatus;

/**
 * DTO for updating an onboarding process.
 * 
 * <p>Used by PUT /onboarding/{processId} to update process status (e.g., marking as ARCHIVED)
 * or adjust denormalized task counters. All fields are optional.</p>
 */
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

    // Constructors
    public UpdateOnboardingProcessRequestDto() {
    }

    public UpdateOnboardingProcessRequestDto(OnboardingStatus status, Integer totalTasksCount, 
                                            Integer completedTasksCount) {
        this.status = status;
        this.totalTasksCount = totalTasksCount;
        this.completedTasksCount = completedTasksCount;
    }

    // Getters and Setters
    public OnboardingStatus getStatus() {
        return status;
    }

    public void setStatus(OnboardingStatus status) {
        this.status = status;
    }

    public Integer getTotalTasksCount() {
        return totalTasksCount;
    }

    public void setTotalTasksCount(Integer totalTasksCount) {
        this.totalTasksCount = totalTasksCount;
    }

    public Integer getCompletedTasksCount() {
        return completedTasksCount;
    }

    public void setCompletedTasksCount(Integer completedTasksCount) {
        this.completedTasksCount = completedTasksCount;
    }
}


