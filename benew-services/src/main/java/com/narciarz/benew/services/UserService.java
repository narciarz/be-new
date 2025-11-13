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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service layer for user management operations.
 * 
 * <p>Handles business logic, validation, and orchestration between the controller
 * and repository layers. All state-changing operations are transactional.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>CRUD operations for users</li>
 *   <li>Email uniqueness validation</li>
 *   <li>Manager relationship validation</li>
 *   <li>Password hashing with BCrypt</li>
 *   <li>Position name normalization</li>
 *   <li>Deletion constraint enforcement</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class UserService {
    
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Constructor-based dependency injection.
     * 
     * @param userRepository repository for user data access
     * @param userMapper mapper for entity-DTO conversion
     * @param passwordEncoder encoder for password hashing
     */
    public UserService(UserRepository userRepository, 
                      UserMapper userMapper, 
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Retrieves all users with pagination and sorting.
     * 
     * @param pageable pagination parameters (page, size, sort)
     * @return page of user response DTOs
     */
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination: {}", pageable);
        return userRepository.findAll(pageable)
                .map(userMapper::toResponseDto);
    }
    
    /**
     * Retrieves users filtered by role.
     * 
     * @param role the user role to filter by
     * @param pageable pagination parameters
     * @return page of user response DTOs
     */
    public Page<UserResponseDto> getUsersByRole(UserRole role, Pageable pageable) {
        log.debug("Fetching users with role: {} and pagination: {}", role, pageable);
        return userRepository.findByRole(role, pageable)
                .map(userMapper::toResponseDto);
    }
    
    /**
     * Retrieves users managed by a specific manager.
     * 
     * @param managerId the manager's user ID
     * @param pageable pagination parameters
     * @return page of user response DTOs
     */
    public Page<UserResponseDto> getUsersByManager(UUID managerId, Pageable pageable) {
        log.debug("Fetching users managed by: {} with pagination: {}", managerId, pageable);
        return userRepository.findByManagerId(managerId, pageable)
                .map(userMapper::toResponseDto);
    }
    
    /**
     * Retrieves users by position name (case-insensitive, partial match).
     * 
     * @param positionName position name to search for
     * @param pageable pagination parameters
     * @return page of user response DTOs
     */
    public Page<UserResponseDto> getUsersByPosition(String positionName, Pageable pageable) {
        log.debug("Searching users by position: {} with pagination: {}", positionName, pageable);
        return userRepository.findByPositionNameContainingIgnoreCase(positionName, pageable)
                .map(userMapper::toResponseDto);
    }
    
    /**
     * Retrieves users by last name (case-insensitive, partial match).
     * 
     * @param lastName last name to search for
     * @param pageable pagination parameters
     * @return page of user response DTOs
     */
    public Page<UserResponseDto> getUsersByLastName(String lastName, Pageable pageable) {
        log.debug("Searching users by last name: {} with pagination: {}", lastName, pageable);
        return userRepository.findByLastNameContainingIgnoreCase(lastName, pageable)
                .map(userMapper::toResponseDto);
    }
    
    /**
     * Retrieves a specific user by ID.
     * 
     * @param userId the user ID
     * @return user response DTO
     * @throws UserNotFoundException if user doesn't exist
     */
    public UserResponseDto getUserById(UUID userId) {
        log.debug("Fetching user by id: {}", userId);
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return userMapper.toResponseDto(user);
    }
    
    /**
     * Creates a new user with validation and password hashing.
     * 
     * <p>Validation includes:</p>
     * <ul>
     *   <li>Email uniqueness check (case-insensitive)</li>
     *   <li>Manager existence validation if managerId provided</li>
     * </ul>
     * 
     * <p>Processing includes:</p>
     * <ul>
     *   <li>Password hashing with BCrypt</li>
     *   <li>Position name normalization (trim + lowercase)</li>
     *   <li>Manager relationship establishment</li>
     * </ul>
     * 
     * @param dto create user request DTO
     * @return created user response DTO
     * @throws DuplicateEmailException if email already exists
     * @throws InvalidManagerException if manager ID is invalid
     */
    @Transactional
    public UserResponseDto createUser(CreateUserRequestDto dto) {
        log.info("Creating new user with email: {}", dto.getEmail());
        
        // Validate email uniqueness
        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            log.warn("Attempt to create user with duplicate email: {}", dto.getEmail());
            throw new DuplicateEmailException(dto.getEmail());
        }
        
        // Map DTO to entity
        AppUser user = userMapper.toEntity(dto);
        
        // Hash password
        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        user.setPasswordHash(hashedPassword);
        
        // Normalize position name if provided
        if (user.getPositionName() != null && !user.getPositionName().isBlank()) {
            user.setPositionName(normalizePositionName(user.getPositionName()));
        }
        
        // Validate and set manager if managerId provided
        if (dto.getManagerId() != null) {
            AppUser manager = userRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new InvalidManagerException(dto.getManagerId()));
            user.setManager(manager);
            log.debug("Assigned manager {} to new user", dto.getManagerId());
        }
        
        // Save user
        AppUser savedUser = userRepository.save(user);
        log.info("Successfully created user with id: {}", savedUser.getId());
        
        return userMapper.toResponseDto(savedUser);
    }
    
    /**
     * Updates an existing user with partial update support.
     * 
     * <p>Only non-null fields in the DTO are updated. Validation includes:</p>
     * <ul>
     *   <li>Email uniqueness check if email is being changed</li>
     *   <li>Manager existence validation if managerId provided</li>
     * </ul>
     * 
     * @param userId the user ID to update
     * @param dto update user request DTO
     * @return updated user response DTO
     * @throws UserNotFoundException if user doesn't exist
     * @throws DuplicateEmailException if new email already exists
     * @throws InvalidManagerException if manager ID is invalid
     */
    @Transactional
    public UserResponseDto updateUser(UUID userId, UpdateUserRequestDto dto) {
        log.info("Updating user with id: {}", userId);
        
        // Fetch existing user
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        // Validate email uniqueness if email is being changed
        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
                log.warn("Attempt to update user {} with duplicate email: {}", userId, dto.getEmail());
                throw new DuplicateEmailException(dto.getEmail());
            }
        }
        
        // Hash password if provided
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            String hashedPassword = passwordEncoder.encode(dto.getPassword());
            user.setPasswordHash(hashedPassword);
            log.debug("Password updated for user {}", userId);
        }
        
        // Update entity from DTO (partial update)
        userMapper.updateEntityFromDto(dto, user);
        
        // Normalize position name if updated
        if (dto.getPositionName() != null && !dto.getPositionName().isBlank()) {
            user.setPositionName(normalizePositionName(dto.getPositionName()));
        }
        
        // Validate and update manager if managerId provided
        if (dto.getManagerId() != null) {
            AppUser manager = userRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new InvalidManagerException(dto.getManagerId()));
            user.setManager(manager);
            log.debug("Updated manager for user {} to {}", userId, dto.getManagerId());
        }
        
        // Save updated user
        AppUser updatedUser = userRepository.save(user);
        log.info("Successfully updated user with id: {}", userId);
        
        return userMapper.toResponseDto(updatedUser);
    }
    
    /**
     * Deletes a user by ID.
     * 
     * <p>Before deletion, validates that:</p>
     * <ul>
     *   <li>User exists</li>
     *   <li>If user is a manager, no employees are assigned (ON DELETE RESTRICT)</li>
     * </ul>
     * 
     * @param userId the user ID to delete
     * @throws UserNotFoundException if user doesn't exist
     * @throws UserDeletionException if user is a manager with employees
     */
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Attempting to delete user with id: {}", userId);
        
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        
        // Check if user is a manager with employees
        long employeeCount = userRepository.countByManagerId(userId);
        if (employeeCount > 0) {
            log.warn("Cannot delete user {} - still has {} employee(s) assigned", userId, employeeCount);
            throw new UserDeletionException(userId, employeeCount);
        }
        
        try {
            userRepository.deleteById(userId);
            log.info("Successfully deleted user with id: {}", userId);
        } catch (Exception e) {
            log.error("Error deleting user {}: {}", userId, e.getMessage());
            throw new UserDeletionException("Failed to delete user: " + e.getMessage(), e);
        }
    }
    
    /**
     * Normalizes position name by trimming whitespace and converting to lowercase.
     * 
     * <p>This ensures consistent position name matching when assigning onboarding templates.</p>
     * 
     * @param positionName the raw position name
     * @return normalized position name
     */
    private String normalizePositionName(String positionName) {
        return positionName.trim().toLowerCase();
    }
}
