package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.TaskOwnerRole;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing template task.
 * 
 * <p>Used by PUT /templates/{templateId}/tasks/{taskId} to update task details.
 * All fields are optional - only provided fields will be updated.</p>
 */
public class UpdateTemplateTaskRequestDto {
    
    /**
     * Task title.
     */
    @Size(max = 255, message = "Title must not exceed 255 characters")
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

    // Constructors
    public UpdateTemplateTaskRequestDto() {
    }

    public UpdateTemplateTaskRequestDto(String title, String description, Integer taskOrder, 
                                       TaskOwnerRole ownerRole) {
        this.title = title;
        this.description = description;
        this.taskOrder = taskOrder;
        this.ownerRole = ownerRole;
    }

    // Getters and Setters
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
}


