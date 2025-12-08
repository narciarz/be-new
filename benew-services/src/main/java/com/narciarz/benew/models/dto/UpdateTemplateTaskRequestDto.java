package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.TaskOwnerRole;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for updating an existing template task.
 * 
 * <p>Used by PUT /templates/{templateId}/tasks/{taskId} to update task details.
 * All fields are optional - only provided fields will be updated.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}


