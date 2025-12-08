package com.narciarz.benew.controllers;

import com.narciarz.benew.models.dto.ManagerTaskResponseDto;
import com.narciarz.benew.services.OnboardingTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for manager-specific task operations.
 * 
 * <p>Provides endpoints for managers to view and manage tasks assigned to them
 * across all their team members' onboarding processes.</p>
 * 
 * <p>Key endpoints:</p>
 * <ul>
 *   <li>GET /manager/tasks - retrieve all MANAGER tasks for current manager</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/manager")
public class ManagerTaskController {
    
    private static final Logger log = LoggerFactory.getLogger(ManagerTaskController.class);
    
    private final OnboardingTaskService taskService;
    
    /**
     * Constructor-based dependency injection.
     * 
     * @param taskService service for onboarding task business logic
     */
    public ManagerTaskController(OnboardingTaskService taskService) {
        this.taskService = taskService;
    }
    
    /**
     * Retrieves all tasks assigned to MANAGER role for the authenticated manager's team.
     * 
     * <p>Returns tasks from all active onboarding processes where the current user
     * is the manager. Each task includes employee information for context.</p>
     * 
     * <p>Example request:</p>
     * <pre>
     * GET /manager/tasks
     * Authorization: Bearer {jwt-token}
     * </pre>
     * 
     * <p>Example response:</p>
     * <pre>
     * [
     *   {
     *     "id": "task-uuid",
     *     "processId": "process-uuid",
     *     "title": "Setup laptop and software",
     *     "description": "Provide MacBook with required software",
     *     "taskOrder": 1,
     *     "ownerRole": "MANAGER",
     *     "isCompleted": false,
     *     "userId": "user-uuid",
     *     "userFirstName": "John",
     *     "userLastName": "Doe",
     *     "userPosition": "Software Engineer",
     *     "processStatus": "ACTIVE",
     *     "createdAt": "2024-01-15T10:00:00",
     *     "updatedAt": "2024-01-15T10:00:00"
     *   }
     * ]
     * </pre>
     * 
     * @param authentication Spring Security authentication object containing current user info
     * @return list of manager task response DTOs with HTTP 200 OK
     */
    @GetMapping("/tasks")
    public ResponseEntity<List<ManagerTaskResponseDto>> getManagerTasks(Authentication authentication) {
        UUID managerId = UUID.fromString(authentication.getName());
        
        log.info("GET /manager/tasks for manager: {}", managerId);
        
        List<ManagerTaskResponseDto> tasks = taskService.getManagerTasks(managerId);
        
        log.debug("Returning {} MANAGER tasks", tasks.size());
        return ResponseEntity.ok(tasks);
    }
}
