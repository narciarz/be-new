package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.TaskOwnerRole;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for onboarding task response data.
 * 
 * <p>Used by GET /onboarding/{processId}/tasks and 
 * GET /onboarding/{processId}/tasks/{taskId} to return onboarding task information.</p>
 */
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

    // Constructors
    public OnboardingTaskResponseDto() {
    }

    public OnboardingTaskResponseDto(UUID id, UUID onboardingProcessId, String title, 
                                    String description, Integer taskOrder, TaskOwnerRole ownerRole,
                                    Boolean isCompleted, OffsetDateTime createdAt, 
                                    OffsetDateTime updatedAt) {
        this.id = id;
        this.onboardingProcessId = onboardingProcessId;
        this.title = title;
        this.description = description;
        this.taskOrder = taskOrder;
        this.ownerRole = ownerRole;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOnboardingProcessId() {
        return onboardingProcessId;
    }

    public void setOnboardingProcessId(UUID onboardingProcessId) {
        this.onboardingProcessId = onboardingProcessId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTaskOrder() {
        return taskOrder;
    }

    public void setTaskOrder(Integer taskOrder) {
        this.taskOrder = taskOrder;
    }

    public TaskOwnerRole getOwnerRole() {
        return ownerRole;
    }

    public void setOwnerRole(TaskOwnerRole ownerRole) {
        this.ownerRole = ownerRole;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}


