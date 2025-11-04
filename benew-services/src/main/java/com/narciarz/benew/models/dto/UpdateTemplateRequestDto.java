package com.narciarz.benew.models.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing template.
 * 
 * <p>Used by PUT /templates/{templateId} to update template details.
 * All fields are optional - only provided fields will be updated.</p>
 */
public class UpdateTemplateRequestDto {
    
    /**
     * Job position name. Must be unique if provided.
     * Application normalizes this value (trim + toLowerCase) before save.
     */
    @Size(max = 50, message = "Position name must not exceed 50 characters")
    private String positionName;

    // Constructors
    public UpdateTemplateRequestDto() {
    }

    public UpdateTemplateRequestDto(String positionName) {
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

