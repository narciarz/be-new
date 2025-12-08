package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.OnboardingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for onboarding process response data.
 * 
 * <p>Used by GET /onboarding and GET /onboarding/{processId} to return onboarding
 * process information with progress tracking.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingProcessResponseDto {
    
    /**
     * Onboarding process ID.
     */
    private UUID id;
    
    /**
     * Employee user ID being onboarded.
     */
    private UUID userId;
    
    /**
     * Employee full name (for display purposes).
     */
    private String userName;
    
    /**
     * Manager user ID overseeing this onboarding.
     */
    private UUID managerId;
    
    /**
     * Manager full name (for display purposes).
     */
    private String managerName;
    
    /**
     * Source template ID used to create this process.
     */
    private UUID sourceTemplateId;
    
    /**
     * Source template position name (for display purposes).
     */
    private String sourceTemplateName;
    
    /**
     * Process status: ACTIVE or ARCHIVED.
     */
    private OnboardingStatus status;
    
    /**
     * Total number of tasks in this onboarding process.
     */
    private Integer totalTasksCount;
    
    /**
     * Number of completed tasks.
     */
    private Integer completedTasksCount;
    
    /**
     * Progress percentage (calculated from completed/total).
     */
    private Double progressPercentage;
    
    /**
     * Record creation timestamp.
     */
    private OffsetDateTime createdAt;
    
    /**
     * Record last update timestamp.
     */
    private OffsetDateTime updatedAt;
}


