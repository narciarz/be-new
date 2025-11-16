package com.narciarz.benew.services;

import com.narciarz.benew.models.AppUser;
import com.narciarz.benew.models.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtService.
 * 
 * <p>Tests JWT token generation, validation, and claim extraction.
 * Uses plain unit testing without Spring context for faster execution.</p>
 */
class JwtServiceTest {
    
    private JwtService jwtService;
    private AppUser testUser;
    
    @BeforeEach
    void setUp() {
        // Initialize JwtService with test configuration
        String testSecret = "testSecretKeyForJwtTokenGenerationAndValidationMustBeLongEnough";
        long testExpiration = 3600000; // 1 hour in milliseconds
        
        jwtService = new JwtService(testSecret, testExpiration);
        
        testUser = new AppUser();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setRole(UserRole.USER);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
    }
    
    @Test
    void shouldGenerateValidToken() {
        // When
        String token = jwtService.generateToken(testUser);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }
    
    @Test
    void shouldValidateCorrectToken() {
        // Given
        String token = jwtService.generateToken(testUser);
        
        // When
        boolean isValid = jwtService.validateToken(token);
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";
        
        // When
        boolean isValid = jwtService.validateToken(invalidToken);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    void shouldExtractUserIdFromToken() {
        // Given
        String token = jwtService.generateToken(testUser);
        
        // When
        UUID extractedUserId = jwtService.extractUserId(token);
        
        // Then
        assertThat(extractedUserId).isEqualTo(testUser.getId());
    }
    
    @Test
    void shouldExtractEmailFromToken() {
        // Given
        String token = jwtService.generateToken(testUser);
        
        // When
        String extractedEmail = jwtService.extractEmail(token);
        
        // Then
        assertThat(extractedEmail).isEqualTo("test@example.com");
    }
    
    @Test
    void shouldExtractRoleFromToken() {
        // Given
        String token = jwtService.generateToken(testUser);
        
        // When
        String extractedRole = jwtService.extractRole(token);
        
        // Then
        assertThat(extractedRole).isEqualTo("USER");
    }
    
    @Test
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // Given
        AppUser user1 = new AppUser();
        user1.setId(UUID.randomUUID());
        user1.setEmail("user1@example.com");
        user1.setRole(UserRole.USER);
        
        AppUser user2 = new AppUser();
        user2.setId(UUID.randomUUID());
        user2.setEmail("user2@example.com");
        user2.setRole(UserRole.ADMIN);
        
        // When
        String token1 = jwtService.generateToken(user1);
        String token2 = jwtService.generateToken(user2);
        
        // Then
        assertThat(token1).isNotEqualTo(token2);
    }
    
    @Test
    void shouldNotBeExpiredImmediately() {
        // Given
        String token = jwtService.generateToken(testUser);
        
        // When
        boolean isExpired = jwtService.isTokenExpired(token);
        
        // Then
        assertThat(isExpired).isFalse();
    }
    
    @Test
    void shouldIncludeAllRequiredClaimsInToken() {
        // Given
        String token = jwtService.generateToken(testUser);
        
        // When - Extract all claims
        UUID userId = jwtService.extractUserId(token);
        String email = jwtService.extractEmail(token);
        String role = jwtService.extractRole(token);
        
        // Then - All claims should be present and correct
        assertThat(userId).isNotNull();
        assertThat(email).isNotNull();
        assertThat(role).isNotNull();
        assertThat(userId).isEqualTo(testUser.getId());
        assertThat(email).isEqualTo(testUser.getEmail());
        assertThat(role).isEqualTo(testUser.getRole().name());
    }
}

