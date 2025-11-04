package com.narciarz.benew.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new checklist template.
 * 
 * <p>Used by POST /templates to create a new onboarding template for a specific position.</p>
 */
public class CreateTemplateRequestDto {
    
    /**
     * Unique job position name that this template applies to (e.g., "Software Engineer").
     * Application normalizes this value (trim + toLowerCase) before save.
     */
    @NotBlank(message = "Position name is required")
    @Size(max = 50, message = "Position name must not exceed 50 characters")
    private String positionName;

    // Constructors
    public CreateTemplateRequestDto() {
    }

    public CreateTemplateRequestDto(String positionName) {
        this.positionName = positionName;
    }

    // Getters and Setters
    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }
}

