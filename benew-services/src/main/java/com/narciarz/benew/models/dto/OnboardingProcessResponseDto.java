package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.OnboardingStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for onboarding process response data.
 * 
 * <p>Used by GET /onboarding and GET /onboarding/{processId} to return onboarding
 * process information with progress tracking.</p>
 */
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

    // Constructors
    public OnboardingProcessResponseDto() {
    }

    public OnboardingProcessResponseDto(UUID id, UUID userId, String userName, UUID managerId,
                                       String managerName, UUID sourceTemplateId, 
                                       String sourceTemplateName, OnboardingStatus status,
                                       Integer totalTasksCount, Integer completedTasksCount,
                                       Double progressPercentage, OffsetDateTime createdAt,
                                       OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.managerId = managerId;
        this.managerName = managerName;
        this.sourceTemplateId = sourceTemplateId;
        this.sourceTemplateName = sourceTemplateName;
        this.status = status;
        this.totalTasksCount = totalTasksCount;
        this.completedTasksCount = completedTasksCount;
        this.progressPercentage = progressPercentage;
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public UUID getManagerId() {
        return managerId;
    }

    public void setManagerId(UUID managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public UUID getSourceTemplateId() {
        return sourceTemplateId;
    }

    public void setSourceTemplateId(UUID sourceTemplateId) {
        this.sourceTemplateId = sourceTemplateId;
    }

    public String getSourceTemplateName() {
        return sourceTemplateName;
    }

    public void setSourceTemplateName(String sourceTemplateName) {
        this.sourceTemplateName = sourceTemplateName;
    }

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

    public Double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Double progressPercentage) {
        this.progressPercentage = progressPercentage;
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


