package com.narciarz.benew.services;

import com.narciarz.benew.exceptions.DuplicateEmailException;
import com.narciarz.benew.exceptions.InvalidManagerException;
import com.narciarz.benew.exceptions.UserDeletionException;
import com.narciarz.benew.exceptions.UserNotFoundException;
import com.narciarz.benew.models.AppUser;
import com.narciarz.benew.models.Template;
import com.narciarz.benew.models.UserRole;
import com.narciarz.benew.models.dto.CreateOnboardingProcessRequestDto;
import com.narciarz.benew.models.dto.CreateUserRequestDto;
import com.narciarz.benew.models.dto.UpdateUserRequestDto;
import com.narciarz.benew.models.dto.UserResponseDto;
import com.narciarz.benew.repositories.TemplateRepository;
import com.narciarz.benew.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private final TemplateRepository templateRepository;
    private final OnboardingService onboardingService;
    
    /**
     * Constructor-based dependency injection.
     * 
     * @param userRepository repository for user data access
     * @param userMapper mapper for entity-DTO conversion
     * @param passwordEncoder encoder for password hashing
     * @param templateRepository repository for template data access
     * @param onboardingService service for creating onboarding processes
     */
    public UserService(UserRepository userRepository, 
                      UserMapper userMapper,
                      PasswordEncoder passwordEncoder,
                      TemplateRepository templateRepository,
                      OnboardingService onboardingService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.templateRepository = templateRepository;
        this.onboardingService = onboardingService;
    }
    
    /**
     * Retrieves all users with pagination and sorting.
     * 
     * <p>For MANAGER role: Returns only USER role users reporting to the current manager.</p>
     * <p>For ADMIN role: Returns all users in the system.</p>
     * 
     * @param pageable pagination parameters (page, size, sort)
     * @return page of user response DTOs
     */
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination: {}", pageable);
        
        // Get current user role and ID
        UUID currentUserId = getCurrentUserId();
        UserRole currentUserRole = getCurrentUserRole();
        
        // MANAGER can only see USER role members of their team
        if (currentUserRole == UserRole.MANAGER) {
            log.debug("Manager {} fetching their USER role team members", currentUserId);
            return userRepository.findByManagerIdAndRole(currentUserId, UserRole.USER, pageable)
                    .map(userMapper::toResponseDto);
        }
        
        // ADMIN can see all users
        return userRepository.findAll(pageable)
                .map(userMapper::toResponseDto);
    }
    
    /**
     * Retrieves users filtered by role.
     * 
     * <p>For MANAGER role: Can only query USER role from their team. Other roles return empty.</p>
     * <p>For ADMIN role: Returns all users matching the role filter.</p>
     * 
     * @param role the user role to filter by
     * @param pageable pagination parameters
     * @return page of user response DTOs
     */
    public Page<UserResponseDto> getUsersByRole(UserRole role, Pageable pageable) {
        log.debug("Fetching users with role: {} and pagination: {}", role, pageable);
        
        UUID currentUserId = getCurrentUserId();
        UserRole currentUserRole = getCurrentUserRole();
        
        // MANAGER can only see USER role from their team
        if (currentUserRole == UserRole.MANAGER) {
            if (role != UserRole.USER) {
                log.warn("Manager {} attempted to query role: {}", currentUserId, role);
                return Page.empty(pageable);
            }
            log.debug("Manager {} fetching USER role team members", currentUserId);
            return userRepository.findByManagerIdAndRole(currentUserId, UserRole.USER, pageable)
                    .map(userMapper::toResponseDto);
        }
        
        return userRepository.findByRole(role, pageable)
                .map(userMapper::toResponseDto);
    }
    
    /**
     * Retrieves users managed by a specific manager.
     * 
     * <p>For MANAGER role: Can only query their own USER role team members.</p>
     * <p>For ADMIN role: Can query any manager's team.</p>
     * 
     * @param managerId the manager's user ID
     * @param pageable pagination parameters
     * @return page of user response DTOs
     */
    public Page<UserResponseDto> getUsersByManager(UUID managerId, Pageable pageable) {
        log.debug("Fetching users managed by: {} with pagination: {}", managerId, pageable);
        
        UUID currentUserId = getCurrentUserId();
        UserRole currentUserRole = getCurrentUserRole();
        
        // MANAGER can only query their own team
        if (currentUserRole == UserRole.MANAGER) {
            if (!managerId.equals(currentUserId)) {
                log.warn("Manager {} attempted to query team of another manager {}", currentUserId, managerId);
                return Page.empty(pageable);
            }
            // Return only USER role members
            return userRepository.findByManagerIdAndRole(managerId, UserRole.USER, pageable)
                    .map(userMapper::toResponseDto);
        }
        
        return userRepository.findByManagerId(managerId, pageable)
                .map(userMapper::toResponseDto);
    }
    
    /**
     * Retrieves users by position name (case-insensitive, partial match).
     * 
     * <p>For MANAGER role: Returns only USER role users from their team matching the position.</p>
     * <p>For ADMIN role: Returns all users matching the position.</p>
     * 
     * @param positionName position name to search for
     * @param pageable pagination parameters
     * @return page of user response DTOs
     */
    public Page<UserResponseDto> getUsersByPosition(String positionName, Pageable pageable) {
        log.debug("Searching users by position: {} with pagination: {}", positionName, pageable);
        
        UUID currentUserId = getCurrentUserId();
        UserRole currentUserRole = getCurrentUserRole();
        
        // MANAGER can only search USER role within their team
        if (currentUserRole == UserRole.MANAGER) {
            log.debug("Manager {} searching USER role team members by position: {}", currentUserId, positionName);
            return userRepository.findByManagerIdAndRoleAndPositionNameContainingIgnoreCase(
                    currentUserId, UserRole.USER, positionName, pageable)
                    .map(userMapper::toResponseDto);
        }
        
        return userRepository.findByPositionNameContainingIgnoreCase(positionName, pageable)
                .map(userMapper::toResponseDto);
    }
    
    /**
     * Retrieves users by last name (case-insensitive, partial match).
     * 
     * <p>For MANAGER role: Returns only USER role users from their team matching the last name.</p>
     * <p>For ADMIN role: Returns all users matching the last name.</p>
     * 
     * @param lastName last name to search for
     * @param pageable pagination parameters
     * @return page of user response DTOs
     */
    public Page<UserResponseDto> getUsersByLastName(String lastName, Pageable pageable) {
        log.debug("Searching users by last name: {} with pagination: {}", lastName, pageable);
        
        UUID currentUserId = getCurrentUserId();
        UserRole currentUserRole = getCurrentUserRole();
        
        // MANAGER can only search USER role within their team
        if (currentUserRole == UserRole.MANAGER) {
            log.debug("Manager {} searching USER role team members by last name: {}", currentUserId, lastName);
            return userRepository.findByManagerIdAndRoleAndLastNameContainingIgnoreCase(
                    currentUserId, UserRole.USER, lastName, pageable)
                    .map(userMapper::toResponseDto);
        }
        
        return userRepository.findByLastNameContainingIgnoreCase(lastName, pageable)
                .map(userMapper::toResponseDto);
    }
    
    /**
     * Retrieves a specific user by ID.
     * 
     * <p>For MANAGER role: Can only retrieve USER role users from their team.</p>
     * <p>For ADMIN role: Can retrieve any user.</p>
     * 
     * @param userId the user ID
     * @return user response DTO
     * @throws UserNotFoundException if user doesn't exist or not accessible
     */
    public UserResponseDto getUserById(UUID userId) {
        log.debug("Fetching user by id: {}", userId);
        
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        // MANAGER can only view USER role team members
        UUID currentUserId = getCurrentUserId();
        UserRole currentUserRole = getCurrentUserRole();
        
        if (currentUserRole == UserRole.MANAGER) {
            // Check if user is USER role and assigned to current manager
            if (user.getRole() != UserRole.USER || 
                user.getManager() == null || 
                !user.getManager().getId().equals(currentUserId)) {
                log.warn("Manager {} attempted to access user {} (role: {}) who is not a USER in their team", 
                        currentUserId, userId, user.getRole());
                throw new UserNotFoundException(userId);
            }
        }
        
        return userMapper.toResponseDto(user);
    }
    
    /**
     * Creates a new user with validation and password hashing.
     * 
     * <p>Role-based restrictions:</p>
     * <ul>
     *   <li>MANAGER: Can only create users with USER role, automatically assigned to themselves as manager</li>
     *   <li>ADMIN: Can create users with any role and specify any manager</li>
     * </ul>
     * 
     * <p>Validation includes:</p>
     * <ul>
     *   <li>Email uniqueness check (case-insensitive)</li>
     *   <li>Manager existence validation if managerId provided</li>
     *   <li>Role restriction for MANAGER users</li>
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
     * @throws IllegalArgumentException if MANAGER attempts to create non-USER role
     */
    @Transactional
    public UserResponseDto createUser(CreateUserRequestDto dto) {
        log.info("Creating new user with email: {}", dto.getEmail());
        
        UUID currentUserId = getCurrentUserId();
        UserRole currentUserRole = getCurrentUserRole();
        
        // MANAGER can only create users with USER role
        if (currentUserRole == UserRole.MANAGER && dto.getRole() != UserRole.USER) {
            log.warn("Manager {} attempted to create user with role: {}", currentUserId, dto.getRole());
            throw new IllegalArgumentException("Managers can only create users with USER role");
        }
        
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
        
        // Set manager based on current user role
        if (currentUserRole == UserRole.MANAGER) {
            // MANAGER creates users assigned to themselves, ignore managerId from DTO
            AppUser manager = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new IllegalStateException("Current user not found"));
            user.setManager(manager);
            log.debug("Automatically assigned manager {} to new user created by MANAGER", currentUserId);
        } else {
            // ADMIN can specify manager
            if (dto.getManagerId() != null) {
                AppUser manager = userRepository.findById(dto.getManagerId())
                        .orElseThrow(() -> new InvalidManagerException(dto.getManagerId()));
                user.setManager(manager);
                log.debug("Assigned manager {} to new user", dto.getManagerId());
            }
        }
        
        // Save user
        AppUser savedUser = userRepository.save(user);
        log.info("Successfully created user with id: {}", savedUser.getId());
        
        // Auto-create onboarding process for USER role (US-006)
        if (savedUser.getRole() == UserRole.USER && savedUser.getPositionName() != null) {
            createOnboardingProcessForUser(savedUser);
        }
        
        return userMapper.toResponseDto(savedUser);
    }
    
    /**
     * Automatically creates an onboarding process for a new USER.
     * 
     * <p>Implements US-006: "System automatically generates checklist for employee."
     * Finds template matching user's position and creates onboarding process with tasks.</p>
     * 
     * @param user the newly created user
     */
    private void createOnboardingProcessForUser(AppUser user) {
        String normalizedPosition = normalizePositionName(user.getPositionName());
        
        // Find template for user's position
        Template template = templateRepository.findByPositionNameIgnoreCase(normalizedPosition).orElse(null);
        
        if (template == null) {
            log.warn("No template found for position: {} - skipping onboarding process creation for user {}", 
                    normalizedPosition, user.getId());
            return;
        }
        
        // Ensure user has a manager
        if (user.getManager() == null) {
            log.warn("User {} has no manager assigned - skipping onboarding process creation", user.getId());
            return;
        }
        
        // Create onboarding process
        try {
            CreateOnboardingProcessRequestDto processDto = new CreateOnboardingProcessRequestDto(
                    user.getId(),
                    user.getManager().getId(),
                    template.getId()
            );
            
            onboardingService.createProcess(processDto);
            log.info("Auto-created onboarding process for user {} using template {}", 
                    user.getId(), template.getId());
        } catch (Exception e) {
            log.error("Failed to auto-create onboarding process for user {}: {}", 
                    user.getId(), e.getMessage(), e);
            // Don't fail user creation if onboarding process creation fails
        }
    }
    
    /**
     * Updates an existing user with partial update support.
     * 
     * <p>Role-based restrictions:</p>
     * <ul>
     *   <li>MANAGER: Can only update USER role users in their team, cannot change manager or role</li>
     *   <li>ADMIN: Can update any user and all fields</li>
     * </ul>
     * 
     * <p>Only non-null fields in the DTO are updated. Validation includes:</p>
     * <ul>
     *   <li>Email uniqueness check if email is being changed</li>
     *   <li>Manager existence validation if managerId provided</li>
     *   <li>Ownership validation for MANAGER role</li>
     * </ul>
     * 
     * @param userId the user ID to update
     * @param dto update user request DTO
     * @return updated user response DTO
     * @throws UserNotFoundException if user doesn't exist or not accessible
     * @throws DuplicateEmailException if new email already exists
     * @throws InvalidManagerException if manager ID is invalid
     * @throws IllegalArgumentException if MANAGER attempts unauthorized update
     */
    @Transactional
    public UserResponseDto updateUser(UUID userId, UpdateUserRequestDto dto) {
        log.info("Updating user with id: {}", userId);
        
        UUID currentUserId = getCurrentUserId();
        UserRole currentUserRole = getCurrentUserRole();
        
        // Fetch existing user
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        // MANAGER can only update USER role team members
        if (currentUserRole == UserRole.MANAGER) {
            // Check if user is USER role and assigned to current manager
            if (user.getRole() != UserRole.USER || 
                user.getManager() == null || 
                !user.getManager().getId().equals(currentUserId)) {
                log.warn("Manager {} attempted to update user {} (role: {}) who is not a USER in their team", 
                        currentUserId, userId, user.getRole());
                throw new UserNotFoundException(userId);
            }
            
            // MANAGER cannot change role
            if (dto.getRole() != null && dto.getRole() != user.getRole()) {
                log.warn("Manager {} attempted to change role of user {}", currentUserId, userId);
                throw new IllegalArgumentException("Managers cannot change user roles");
            }
            
            // MANAGER cannot change manager
            if (dto.getManagerId() != null && !dto.getManagerId().equals(user.getManager().getId())) {
                log.warn("Manager {} attempted to change manager of user {}", currentUserId, userId);
                throw new IllegalArgumentException("Managers cannot reassign users to other managers");
            }
        }
        
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
        
        // Validate and update manager if managerId provided (ADMIN only)
        if (currentUserRole == UserRole.ADMIN && dto.getManagerId() != null) {
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
     * <p>Role-based restrictions:</p>
     * <ul>
     *   <li>MANAGER: Can only delete USER role users from their team</li>
     *   <li>ADMIN: Can delete any user</li>
     * </ul>
     * 
     * <p>Before deletion, validates that:</p>
     * <ul>
     *   <li>User exists and is accessible</li>
     *   <li>If user is a manager, no employees are assigned (ON DELETE RESTRICT)</li>
     * </ul>
     * 
     * @param userId the user ID to delete
     * @throws UserNotFoundException if user doesn't exist or not accessible
     * @throws UserDeletionException if user is a manager with employees
     */
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Attempting to delete user with id: {}", userId);
        
        UUID currentUserId = getCurrentUserId();
        UserRole currentUserRole = getCurrentUserRole();
        
        // Fetch user to verify existence and ownership
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        // MANAGER can only delete USER role team members
        if (currentUserRole == UserRole.MANAGER) {
            // Check if user is USER role and assigned to current manager
            if (user.getRole() != UserRole.USER || 
                user.getManager() == null || 
                !user.getManager().getId().equals(currentUserId)) {
                log.warn("Manager {} attempted to delete user {} (role: {}) who is not a USER in their team", 
                        currentUserId, userId, user.getRole());
                throw new UserNotFoundException(userId);
            }
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
    
    /**
     * Extracts the current authenticated user's ID from the JWT token.
     * 
     * <p>Retrieves the user ID from the JWT subject claim in the Spring Security context.</p>
     * 
     * @return current user's ID
     * @throws IllegalStateException if no authentication is present or token is invalid
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String subject = jwt.getSubject();
            return UUID.fromString(subject);
        }
        
        throw new IllegalStateException("Invalid authentication principal type");
    }
    
    /**
     * Extracts the current authenticated user's role from the JWT token.
     * 
     * <p>Retrieves the user role from the JWT role claim in the Spring Security context.</p>
     * 
     * @return current user's role
     * @throws IllegalStateException if no authentication is present or token is invalid
     */
    private UserRole getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String role = jwt.getClaim("role");
            return UserRole.valueOf(role);
        }
        
        throw new IllegalStateException("Invalid authentication principal type");
    }
}
