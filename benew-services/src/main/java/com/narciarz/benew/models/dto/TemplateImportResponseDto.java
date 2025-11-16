package com.narciarz.benew.models.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO for CSV template import response data.
 * 
 * <p>Used by POST /templates/import to return import summary and results.</p>
 * <p>Contains information about the created template and imported tasks.</p>
 */
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

    // Constructors
    public TemplateImportResponseDto() {
    }

    public TemplateImportResponseDto(UUID templateId, String positionName, 
                                    int tasksImported, List<UUID> taskIds, String message) {
        this.templateId = templateId;
        this.positionName = positionName;
        this.tasksImported = tasksImported;
        this.taskIds = taskIds;
        this.message = message;
    }

    // Getters and Setters
    public UUID getTemplateId() {
        return templateId;
    }

    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public int getTasksImported() {
        return tasksImported;
    }

    public void setTasksImported(int tasksImported) {
        this.tasksImported = tasksImported;
    }

    public List<UUID> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<UUID> taskIds) {
        this.taskIds = taskIds;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

