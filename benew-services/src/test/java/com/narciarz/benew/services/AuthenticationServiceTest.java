package com.narciarz.benew.services;

import com.narciarz.benew.exceptions.InvalidCredentialsException;
import com.narciarz.benew.models.AppUser;
import com.narciarz.benew.models.UserRole;
import com.narciarz.benew.models.dto.LoginRequestDto;
import com.narciarz.benew.models.dto.LoginResponseDto;
import com.narciarz.benew.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService.
 * 
 * <p>Tests authentication logic with mocked dependencies.</p>
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtService jwtService;
    
    @InjectMocks
    private AuthenticationService authenticationService;
    
    private AppUser testUser;
    private LoginRequestDto loginRequest;
    
    @BeforeEach
    void setUp() {
        testUser = new AppUser();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("$2a$10$hashedPassword");
        testUser.setRole(UserRole.USER);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        
        loginRequest = new LoginRequestDto("test@example.com", "plainPassword");
    }
    
    @Test
    void shouldAuthenticateUserWithValidCredentials() {
        // Given
        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("plainPassword", "$2a$10$hashedPassword"))
                .thenReturn(true);
        when(jwtService.generateToken(testUser))
                .thenReturn("mocked.jwt.token");
        
        // When
        LoginResponseDto response = authenticationService.login(loginRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getUserId()).isEqualTo(testUser.getId());
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.USER);
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        
        verify(userRepository).findByEmailIgnoreCase("test@example.com");
        verify(passwordEncoder).matches("plainPassword", "$2a$10$hashedPassword");
        verify(jwtService).generateToken(testUser);
    }
    
    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findByEmailIgnoreCase(anyString()))
                .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
        
        verify(userRepository).findByEmailIgnoreCase("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any());
    }
    
    @Test
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        // Given
        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("plainPassword", "$2a$10$hashedPassword"))
                .thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
        
        verify(userRepository).findByEmailIgnoreCase("test@example.com");
        verify(passwordEncoder).matches("plainPassword", "$2a$10$hashedPassword");
        verify(jwtService, never()).generateToken(any());
    }
    
    @Test
    void shouldAuthenticateWithCaseInsensitiveEmail() {
        // Given
        LoginRequestDto requestWithUpperCaseEmail = new LoginRequestDto("TEST@EXAMPLE.COM", "plainPassword");
        when(userRepository.findByEmailIgnoreCase("TEST@EXAMPLE.COM"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("plainPassword", "$2a$10$hashedPassword"))
                .thenReturn(true);
        when(jwtService.generateToken(testUser))
                .thenReturn("mocked.jwt.token");
        
        // When
        LoginResponseDto response = authenticationService.login(requestWithUpperCaseEmail);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com"); // Original email from database
        
        verify(userRepository).findByEmailIgnoreCase("TEST@EXAMPLE.COM");
    }
    
    @Test
    void shouldGenerateTokenWithCorrectUserData() {
        // Given
        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("plainPassword", "$2a$10$hashedPassword"))
                .thenReturn(true);
        when(jwtService.generateToken(testUser))
                .thenReturn("mocked.jwt.token");
        
        // When
        authenticationService.login(loginRequest);
        
        // Then
        verify(jwtService).generateToken(testUser);
    }
    
    @Test
    void shouldAuthenticateAdminUser() {
        // Given
        testUser.setRole(UserRole.ADMIN);
        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("plainPassword", "$2a$10$hashedPassword"))
                .thenReturn(true);
        when(jwtService.generateToken(testUser))
                .thenReturn("admin.jwt.token");
        
        // When
        LoginResponseDto response = authenticationService.login(loginRequest);
        
        // Then
        assertThat(response.getRole()).isEqualTo(UserRole.ADMIN);
    }
    
    @Test
    void shouldAuthenticateManagerUser() {
        // Given
        testUser.setRole(UserRole.MANAGER);
        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("plainPassword", "$2a$10$hashedPassword"))
                .thenReturn(true);
        when(jwtService.generateToken(testUser))
                .thenReturn("manager.jwt.token");
        
        // When
        LoginResponseDto response = authenticationService.login(loginRequest);
        
        // Then
        assertThat(response.getRole()).isEqualTo(UserRole.MANAGER);
    }
}

