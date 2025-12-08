package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for updating an existing user.
 * 
 * <p>Used by PUT /users/{userId} to update user details.
 * All fields are optional - only provided fields will be updated.
 * Password is excluded unless explicitly being changed.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}

