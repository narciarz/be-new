package com.narciarz.benew.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for template response data.
 * 
 * <p>Used by GET /templates and GET /templates/{templateId} to return template information.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}


