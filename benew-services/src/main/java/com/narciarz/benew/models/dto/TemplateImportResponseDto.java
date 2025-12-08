package com.narciarz.benew.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * DTO for CSV template import response data.
 * 
 * <p>Used by POST /templates/import to return import summary and results.</p>
 * <p>Contains information about the created template and imported tasks.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateImportResponseDto {
    
    /**
     * ID of the created template.
     */
    private UUID templateId;
    
    /**
     * Position name for the imported template.
     */
    private String positionName;
    
    /**
     * Total number of tasks imported from CSV.
     */
    private int tasksImported;
    
    /**
     * List of created template task IDs.
     */
    private List<UUID> taskIds;
    
    /**
     * Import status message.
     */
    private String message;
}

