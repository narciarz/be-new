package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.UserRole;

import java.util.UUID;

/**
 * DTO for successful login response.
 * 
 * <p>Returned by POST /auth/login on successful authentication.
 * Contains JWT token and user information for client-side role-based access control.</p>
 */
public class LoginResponseDto {
    
    /**
     * JWT authentication token for subsequent API calls.
     * Include this in Authorization header as Bearer token.
     */
    private String token;
    
    /**
     * User ID.
     */
    private UUID userId;
    
    /**
     * User email.
     */
    private String email;
    
    /**
     * User role (ADMIN, MANAGER, USER) for client-side access control.
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

    // Constructors
    public LoginResponseDto() {
    }

    public LoginResponseDto(String token, UUID userId, String email, UserRole role, 
                           String firstName, String lastName) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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
}

