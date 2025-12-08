package com.narciarz.benew.models.dto;

import com.narciarz.benew.models.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for successful login response.
 * 
 * <p>Returned by POST /auth/login on successful authentication.
 * Contains JWT token and user information for client-side role-based access control.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}


