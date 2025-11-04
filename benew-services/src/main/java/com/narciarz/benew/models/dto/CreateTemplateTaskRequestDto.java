package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.TaskOwnerRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new template task.
 * 
 * <p>Used by POST /templates/{templateId}/tasks to add a task to a template.</p>
 */
public class CreateTemplateTaskRequestDto {
    
    /**
     * Short task title displayed in checklist.
     */
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    /**
     * Optional detailed task description/instructions.
     */
    private String description;
    
    /**
     * Display order in checklist. Lower numbers appear first.
     */
    @NotNull(message = "Task order is required")
    private Integer taskOrder;
    
    /**
     * Role responsible for completing task: MANAGER or USER.
     */
    @NotNull(message = "Owner role is required")
    private TaskOwnerRole ownerRole;

    // Constructors
    public CreateTemplateTaskRequestDto() {
    }

    public CreateTemplateTaskRequestDto(String title, String description, Integer taskOrder, 
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

