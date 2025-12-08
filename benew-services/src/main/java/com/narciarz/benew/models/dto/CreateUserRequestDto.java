package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for creating a new user.
 * 
 * <p>Used by POST /users to create a new user account.
 * Includes establishing manager relationships (if applicable) and assigning roles.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequestDto {
    
    /**
     * User email address (used as username). Must be unique.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    /**
     * User password (plain text, will be hashed with BCrypt).
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    /**
     * User first name.
     */
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;
    
    /**
     * User last name.
     */
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;
    
    /**
     * Job position/title. Used to assign appropriate onboarding template.
     * Optional for administrators.
     */
    @Size(max = 50, message = "Position name must not exceed 50 characters")
    private String positionName;
    
    /**
     * Manager ID (optional). References another user who will be this user's manager.
     */
    private UUID managerId;
    
    /**
     * User role: ADMIN, MANAGER, or USER.
     */
    @NotNull(message = "Role is required")
    private UserRole role;
}

