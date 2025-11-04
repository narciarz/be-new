package com.narciarz.benew.models.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for template response data.
 * 
 * <p>Used by GET /templates and GET /templates/{templateId} to return template information.</p>
 */
public class TemplateResponseDto {
    
    /**
     * Template ID.
     */
    private UUID id;
    
    /**
     * Job position name that this template applies to.
     */
    private String positionName;
    
    /**
     * Record creation timestamp.
     */
    private OffsetDateTime createdAt;
    
    /**
     * Record last update timestamp.
     */
    private OffsetDateTime updatedAt;

    // Constructors
    public TemplateResponseDto() {
    }

    public TemplateResponseDto(UUID id, String positionName, OffsetDateTime createdAt, 
                              OffsetDateTime updatedAt) {
        this.id = id;
        this.positionName = positionName;
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

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
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

