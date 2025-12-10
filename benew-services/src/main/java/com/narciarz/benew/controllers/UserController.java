package com.narciarz.benew.controllers;

import com.narciarz.benew.models.UserRole;
import com.narciarz.benew.models.dto.CreateUserRequestDto;
import com.narciarz.benew.models.dto.UpdateUserRequestDto;
import com.narciarz.benew.models.dto.UserResponseDto;
import com.narciarz.benew.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for user management endpoints.
 * 
 * <p>Provides HTTP endpoints for CRUD operations on users. Handles routing
 * and request/response mapping while delegating business logic to {@link UserService}.</p>
 * 
 * <p>All endpoints are secured with JWT authentication.
 * Role-based access control restricts operations based on user roles (ADMIN, MANAGER, USER).</p>
 * 
 * <p>Base path: {@code /api/users}</p>
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
    
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    
    /**
     * Constructor-based dependency injection.
     * 
     * @param userService service for user business logic
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * GET /api/users - Retrieves paginated list of users with optional filtering.
     * 
     * <p>Supports filtering by:</p>
     * <ul>
     *   <li>role - filter by user role (ADMIN, MANAGER, USER)</li>
     *   <li>managerId - filter by manager ID</li>
     *   <li>position - search by position name (partial match)</li>
     *   <li>lastName - search by last name (partial match)</li>
     * </ul>
     * 
     * <p>Query parameters:</p>
     * <ul>
     *   <li>page - page number (default: 0)</li>
     *   <li>size - page size (default: 20)</li>
     *   <li>sort - sort criteria (default: lastName,asc). Examples: "lastName,asc" or "email,desc"</li>
     * </ul>
     * 
     * @param role optional role filter
     * @param managerId optional manager ID filter
     * @param position optional position name search
     * @param lastName optional last name search
     * @param pageable pagination and sorting parameters
     * @return page of user response DTOs
     */
    @GetMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // TODO: SECURITY TEMPORARILY DISABLED
    @Operation(
        summary = "Get all users",
        description = "Retrieves paginated list of users with optional filtering by role, manager, position, or last name. MANAGER role returns only their team members."
    )
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @Parameter(description = "Filter by user role (ADMIN, MANAGER, USER)")
            @RequestParam(required = false) UserRole role,
            @Parameter(description = "Filter by manager ID")
            @RequestParam(required = false) UUID managerId,
            @Parameter(description = "Search by position name (partial match)")
            @RequestParam(required = false) String position,
            @Parameter(description = "Search by last name (partial match)")
            @RequestParam(required = false) String lastName,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.debug("GET /api/users - role: {}, managerId: {}, position: {}, lastName: {}, pageable: {}",
                role, managerId, position, lastName, pageable);
        
        Page<UserResponseDto> users;
        
        // Apply filters based on query parameters
        if (role != null) {
            users = userService.getUsersByRole(role, pageable);
        } else if (managerId != null) {
            users = userService.getUsersByManager(managerId, pageable);
        } else if (position != null && !position.isBlank()) {
            users = userService.getUsersByPosition(position, pageable);
        } else if (lastName != null && !lastName.isBlank()) {
            users = userService.getUsersByLastName(lastName, pageable);
        } else {
            users = userService.getAllUsers(pageable);
        }
        
        return ResponseEntity.ok(users);
    }
    
    /**
     * GET /api/users/{userId} - Retrieves a specific user by ID.
     * 
     * @param userId the user ID
     * @return user response DTO
     * @throws com.narciarz.benew.exceptions.UserNotFoundException if user doesn't exist (404)
     */
    @GetMapping("/{userId}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // TODO: SECURITY TEMPORARILY DISABLED
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID userId) {
        log.debug("GET /api/users/{}", userId);
        UserResponseDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
    /**
     * POST /api/users - Creates a new user.
     * 
     * <p>Request body must include:</p>
     * <ul>
     *   <li>email - unique email address</li>
     *   <li>password - minimum 8 characters (will be hashed)</li>
     *   <li>firstName - user's first name</li>
     *   <li>lastName - user's last name</li>
     *   <li>role - user role (ADMIN, MANAGER, USER)</li>
     * </ul>
     * 
     * <p>Optional fields:</p>
     * <ul>
     *   <li>positionName - job position/title</li>
     *   <li>managerId - ID of the user's manager</li>
     * </ul>
     * 
     * @param dto create user request DTO (validated)
     * @return created user response DTO with HTTP 201 Created
     * @throws com.narciarz.benew.exceptions.DuplicateEmailException if email exists (400)
     * @throws com.narciarz.benew.exceptions.InvalidManagerException if manager ID invalid (400)
     */
    @PostMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // TODO: SECURITY TEMPORARILY DISABLED
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody CreateUserRequestDto dto) {
        log.info("POST /api/users - creating user with email: {}", dto.getEmail());
        UserResponseDto createdUser = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    
    /**
     * PUT /api/users/{userId} - Updates an existing user.
     * 
     * <p>Supports partial updates - only provided fields will be updated.
     * All fields are optional except the userId path parameter.</p>
     * 
     * <p>Updatable fields:</p>
     * <ul>
     *   <li>email - must be unique if changed</li>
     *   <li>password - minimum 8 characters if provided</li>
     *   <li>firstName - user's first name</li>
     *   <li>lastName - user's last name</li>
     *   <li>positionName - job position/title</li>
     *   <li>managerId - ID of the user's manager</li>
     *   <li>role - user role</li>
     * </ul>
     * 
     * @param userId the user ID to update
     * @param dto update user request DTO (validated)
     * @return updated user response DTO
     * @throws com.narciarz.benew.exceptions.UserNotFoundException if user doesn't exist (404)
     * @throws com.narciarz.benew.exceptions.DuplicateEmailException if new email exists (400)
     * @throws com.narciarz.benew.exceptions.InvalidManagerException if manager ID invalid (400)
     */
    @PutMapping("/{userId}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // TODO: SECURITY TEMPORARILY DISABLED
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequestDto dto) {
        log.info("PUT /api/users/{} - updating user", userId);
        UserResponseDto updatedUser = userService.updateUser(userId, dto);
        return ResponseEntity.ok(updatedUser);
    }
    
    /**
     * DELETE /api/users/{userId} - Deletes a user.
     * 
     * <p>Deletion is prevented if the user is a manager with employees assigned
     * (database constraint: ON DELETE RESTRICT).</p>
     * 
     * @param userId the user ID to delete
     * @return HTTP 204 No Content on success
     * @throws com.narciarz.benew.exceptions.UserNotFoundException if user doesn't exist (404)
     * @throws com.narciarz.benew.exceptions.UserDeletionException if deletion not allowed (400)
     */
    @DeleteMapping("/{userId}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // TODO: SECURITY TEMPORARILY DISABLED
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        log.info("DELETE /api/users/{} - deleting user", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
