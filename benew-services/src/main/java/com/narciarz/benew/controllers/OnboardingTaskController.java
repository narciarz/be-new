package com.narciarz.benew.controllers;

import com.narciarz.benew.exceptions.OnboardingProcessNotFoundException;
import com.narciarz.benew.exceptions.OnboardingTaskNotFoundException;
import com.narciarz.benew.models.dto.OnboardingTaskResponseDto;
import com.narciarz.benew.models.dto.UpdateOnboardingTaskRequestDto;
import com.narciarz.benew.services.OnboardingTaskService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for onboarding task management endpoints.
 * 
 * <p>Provides HTTP endpoints for operations on tasks within onboarding processes:</p>
 * <ul>
 *   <li>GET /onboarding/{processId}/tasks - retrieve all tasks for a process</li>
 *   <li>GET /onboarding/{processId}/tasks/{taskId} - retrieve specific task</li>
 *   <li>PUT /onboarding/{processId}/tasks/{taskId} - update task (mark completed)</li>
 * </ul>
 * 
 * <p>All endpoints require authentication (JWT token in Authorization header).
 * Authorization is enforced by Spring Security configuration.</p>
 * 
 * <p>Task updates automatically synchronize denormalized counters in the parent
 * OnboardingProcess for accurate progress tracking.</p>
 */
@RestController
@RequestMapping("/onboarding/{processId}/tasks")
public class OnboardingTaskController {
    
    private static final Logger log = LoggerFactory.getLogger(OnboardingTaskController.class);
    
    private final OnboardingTaskService taskService;
    
    /**
     * Constructor-based dependency injection.
     * 
     * @param taskService service for onboarding task business logic
     */
    public OnboardingTaskController(OnboardingTaskService taskService) {
        this.taskService = taskService;
    }
    
    /**
     * Retrieves all tasks for a specific onboarding process.
     * 
     * <p>Tasks are returned ordered by taskOrder for proper checklist display.
     * No pagination is needed as task lists are typically small (10-50 tasks).</p>
     * 
     * <p>Example request:</p>
     * <pre>
     * GET /onboarding/123e4567-e89b-12d3-a456-426614174000/tasks
     * </pre>
     * 
     * @param processId the onboarding process UUID
     * @return list of onboarding task response DTOs with HTTP 200 OK
     * @throws OnboardingProcessNotFoundException if process doesn't exist (returns HTTP 404)
     */
    @GetMapping
    public ResponseEntity<List<OnboardingTaskResponseDto>> getTasksByProcessId(
            @PathVariable UUID processId) {
        
        log.info("GET /onboarding/{}/tasks", processId);
        
        List<OnboardingTaskResponseDto> tasks = taskService.getTasksByProcessId(processId);
        
        log.debug("Returning {} tasks for process {}", tasks.size(), processId);
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Retrieves a specific task by ID within an onboarding process.
     * 
     * <p>Validates that the task belongs to the specified process for security.</p>
     * 
     * <p>Example request:</p>
     * <pre>
     * GET /onboarding/123e4567-e89b-12d3-a456-426614174000/tasks/987e6543-e21c-34d5-b678-123456789abc
     * </pre>
     * 
     * @param processId the onboarding process UUID
     * @param taskId the task UUID
     * @return onboarding task response DTO with HTTP 200 OK
     * @throws OnboardingTaskNotFoundException if task doesn't exist or doesn't belong to process (returns HTTP 404)
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<OnboardingTaskResponseDto> getTaskById(
            @PathVariable UUID processId,
            @PathVariable UUID taskId) {
        
        log.info("GET /onboarding/{}/tasks/{}", processId, taskId);
        
        OnboardingTaskResponseDto task = taskService.getTaskById(processId, taskId);
        
        log.debug("Returning task {} from process {}", taskId, processId);
        return ResponseEntity.ok(task);
    }
    
    /**
     * Updates a task's completion status.
     * 
     * <p>Primary use case is marking tasks as completed/incomplete. When isCompleted
     * changes, automatically updates parent process counters (completedTasksCount)
     * for accurate progress tracking.</p>
     * 
     * <p>Request body typically contains:</p>
     * <ul>
     *   <li>isCompleted: true/false - mark task as done or not done</li>
     * </ul>
     * 
     * <p>Example request:</p>
     * <pre>
     * PUT /onboarding/123e4567-e89b-12d3-a456-426614174000/tasks/987e6543-e21c-34d5-b678-123456789abc
     * Content-Type: application/json
     * 
     * {
     *   "isCompleted": true
     * }
     * </pre>
     * 
     * @param processId the onboarding process UUID
     * @param taskId the task UUID
     * @param dto update task request DTO
     * @return updated onboarding task response DTO with HTTP 200 OK
     * @throws OnboardingTaskNotFoundException if task doesn't exist or doesn't belong to process (returns HTTP 404)
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<OnboardingTaskResponseDto> updateTask(
            @PathVariable UUID processId,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateOnboardingTaskRequestDto dto) {
        
        log.info("PUT /onboarding/{}/tasks/{} - isCompleted: {}", 
                processId, taskId, dto.getIsCompleted());
        
        OnboardingTaskResponseDto updatedTask = taskService.updateTask(processId, taskId, dto);
        
        log.info("Updated task {} in process {}", taskId, processId);
        return ResponseEntity.ok(updatedTask);
    }
}

