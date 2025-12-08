package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.TaskOwnerRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for creating a new template task.
 * 
 * <p>Used by POST /templates/{templateId}/tasks to add a task to a template.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}


