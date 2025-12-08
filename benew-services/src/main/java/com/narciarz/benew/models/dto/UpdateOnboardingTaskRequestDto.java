package com.narciarz.benew.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for updating an onboarding task.
 * 
 * <p>Used by PUT /onboarding/{processId}/tasks/{taskId} to update task status
 * (e.g., marking as completed).</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOnboardingTaskRequestDto {
    
    /**
     * Task completion status. Set to true to mark task as completed.
     * Triggers update of completed_tasks_count in parent onboarding_process.
     */
    private Boolean isCompleted;
}


