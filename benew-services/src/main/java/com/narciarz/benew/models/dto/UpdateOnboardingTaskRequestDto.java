package com.narciarz.benew.models.dto;

/**
 * DTO for updating an onboarding task.
 * 
 * <p>Used by PUT /onboarding/{processId}/tasks/{taskId} to update task status
 * (e.g., marking as completed).</p>
 */
public class UpdateOnboardingTaskRequestDto {
    
    /**
     * Task completion status. Set to true to mark task as completed.
     * Triggers update of completed_tasks_count in parent onboarding_process.
     */
    private Boolean isCompleted;

    // Constructors
    public UpdateOnboardingTaskRequestDto() {
    }

    public UpdateOnboardingTaskRequestDto(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    // Getters and Setters
    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
}

