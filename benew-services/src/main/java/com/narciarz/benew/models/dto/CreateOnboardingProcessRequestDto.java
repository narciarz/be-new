package com.narciarz.benew.models.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO for creating a new onboarding process.
 * 
 * <p>Used by POST /onboarding to create a new onboarding process.
 * Typically triggered upon creation of a user account. The backend copies tasks
 * from the relevant template.</p>
 */
public class CreateOnboardingProcessRequestDto {
    
    /**
     * Employee user ID being onboarded.
     */
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    /**
     * Manager user ID overseeing this onboarding.
     */
    @NotNull(message = "Manager ID is required")
    private UUID managerId;
    
    /**
     * Template ID to use as source for copying tasks.
     */
    @NotNull(message = "Source template ID is required")
    private UUID sourceTemplateId;

    // Constructors
    public CreateOnboardingProcessRequestDto() {
    }

    public CreateOnboardingProcessRequestDto(UUID userId, UUID managerId, UUID sourceTemplateId) {
        this.userId = userId;
        this.managerId = managerId;
        this.sourceTemplateId = sourceTemplateId;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getManagerId() {
        return managerId;
    }

    public void setManagerId(UUID managerId) {
        this.managerId = managerId;
    }

    public UUID getSourceTemplateId() {
        return sourceTemplateId;
    }

    public void setSourceTemplateId(UUID sourceTemplateId) {
        this.sourceTemplateId = sourceTemplateId;
    }
}

