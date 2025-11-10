package com.narciarz.benew.services;

import com.narciarz.benew.exceptions.DuplicateEmailException;
import com.narciarz.benew.exceptions.InvalidManagerException;
import com.narciarz.benew.exceptions.UserDeletionException;
import com.narciarz.benew.exceptions.UserNotFoundException;
import com.narciarz.benew.models.AppUser;
import com.narciarz.benew.models.UserRole;
import com.narciarz.benew.models.dto.CreateUserRequestDto;
import com.narciarz.benew.models.dto.UpdateUserRequestDto;
import com.narciarz.benew.models.dto.UserResponseDto;
import com.narciarz.benew.repositories.UserRepository;
import com.narciarz.benew.services.mappers.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 * 
 * <p>Tests business logic, validation, and error handling using Mockito
 * to mock dependencies. Follows AAA (Arrange-Act-Assert) pattern.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private AppUser testUser;
    private AppUser testManager;
    private CreateUserRequestDto createUserDto;
    private UpdateUserRequestDto updateUserDto;
    private UserResponseDto userResponseDto;
    private UUID testUserId;
    private UUID testManagerId;
    
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testManagerId = UUID.randomUUID();
        
        // Setup test manager
        testManager = new AppUser();
        testManager.setId(testManagerId);
        testManager.setEmail("manager@example.com");
        testManager.setPasswordHash("hashedPassword");
        testManager.setRole(UserRole.MANAGER);
        testManager.setFirstName("Jane");
        testManager.setLastName("Manager");
        testManager.setPositionName("senior developer");
        testManager.setCreatedAt(OffsetDateTime.now());
        testManager.setUpdatedAt(OffsetDateTime.now());
        
        // Setup test user
        testUser = new AppUser();
        testUser.setId(testUserId);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setRole(UserRole.USER);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPositionName("developer");
        testUser.setManager(testManager);
        testUser.setCreatedAt(OffsetDateTime.now());
        testUser.setUpdatedAt(OffsetDateTime.now());
        
        // Setup DTOs
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
    }
    
    // ========== GET Operations Tests ==========
    
    @Test
    @DisplayName("getAllUsers - should return paginated users")
    void getAllUsers_ShouldReturnPaginatedUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<AppUser> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toResponseDto(testUser)).thenReturn(userResponseDto);
        
        // Act
        Page<UserResponseDto> result = userService.getAllUsers(pageable);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findAll(pageable);
        verify(userMapper).toResponseDto(testUser);
    }
    
    @Test
    @DisplayName("getUsersByRole - should return users filtered by role")
    void getUsersByRole_ShouldReturnFilteredUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<AppUser> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findByRole(UserRole.USER, pageable)).thenReturn(userPage);
        when(userMapper.toResponseDto(testUser)).thenReturn(userResponseDto);
        
        // Act
        Page<UserResponseDto> result = userService.getUsersByRole(UserRole.USER, pageable);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).findByRole(UserRole.USER, pageable);
    }
    
    @Test
    @DisplayName("getUserById - should return user when found")
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDto(testUser)).thenReturn(userResponseDto);
        
        // Act
        UserResponseDto result = userService.getUserById(testUserId);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findById(testUserId);
        verify(userMapper).toResponseDto(testUser);
    }
    
    @Test
    @DisplayName("getUserById - should throw UserNotFoundException when user not found")
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(testUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(testUserId.toString());
        
        verify(userRepository).findById(testUserId);
        verify(userMapper, never()).toResponseDto(any());
    }
    
    // ========== CREATE Operation Tests ==========
    
    @Test
    @DisplayName("createUser - should create user successfully with manager")
    void createUser_ShouldCreateSuccessfully_WithManager() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase(createUserDto.getEmail())).thenReturn(false);
        when(userMapper.toEntity(createUserDto)).thenReturn(testUser);
        when(passwordEncoder.encode(createUserDto.getPassword())).thenReturn("hashedPassword123");
        when(userRepository.findById(testManagerId)).thenReturn(Optional.of(testManager));
        when(userRepository.save(any(AppUser.class))).thenReturn(testUser);
        when(userMapper.toResponseDto(testUser)).thenReturn(userResponseDto);
        
        // Act
        UserResponseDto result = userService.createUser(createUserDto);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        
        verify(userRepository).existsByEmailIgnoreCase(createUserDto.getEmail());
        verify(passwordEncoder).encode(createUserDto.getPassword());
        verify(userRepository).findById(testManagerId);
        verify(userRepository).save(any(AppUser.class));
        verify(userMapper).toResponseDto(testUser);
    }
    
    @Test
    @DisplayName("createUser - should create user without manager")
    void createUser_ShouldCreateSuccessfully_WithoutManager() {
        // Arrange
        CreateUserRequestDto dtoNoManager = new CreateUserRequestDto(
                "admin@example.com",
                "password123",
                "Admin",
                "User",
                null,
                null,
                UserRole.ADMIN
        );
        AppUser adminUser = new AppUser();
        adminUser.setEmail("admin@example.com");
        
        when(userRepository.existsByEmailIgnoreCase(dtoNoManager.getEmail())).thenReturn(false);
        when(userMapper.toEntity(dtoNoManager)).thenReturn(adminUser);
        when(passwordEncoder.encode(dtoNoManager.getPassword())).thenReturn("hashedPassword123");
        when(userRepository.save(any(AppUser.class))).thenReturn(adminUser);
        when(userMapper.toResponseDto(adminUser)).thenReturn(userResponseDto);
        
        // Act
        UserResponseDto result = userService.createUser(dtoNoManager);
        
        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).existsByEmailIgnoreCase(dtoNoManager.getEmail());
        verify(userRepository, never()).findById(any());
        verify(userRepository).save(any(AppUser.class));
    }
    
    @Test
    @DisplayName("createUser - should throw DuplicateEmailException when email exists")
    void createUser_ShouldThrowException_WhenEmailExists() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase(createUserDto.getEmail())).thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(createUserDto))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining(createUserDto.getEmail());
        
        verify(userRepository).existsByEmailIgnoreCase(createUserDto.getEmail());
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("createUser - should throw InvalidManagerException when manager not found")
    void createUser_ShouldThrowException_WhenManagerNotFound() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase(createUserDto.getEmail())).thenReturn(false);
        when(userMapper.toEntity(createUserDto)).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.findById(testManagerId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(createUserDto))
                .isInstanceOf(InvalidManagerException.class)
                .hasMessageContaining(testManagerId.toString());
        
        verify(userRepository).findById(testManagerId);
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("createUser - should normalize position name")
    void createUser_ShouldNormalizePositionName() {
        // Arrange
        CreateUserRequestDto dtoWithPosition = new CreateUserRequestDto(
                "test@example.com",
                "password123",
                "John",
                "Doe",
                "  Senior Developer  ",
                null,
                UserRole.USER
        );
        AppUser user = new AppUser();
        user.setPositionName("  Senior Developer  ");
        
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userMapper.toEntity(dtoWithPosition)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser savedUser = invocation.getArgument(0);
            assertThat(savedUser.getPositionName()).isEqualTo("senior developer");
            return savedUser;
        });
        when(userMapper.toResponseDto(any())).thenReturn(userResponseDto);
        
        // Act
        userService.createUser(dtoWithPosition);
        
        // Assert
        verify(userRepository).save(any(AppUser.class));
    }
    
    // ========== UPDATE Operation Tests ==========
    
    @Test
    @DisplayName("updateUser - should update user successfully")
    void updateUser_ShouldUpdateSuccessfully() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailIgnoreCase(updateUserDto.getEmail())).thenReturn(false);
        doNothing().when(userMapper).updateEntityFromDto(updateUserDto, testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toResponseDto(testUser)).thenReturn(userResponseDto);
        
        // Act
        UserResponseDto result = userService.updateUser(testUserId, updateUserDto);
        
        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).findById(testUserId);
        verify(userMapper).updateEntityFromDto(updateUserDto, testUser);
        verify(userRepository).save(testUser);
    }
    
    @Test
    @DisplayName("updateUser - should throw UserNotFoundException when user not found")
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(testUserId, updateUserDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(testUserId.toString());
        
        verify(userRepository).findById(testUserId);
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("updateUser - should throw DuplicateEmailException when new email exists")
    void updateUser_ShouldThrowException_WhenNewEmailExists() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailIgnoreCase(updateUserDto.getEmail())).thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(testUserId, updateUserDto))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining(updateUserDto.getEmail());
        
        verify(userRepository).findById(testUserId);
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("updateUser - should hash password when provided")
    void updateUser_ShouldHashPassword_WhenProvided() {
        // Arrange
        UpdateUserRequestDto dtoWithPassword = new UpdateUserRequestDto(
                null,
                "newPassword123",
                null,
                null,
                null,
                null,
                null
        );
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newHashedPassword");
        doNothing().when(userMapper).updateEntityFromDto(dtoWithPassword, testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toResponseDto(testUser)).thenReturn(userResponseDto);
        
        // Act
        userService.updateUser(testUserId, dtoWithPassword);
        
        // Assert
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(testUser);
    }
    
    // ========== DELETE Operation Tests ==========
    
    @Test
    @DisplayName("deleteUser - should delete user successfully when no employees")
    void deleteUser_ShouldDeleteSuccessfully_WhenNoEmployees() {
        // Arrange
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(userRepository.countByManagerId(testUserId)).thenReturn(0L);
        doNothing().when(userRepository).deleteById(testUserId);
        
        // Act
        userService.deleteUser(testUserId);
        
        // Assert
        verify(userRepository).existsById(testUserId);
        verify(userRepository).countByManagerId(testUserId);
        verify(userRepository).deleteById(testUserId);
    }
    
    @Test
    @DisplayName("deleteUser - should throw UserNotFoundException when user not found")
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.existsById(testUserId)).thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(testUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(testUserId.toString());
        
        verify(userRepository).existsById(testUserId);
        verify(userRepository, never()).deleteById(any());
    }
    
    @Test
    @DisplayName("deleteUser - should throw UserDeletionException when user has employees")
    void deleteUser_ShouldThrowException_WhenUserHasEmployees() {
        // Arrange
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(userRepository.countByManagerId(testUserId)).thenReturn(3L);
        
        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(testUserId))
                .isInstanceOf(UserDeletionException.class)
                .hasMessageContaining("3 employee(s)");
        
        verify(userRepository).existsById(testUserId);
        verify(userRepository).countByManagerId(testUserId);
        verify(userRepository, never()).deleteById(any());
    }
}
