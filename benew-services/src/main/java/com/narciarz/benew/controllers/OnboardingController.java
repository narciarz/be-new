package com.narciarz.benew.controllers;

import com.narciarz.benew.models.OnboardingStatus;
import com.narciarz.benew.models.dto.CreateOnboardingProcessRequestDto;
import com.narciarz.benew.models.dto.OnboardingProcessResponseDto;
import com.narciarz.benew.models.dto.UpdateOnboardingProcessRequestDto;
import com.narciarz.benew.services.OnboardingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for onboarding process management endpoints.
 * 
 * <p>Provides HTTP endpoints for CRUD operations on onboarding processes:</p>
 * <ul>
 *   <li>GET /onboarding - retrieve all processes with filtering and pagination</li>
 *   <li>GET /onboarding/{processId} - retrieve specific process</li>
 *   <li>POST /onboarding - create new process (with task copying from template)</li>
 *   <li>PUT /onboarding/{processId} - update process status/counters</li>
 *   <li>DELETE /onboarding/{processId} - archive/delete process</li>
 * </ul>
 * 
 * <p>All endpoints require authentication (JWT token in Authorization header).
 * Authorization is enforced by Spring Security configuration.</p>
 */
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    
    private static final Logger log = LoggerFactory.getLogger(OnboardingController.class);
    
    private final OnboardingService onboardingService;
    
    /**
     * Constructor-based dependency injection.
     * 
     * @param onboardingService service for onboarding business logic
     */
    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }
    
    /**
     * Retrieves all onboarding processes with pagination and optional filtering.
     * 
     * <p>Query Parameters:</p>
     * <ul>
     *   <li>status - filter by OnboardingStatus (ACTIVE, ARCHIVED)</li>
     *   <li>managerId - filter by manager UUID</li>
     *   <li>userId - filter by user UUID</li>
     *   <li>page - page number (default: 0)</li>
     *   <li>size - page size (default: 20)</li>
     *   <li>sort - sort criteria (default: createdAt,desc)</li>
     * </ul>
     * 
     * <p>Example requests:</p>
     * <pre>
     * GET /onboarding?status=ACTIVE&page=0&size=10
     * GET /onboarding?managerId=123e4567-e89b-12d3-a456-426614174000
     * GET /onboarding?userId=123e4567-e89b-12d3-a456-426614174001
     * </pre>
     * 
     * @param status optional status filter
     * @param managerId optional manager filter
     * @param userId optional user filter
     * @param pageable pagination parameters
     * @return page of onboarding process response DTOs with HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<Page<OnboardingProcessResponseDto>> getAllProcesses(
            @RequestParam(required = false) OnboardingStatus status,
            @RequestParam(required = false) UUID managerId,
            @RequestParam(required = false) UUID userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /onboarding - filters: status={}, managerId={}, userId={}, page={}", 
                status, managerId, userId, pageable.getPageNumber());
        
        Page<OnboardingProcessResponseDto> processes = onboardingService.getAllProcesses(
                pageable, status, managerId, userId);
        
        log.debug("Returning {} onboarding processes (page {} of {})", 
                 processes.getNumberOfElements(), 
                 processes.getNumber() + 1, 
                 processes.getTotalPages());
        
        return ResponseEntity.ok(processes);
    }
    
    /**
     * Retrieves a specific onboarding process by ID.
     * 
     * <p>Example request:</p>
     * <pre>
     * GET /onboarding/123e4567-e89b-12d3-a456-426614174000
     * </pre>
     * 
     * @param processId the onboarding process UUID
     * @return onboarding process response DTO with HTTP 200 OK
     * @throws OnboardingProcessNotFoundException if process doesn't exist (returns HTTP 404)
     */
    @GetMapping("/{processId}")
    public ResponseEntity<OnboardingProcessResponseDto> getProcessById(@PathVariable UUID processId) {
        log.info("GET /onboarding/{}", processId);
        
        OnboardingProcessResponseDto process = onboardingService.getProcessById(processId);
        
        log.debug("Returning onboarding process: {}", processId);
        return ResponseEntity.ok(process);
    }
    
    /**
     * Creates a new onboarding process with automatic task copying from template.
     * 
     * <p>Request body must contain:</p>
     * <ul>
     *   <li>userId - UUID of employee being onboarded</li>
     *   <li>managerId - UUID of manager overseeing onboarding</li>
     *   <li>sourceTemplateId - UUID of template to copy tasks from</li>
     * </ul>
     * 
     * <p>Process creation automatically:</p>
     * <ul>
     *   <li>Sets status to ACTIVE</li>
     *   <li>Copies all tasks from source template</li>
     *   <li>Initializes task counters</li>
     * </ul>
     * 
     * <p>Example request:</p>
     * <pre>
     * POST /onboarding
     * Content-Type: application/json
     * 
     * {
     *   "userId": "123e4567-e89b-12d3-a456-426614174000",
     *   "managerId": "123e4567-e89b-12d3-a456-426614174001",
     *   "sourceTemplateId": "123e4567-e89b-12d3-a456-426614174002"
     * }
     * </pre>
     * 
     * @param dto create onboarding process request DTO
     * @return created onboarding process response DTO with HTTP 201 Created
     * @throws UserNotFoundException if user or manager doesn't exist (returns HTTP 404)
     * @throws TemplateNotFoundException if template doesn't exist (returns HTTP 404)
     */
    @PostMapping
    public ResponseEntity<OnboardingProcessResponseDto> createProcess(
            @Valid @RequestBody CreateOnboardingProcessRequestDto dto) {
        
        log.info("POST /onboarding - userId: {}, managerId: {}, templateId: {}", 
                dto.getUserId(), dto.getManagerId(), dto.getSourceTemplateId());
        
        OnboardingProcessResponseDto createdProcess = onboardingService.createProcess(dto);
        
        log.info("Created onboarding process with id: {}", createdProcess.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProcess);
    }
    
    /**
     * Updates an existing onboarding process.
     * 
     * <p>Supports partial updates. Common use cases:</p>
     * <ul>
     *   <li>Archiving process (status: ARCHIVED)</li>
     *   <li>Manual adjustment of task counters (typically automatic)</li>
     * </ul>
     * 
     * <p>Example request (archiving):</p>
     * <pre>
     * PUT /onboarding/123e4567-e89b-12d3-a456-426614174000
     * Content-Type: application/json
     * 
     * {
     *   "status": "ARCHIVED"
     * }
     * </pre>
     * 
     * @param processId the onboarding process UUID
     * @param dto update onboarding process request DTO
     * @return updated onboarding process response DTO with HTTP 200 OK
     * @throws OnboardingProcessNotFoundException if process doesn't exist (returns HTTP 404)
     */
    @PutMapping("/{processId}")
    public ResponseEntity<OnboardingProcessResponseDto> updateProcess(
            @PathVariable UUID processId,
            @Valid @RequestBody UpdateOnboardingProcessRequestDto dto) {
        
        log.info("PUT /onboarding/{} - status: {}", processId, dto.getStatus());
        
        OnboardingProcessResponseDto updatedProcess = onboardingService.updateProcess(processId, dto);
        
        log.info("Updated onboarding process: {}", processId);
        return ResponseEntity.ok(updatedProcess);
    }
    
    /**
     * Deletes (archives) an onboarding process.
     * 
     * <p>Deletion behavior:</p>
     * <ul>
     *   <li>If process is ACTIVE: automatically archives it (soft deletion)</li>
     *   <li>If process is ARCHIVED: performs hard deletion if no tasks remain</li>
     * </ul>
     * 
     * <p>Example request:</p>
     * <pre>
     * DELETE /onboarding/123e4567-e89b-12d3-a456-426614174000
     * </pre>
     * 
     * @param processId the onboarding process UUID
     * @return HTTP 204 No Content on successful deletion
     * @throws OnboardingProcessNotFoundException if process doesn't exist (returns HTTP 404)
     * @throws OnboardingProcessDeletionException if process has tasks (returns HTTP 400)
     */
    @DeleteMapping("/{processId}")
    public ResponseEntity<Void> deleteProcess(@PathVariable UUID processId) {
        log.info("DELETE /onboarding/{}", processId);
        
        onboardingService.deleteProcess(processId);
        
        log.info("Deleted/archived onboarding process: {}", processId);
        return ResponseEntity.noContent().build();
    }
}

