package com.narciarz.benew.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for user login request.
 * 
 * <p>Used by POST /auth/login to authenticate user credentials and obtain JWT token.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    
    /**
     * User email address (used as username).
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    /**
     * User password (plain text, will be verified against BCrypt hash).
     */
    @NotBlank(message = "Password is required")
    private String password;
}


