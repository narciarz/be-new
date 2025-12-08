package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.TaskOwnerRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for template task response data.
 * 
 * <p>Used by GET /templates/{templateId}/tasks to return template task information.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateTaskResponseDto {
    
    /**
     * Task ID.
     */
    private UUID id;
    
    /**
     * Template ID that this task belongs to.
     */
    private UUID templateId;
    
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
     * Record creation timestamp.
     */
    private OffsetDateTime createdAt;
    
    /**
     * Record last update timestamp.
     */
    private OffsetDateTime updatedAt;
}


