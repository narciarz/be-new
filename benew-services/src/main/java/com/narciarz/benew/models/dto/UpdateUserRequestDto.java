package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO for updating an existing user.
 * 
 * <p>Used by PUT /users/{userId} to update user details.
 * All fields are optional - only provided fields will be updated.
 * Password is excluded unless explicitly being changed.</p>
 */
public class UpdateUserRequestDto {
    
    /**
     * User email address. Must be unique if provided.
     */
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    /**
     * New password (optional). If provided, will be hashed with BCrypt.
     */
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    /**
     * User first name.
     */
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;
    
    /**
     * User last name.
     */
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;
    
    /**
     * Job position/title.
     */
    @Size(max = 50, message = "Position name must not exceed 50 characters")
    private String positionName;
    
    /**
     * Manager ID. References another user who will be this user's manager.
     */
    private UUID managerId;
    
    /**
     * User role: ADMIN, MANAGER, or USER.
     */
    private UserRole role;

    // Constructors
    public UpdateUserRequestDto() {
    }

    public UpdateUserRequestDto(String email, String password, String firstName, String lastName,
                               String positionName, UUID managerId, UserRole role) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.positionName = positionName;
        this.managerId = managerId;
        this.role = role;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}

