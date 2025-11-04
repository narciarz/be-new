package com.narciarz.benew.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login request.
 * 
 * <p>Used by POST /auth/login to authenticate user credentials and obtain JWT token.</p>
 */
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

    // Constructors
    public LoginRequestDto() {
    }

    public LoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
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
}

