package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for user response data.
 * 
 * <p>Used by GET /users, GET /users/{userId} to return user information.
 * Password hash is excluded from response for security.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    
    /**
     * User ID.
     */
    private UUID id;
    
    /**
     * User email address.
     */
    private String email;
    
    /**
     * User role (ADMIN, MANAGER, USER).
     */
    private UserRole role;
    
    /**
     * User first name.
     */
    private String firstName;
    
    /**
     * User last name.
     */
    private String lastName;
    
    /**
     * Job position/title.
     */
    private String positionName;
    
    /**
     * Manager ID (if user has a manager).
     */
    private UUID managerId;
    
    /**
     * Manager full name (for display purposes).
     */
    private String managerName;
    
    /**
     * Record creation timestamp.
     */
    private OffsetDateTime createdAt;
    
    /**
     * Record last update timestamp.
     */
    private OffsetDateTime updatedAt;
}

