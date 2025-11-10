package com.narciarz.benew.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.narciarz.benew.exceptions.DuplicateEmailException;
import com.narciarz.benew.exceptions.InvalidManagerException;
import com.narciarz.benew.exceptions.UserDeletionException;
import com.narciarz.benew.exceptions.UserNotFoundException;
import com.narciarz.benew.models.UserRole;
import com.narciarz.benew.models.dto.CreateUserRequestDto;
import com.narciarz.benew.models.dto.UpdateUserRequestDto;
import com.narciarz.benew.models.dto.UserResponseDto;
import com.narciarz.benew.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController using @WebMvcTest.
 * 
 * <p>Tests the web layer (controller) in isolation with mocked service layer.
 * Uses MockMvc to perform HTTP requests and verify responses.</p>
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController Integration Tests")
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private UserService userService;
    
    private UserResponseDto userResponseDto;
    private CreateUserRequestDto createUserDto;
    private UpdateUserRequestDto updateUserDto;
    private UUID testUserId;
    private UUID testManagerId;
    
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testManagerId = UUID.randomUUID();
        
        userResponseDto = new UserResponseDto(
                testUserId,
                "test@example.com",
                UserRole.USER,
                "John",
                "Doe",
                "developer",
                testManagerId,
                "Jane Manager",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
        
        createUserDto = new CreateUserRequestDto(
                "test@example.com",
                "password123",
                "John",
                "Doe",
                "Developer",
                testManagerId,
                UserRole.USER
        );
        
        updateUserDto = new UpdateUserRequestDto(
                "updated@example.com",
                null,
                "John Updated",
                "Doe Updated",
                "Senior Developer",
                null,
                UserRole.MANAGER
        );
    }
    
    // ========== GET /api/users Tests ==========
    
    @Test
    @DisplayName("GET /api/users - should return paginated users")
    void getAllUsers_ShouldReturnPaginatedUsers() throws Exception {
        // Arrange
        Page<UserResponseDto> userPage = new PageImpl<>(List.of(userResponseDto));
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);
        
        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email").value("test@example.com"))
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.content[0].lastName").value("Doe"));
        
        verify(userService).getAllUsers(any(Pageable.class));
    }
    
    @Test
    @DisplayName("GET /api/users?role=USER - should filter by role")
    void getAllUsers_ShouldFilterByRole() throws Exception {
        // Arrange
        Page<UserResponseDto> userPage = new PageImpl<>(List.of(userResponseDto));
        when(userService.getUsersByRole(eq(UserRole.USER), any(Pageable.class))).thenReturn(userPage);
        
        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .param("role", "USER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
        
        verify(userService).getUsersByRole(eq(UserRole.USER), any(Pageable.class));
        verify(userService, never()).getAllUsers(any(Pageable.class));
    }
    
    @Test
    @DisplayName("GET /api/users?managerId={id} - should filter by manager")
    void getAllUsers_ShouldFilterByManager() throws Exception {
        // Arrange
        Page<UserResponseDto> userPage = new PageImpl<>(List.of(userResponseDto));
        when(userService.getUsersByManager(eq(testManagerId), any(Pageable.class))).thenReturn(userPage);
        
        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .param("managerId", testManagerId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
        
        verify(userService).getUsersByManager(eq(testManagerId), any(Pageable.class));
    }
    
    @Test
    @DisplayName("GET /api/users?position=developer - should filter by position")
    void getAllUsers_ShouldFilterByPosition() throws Exception {
        // Arrange
        Page<UserResponseDto> userPage = new PageImpl<>(List.of(userResponseDto));
        when(userService.getUsersByPosition(eq("developer"), any(Pageable.class))).thenReturn(userPage);
        
        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .param("position", "developer")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
        
        verify(userService).getUsersByPosition(eq("developer"), any(Pageable.class));
    }
    
    @Test
    @DisplayName("GET /api/users?lastName=Doe - should filter by last name")
    void getAllUsers_ShouldFilterByLastName() throws Exception {
        // Arrange
        Page<UserResponseDto> userPage = new PageImpl<>(List.of(userResponseDto));
        when(userService.getUsersByLastName(eq("Doe"), any(Pageable.class))).thenReturn(userPage);
        
        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .param("lastName", "Doe")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
        
        verify(userService).getUsersByLastName(eq("Doe"), any(Pageable.class));
    }
    
    // ========== GET /api/users/{userId} Tests ==========
    
    @Test
    @DisplayName("GET /api/users/{userId} - should return user when found")
    void getUserById_ShouldReturnUser_WhenExists() throws Exception {
        // Arrange
        when(userService.getUserById(testUserId)).thenReturn(userResponseDto);
        
        // Act & Assert
        mockMvc.perform(get("/api/users/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("USER"));
        
        verify(userService).getUserById(testUserId);
    }
    
    @Test
    @DisplayName("GET /api/users/{userId} - should return 404 when user not found")
    void getUserById_ShouldReturn404_WhenNotFound() throws Exception {
        // Arrange
        when(userService.getUserById(testUserId)).thenThrow(new UserNotFoundException(testUserId));
        
        // Act & Assert
        mockMvc.perform(get("/api/users/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(containsString(testUserId.toString())));
        
        verify(userService).getUserById(testUserId);
    }
    
    // ========== POST /api/users Tests ==========
    
    @Test
    @DisplayName("POST /api/users - should create user successfully")
    void createUser_ShouldCreateSuccessfully() throws Exception {
        // Arrange
        when(userService.createUser(any(CreateUserRequestDto.class))).thenReturn(userResponseDto);
        
        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
        
        verify(userService).createUser(any(CreateUserRequestDto.class));
    }
    
    @Test
    @DisplayName("POST /api/users - should return 400 for invalid email")
    void createUser_ShouldReturn400_ForInvalidEmail() throws Exception {
        // Arrange
        CreateUserRequestDto invalidDto = new CreateUserRequestDto(
                "invalid-email",
                "password123",
                "John",
                "Doe",
                "Developer",
                null,
                UserRole.USER
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[*].field", hasItem("email")));
        
        verify(userService, never()).createUser(any());
    }
    
    @Test
    @DisplayName("POST /api/users - should return 400 for short password")
    void createUser_ShouldReturn400_ForShortPassword() throws Exception {
        // Arrange
        CreateUserRequestDto invalidDto = new CreateUserRequestDto(
                "test@example.com",
                "short",
                "John",
                "Doe",
                "Developer",
                null,
                UserRole.USER
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[*].field", hasItem("password")));
        
        verify(userService, never()).createUser(any());
    }
    
    @Test
    @DisplayName("POST /api/users - should return 400 for missing required fields")
    void createUser_ShouldReturn400_ForMissingFields() throws Exception {
        // Arrange
        CreateUserRequestDto invalidDto = new CreateUserRequestDto(
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[*].field", 
                        hasItems("email", "password", "firstName", "lastName", "role")));
        
        verify(userService, never()).createUser(any());
    }
    
    @Test
    @DisplayName("POST /api/users - should return 400 when email exists")
    void createUser_ShouldReturn400_WhenEmailExists() throws Exception {
        // Arrange
        when(userService.createUser(any(CreateUserRequestDto.class)))
                .thenThrow(new DuplicateEmailException("test@example.com"));
        
        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("test@example.com")));
        
        verify(userService).createUser(any(CreateUserRequestDto.class));
    }
    
    @Test
    @DisplayName("POST /api/users - should return 400 when manager invalid")
    void createUser_ShouldReturn400_WhenManagerInvalid() throws Exception {
        // Arrange
        when(userService.createUser(any(CreateUserRequestDto.class)))
                .thenThrow(new InvalidManagerException(testManagerId));
        
        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString(testManagerId.toString())));
        
        verify(userService).createUser(any(CreateUserRequestDto.class));
    }
    
    // ========== PUT /api/users/{userId} Tests ==========
    
    @Test
    @DisplayName("PUT /api/users/{userId} - should update user successfully")
    void updateUser_ShouldUpdateSuccessfully() throws Exception {
        // Arrange
        when(userService.updateUser(eq(testUserId), any(UpdateUserRequestDto.class)))
                .thenReturn(userResponseDto);
        
        // Act & Assert
        mockMvc.perform(put("/api/users/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId.toString()));
        
        verify(userService).updateUser(eq(testUserId), any(UpdateUserRequestDto.class));
    }
    
    @Test
    @DisplayName("PUT /api/users/{userId} - should return 404 when user not found")
    void updateUser_ShouldReturn404_WhenNotFound() throws Exception {
        // Arrange
        when(userService.updateUser(eq(testUserId), any(UpdateUserRequestDto.class)))
                .thenThrow(new UserNotFoundException(testUserId));
        
        // Act & Assert
        mockMvc.perform(put("/api/users/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        
        verify(userService).updateUser(eq(testUserId), any(UpdateUserRequestDto.class));
    }
    
    @Test
    @DisplayName("PUT /api/users/{userId} - should return 400 for invalid email")
    void updateUser_ShouldReturn400_ForInvalidEmail() throws Exception {
        // Arrange
        UpdateUserRequestDto invalidDto = new UpdateUserRequestDto(
                "invalid-email",
                null,
                null,
                null,
                null,
                null,
                null
        );
        
        // Act & Assert
        mockMvc.perform(put("/api/users/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[*].field", hasItem("email")));
        
        verify(userService, never()).updateUser(any(), any());
    }
    
    // ========== DELETE /api/users/{userId} Tests ==========
    
    @Test
    @DisplayName("DELETE /api/users/{userId} - should delete user successfully")
    void deleteUser_ShouldDeleteSuccessfully() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(testUserId);
        
        // Act & Assert
        mockMvc.perform(delete("/api/users/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        
        verify(userService).deleteUser(testUserId);
    }
    
    @Test
    @DisplayName("DELETE /api/users/{userId} - should return 404 when user not found")
    void deleteUser_ShouldReturn404_WhenNotFound() throws Exception {
        // Arrange
        doThrow(new UserNotFoundException(testUserId)).when(userService).deleteUser(testUserId);
        
        // Act & Assert
        mockMvc.perform(delete("/api/users/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        
        verify(userService).deleteUser(testUserId);
    }
    
    @Test
    @DisplayName("DELETE /api/users/{userId} - should return 400 when user has employees")
    void deleteUser_ShouldReturn400_WhenUserHasEmployees() throws Exception {
        // Arrange
        doThrow(new UserDeletionException(testUserId, 3L)).when(userService).deleteUser(testUserId);
        
        // Act & Assert
        mockMvc.perform(delete("/api/users/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("3 employee(s)")));
        
        verify(userService).deleteUser(testUserId);
    }
}
