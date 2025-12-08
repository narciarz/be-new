package com.narciarz.benew.models.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for manager tasks with user information.
 * 
 * <p>Extends the basic task information with details about the employee
 * the task belongs to. Used in manager's task view.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class ManagerTaskResponseDto {
    
    private UUID id;
    private UUID processId;
    private String title;
    private String description;
    private Integer taskOrder;
    private String ownerRole;
    private Boolean isCompleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    // User information
    private UUID userId;
    private String userFirstName;
    private String userLastName;
    private String userPosition;
    
    // Process information
    private String processStatus;
}
