package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.TaskOwnerRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for onboarding task response data.
 * 
 * <p>Used by GET /onboarding/{processId}/tasks and 
 * GET /onboarding/{processId}/tasks/{taskId} to return onboarding task information.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingTaskResponseDto {
    
    /**
     * Task ID.
     */
    private UUID id;
    
    /**
     * Onboarding process ID that this task belongs to.
     */
    private UUID onboardingProcessId;
    
    /**
     * Task title.
     */
    private String title;
    
    /**
     * Task description.
     */
    private String description;
    
    /**
     * Display order in checklist.
     */
    private Integer taskOrder;
    
    /**
     * Role responsible for completing task: MANAGER or USER.
     */
    private TaskOwnerRole ownerRole;
    
    /**
     * Task completion status.
     */
    private Boolean isCompleted;
    
    /**
     * Record creation timestamp.
     */
    private OffsetDateTime createdAt;
    
    /**
     * Record last update timestamp.
     */
    private OffsetDateTime updatedAt;
}


