package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.UserRole;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for user response data.
 * 
 * <p>Used by GET /users, GET /users/{userId} to return user information.
 * Password hash is excluded from response for security.</p>
 */
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

    // Constructors
    public UserResponseDto() {
    }

    public UserResponseDto(UUID id, String email, UserRole role, String firstName, String lastName,
                          String positionName, UUID managerId, String managerName,
                          OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.positionName = positionName;
        this.managerId = managerId;
        this.managerName = managerName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public UUID getManagerId() {
        return managerId;
    }

    public void setManagerId(UUID managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

