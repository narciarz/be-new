package com.narciarz.benew.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.narciarz.benew.exceptions.InvalidCredentialsException;
import com.narciarz.benew.models.UserRole;
import com.narciarz.benew.models.dto.LoginRequestDto;
import com.narciarz.benew.models.dto.LoginResponseDto;
import com.narciarz.benew.services.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for AuthController.
 * 
 * <p>Uses @WebMvcTest for slice testing - only loads web layer components.
 * Dependencies are mocked with @MockitoBean.</p>
 * 
 * <p>Tests HTTP endpoints, request validation, and response formatting.</p>
 * 
 * <p>Spring Security is configured to permit all requests in tests via
 * TestSecurityConfig which disables authentication for easier testing.</p>
 */
@WebMvcTest(controllers = AuthController.class)
@Import(AuthControllerTest.TestSecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private AuthenticationService authenticationService;
    
    @Test
    void shouldReturnTokenOnSuccessfulLogin() throws Exception {
        // Given
        LoginRequestDto request = new LoginRequestDto("test@example.com", "password123");
        LoginResponseDto response = new LoginResponseDto(
                "mocked.jwt.token",
                UUID.randomUUID(),
                "test@example.com",
                UserRole.USER,
                "John",
                "Doe"
        );
        
        when(authenticationService.login(any(LoginRequestDto.class)))
                .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.userId").exists());
        
        verify(authenticationService).login(any(LoginRequestDto.class));
    }
    
    @Test
    void shouldReturn401OnInvalidCredentials() throws Exception {
        // Given
        LoginRequestDto request = new LoginRequestDto("test@example.com", "wrongPassword");
        
        when(authenticationService.login(any(LoginRequestDto.class)))
                .thenThrow(new InvalidCredentialsException());
        
        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
    
    @Test
    void shouldReturn400WhenEmailIsMissing() throws Exception {
        // Given - Request without email
        String invalidRequest = """
                {
                    "password": "password123"
                }
                """;
        
        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void shouldReturn400WhenPasswordIsMissing() throws Exception {
        // Given - Request without password
        String invalidRequest = """
                {
                    "email": "test@example.com"
                }
                """;
        
        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }
    
    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        // Given - Request with invalid email format
        LoginRequestDto request = new LoginRequestDto("not-an-email", "password123");
        
        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }
    
    @Test
    void shouldReturn400WhenEmailIsBlank() throws Exception {
        // Given - Request with blank email
        LoginRequestDto request = new LoginRequestDto("", "password123");
        
        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }
    
    @Test
    void shouldReturn400WhenPasswordIsBlank() throws Exception {
        // Given - Request with blank password
        LoginRequestDto request = new LoginRequestDto("test@example.com", "");
        
        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }
    
    @Test
    void shouldReturn400WhenRequestBodyIsMalformed() throws Exception {
        // Given - Malformed JSON
        String malformedJson = """
                {
                    "email": "test@example.com",
                    "password": "password123"
                """; // Missing closing brace
        
        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldReturnCorrectResponseForAdminLogin() throws Exception {
        // Given
        LoginRequestDto request = new LoginRequestDto("admin@example.com", "adminPass");
        LoginResponseDto response = new LoginResponseDto(
                "admin.jwt.token",
                UUID.randomUUID(),
                "admin@example.com",
                UserRole.ADMIN,
                "Admin",
                "User"
        );
        
        when(authenticationService.login(any(LoginRequestDto.class)))
                .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.token").value("admin.jwt.token"));
    }
    
    @Test
    void shouldReturnCorrectResponseForManagerLogin() throws Exception {
        // Given
        LoginRequestDto request = new LoginRequestDto("manager@example.com", "managerPass");
        LoginResponseDto response = new LoginResponseDto(
                "manager.jwt.token",
                UUID.randomUUID(),
                "manager@example.com",
                UserRole.MANAGER,
                "Manager",
                "User"
        );
        
        when(authenticationService.login(any(LoginRequestDto.class)))
                .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MANAGER"))
                .andExpect(jsonPath("$.token").value("manager.jwt.token"));
    }
    
    @Test
    void shouldAcceptLoginRequestWithValidData() throws Exception {
        // Given
        LoginRequestDto request = new LoginRequestDto("valid@example.com", "ValidPass123!");
        LoginResponseDto response = new LoginResponseDto(
                "valid.jwt.token",
                UUID.randomUUID(),
                "valid@example.com",
                UserRole.USER,
                "Valid",
                "User"
        );
        
        when(authenticationService.login(any(LoginRequestDto.class)))
                .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.role").exists())
                .andExpect(jsonPath("$.firstName").exists())
                .andExpect(jsonPath("$.lastName").exists());
    }
    
    /**
     * Test-specific security configuration that disables authentication.
     * 
     * <p>Allows testing controller logic without dealing with Spring Security filters.
     * This configuration is only loaded for tests using @Import.</p>
     */
    @TestConfiguration
    static class TestSecurityConfig {
        
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }
}

