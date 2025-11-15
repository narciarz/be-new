package com.narciarz.benew.controllers;

import com.narciarz.benew.models.dto.CreateTemplateRequestDto;
import com.narciarz.benew.models.dto.UpdateTemplateRequestDto;
import com.narciarz.benew.models.dto.TemplateResponseDto;
import com.narciarz.benew.models.dto.CreateTemplateTaskRequestDto;
import com.narciarz.benew.models.dto.UpdateTemplateTaskRequestDto;
import com.narciarz.benew.models.dto.TemplateTaskResponseDto;
import com.narciarz.benew.services.TemplateService;
import com.narciarz.benew.services.TemplateTaskService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for template management endpoints.
 * 
 * <p>Provides HTTP endpoints for CRUD operations on onboarding checklist templates
 * and their associated tasks. Handles routing and request/response mapping while
 * delegating business logic to {@link TemplateService} and {@link TemplateTaskService}.</p>
 * 
 * <p>All endpoints will be secured with JWT authentication and restricted to ADMIN role
 * (to be implemented).</p>
 * 
 * <p>Base path: {@code /api/templates}</p>
 */
@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    
    private static final Logger log = LoggerFactory.getLogger(TemplateController.class);
    
    private final TemplateService templateService;
    private final TemplateTaskService templateTaskService;
    
    /**
     * Constructor-based dependency injection.
     * 
     * @param templateService service for template business logic
     * @param templateTaskService service for template task business logic
     */
    public TemplateController(TemplateService templateService,
                             TemplateTaskService templateTaskService) {
        this.templateService = templateService;
        this.templateTaskService = templateTaskService;
    }
    
    /**
     * GET /api/templates - Retrieves paginated list of templates with optional filtering.
     * 
     * <p>Supports filtering by:</p>
     * <ul>
     *   <li>positionName - search by position name (partial match, case-insensitive)</li>
     * </ul>
     * 
     * <p>Query parameters:</p>
     * <ul>
     *   <li>page - page number (default: 0)</li>
     *   <li>size - page size (default: 20)</li>
     *   <li>sort - sort criteria (default: positionName,asc)</li>
     * </ul>
     * 
     * @param positionName optional position name search filter
     * @param pageable pagination and sorting parameters
     * @return page of template response DTOs
     */
    @GetMapping
    public ResponseEntity<Page<TemplateResponseDto>> getAllTemplates(
            @RequestParam(required = false) String positionName,
            @PageableDefault(size = 20, sort = "positionName", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.debug("GET /api/templates - positionName: {}, pageable: {}", positionName, pageable);
        
        Page<TemplateResponseDto> templates;
        
        // Apply filter if positionName is provided
        if (positionName != null && !positionName.isBlank()) {
            templates = templateService.getTemplatesByPositionName(positionName, pageable);
        } else {
            templates = templateService.getAllTemplates(pageable);
        }
        
        return ResponseEntity.ok(templates);
    }
    
    /**
     * GET /api/templates/{templateId} - Retrieves a specific template by ID.
     * 
     * @param templateId the template ID
     * @return template response DTO
     * @throws com.narciarz.benew.exceptions.TemplateNotFoundException if template doesn't exist (404)
     */
    @GetMapping("/{templateId}")
    public ResponseEntity<TemplateResponseDto> getTemplateById(@PathVariable UUID templateId) {
        log.debug("GET /api/templates/{}", templateId);
        TemplateResponseDto template = templateService.getTemplateById(templateId);
        return ResponseEntity.ok(template);
    }
    
    /**
     * POST /api/templates - Creates a new template.
     * 
     * <p>Request body must include:</p>
     * <ul>
     *   <li>positionName - unique position name (max 50 characters)</li>
     * </ul>
     * 
     * <p>Position name will be normalized (trim + lowercase) before storage and
     * uniqueness is enforced case-insensitively.</p>
     * 
     * @param dto create template request DTO (validated)
     * @return created template response DTO with HTTP 201 Created and Location header
     * @throws com.narciarz.benew.exceptions.DuplicatePositionNameException if position name exists (400)
     */
    @PostMapping
    public ResponseEntity<TemplateResponseDto> createTemplate(@Valid @RequestBody CreateTemplateRequestDto dto) {
        log.info("POST /api/templates - creating template with position name: {}", dto.getPositionName());
        
        TemplateResponseDto createdTemplate = templateService.createTemplate(dto);
        
        // Build Location header with the URI of the created resource
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTemplate.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(createdTemplate);
    }
    
    /**
     * PUT /api/templates/{templateId} - Updates an existing template.
     * 
     * <p>Supports partial updates - only provided fields will be updated.
     * All fields are optional except the templateId path parameter.</p>
     * 
     * <p>Updatable fields:</p>
     * <ul>
     *   <li>positionName - must be unique if changed (max 50 characters)</li>
     * </ul>
     * 
     * <p>Position name will be normalized (trim + lowercase) before storage if provided.</p>
     * 
     * @param templateId the template ID to update
     * @param dto update template request DTO (validated)
     * @return updated template response DTO
     * @throws com.narciarz.benew.exceptions.TemplateNotFoundException if template doesn't exist (404)
     * @throws com.narciarz.benew.exceptions.DuplicatePositionNameException if new position name exists (400)
     */
    @PutMapping("/{templateId}")
    public ResponseEntity<TemplateResponseDto> updateTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody UpdateTemplateRequestDto dto) {
        log.info("PUT /api/templates/{} - updating template", templateId);
        TemplateResponseDto updatedTemplate = templateService.updateTemplate(templateId, dto);
        return ResponseEntity.ok(updatedTemplate);
    }
    
    /**
     * DELETE /api/templates/{templateId} - Deletes a template.
     * 
     * <p>Deletion is prevented if the template has associated template tasks
     * (database constraint: ON DELETE RESTRICT). All template tasks must be
     * deleted first before the template can be removed.</p>
     * 
     * @param templateId the template ID to delete
     * @return HTTP 204 No Content on success
     * @throws com.narciarz.benew.exceptions.TemplateNotFoundException if template doesn't exist (404)
     * @throws com.narciarz.benew.exceptions.TemplateDeletionException if template has tasks (400)
     */
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID templateId) {
        log.info("DELETE /api/templates/{} - deleting template", templateId);
        templateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }
    
    // ==================== Template Task Endpoints ====================
    
    /**
     * GET /api/templates/{templateId}/tasks - Retrieves all tasks for a template.
     * 
     * <p>Returns tasks ordered by taskOrder field (ascending). This endpoint retrieves
     * the master task list that will be copied when creating new onboarding processes.</p>
     * 
     * @param templateId the template ID
     * @return list of template task response DTOs
     * @throws com.narciarz.benew.exceptions.TemplateNotFoundException if template doesn't exist (404)
     */
    @GetMapping("/{templateId}/tasks")
    public ResponseEntity<List<TemplateTaskResponseDto>> getTasksForTemplate(
            @PathVariable UUID templateId) {
        log.debug("GET /api/templates/{}/tasks - retrieving tasks", templateId);
        List<TemplateTaskResponseDto> tasks = templateTaskService.getAllTasksForTemplate(templateId);
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * POST /api/templates/{templateId}/tasks - Creates a new task for a template.
     * 
     * <p>Request body must include:</p>
     * <ul>
     *   <li>title - task title (required, max 255 characters)</li>
     *   <li>description - task description (optional)</li>
     *   <li>taskOrder - display order in checklist (required)</li>
     *   <li>ownerRole - role responsible for task: MANAGER or USER (required)</li>
     * </ul>
     * 
     * <p>Tasks are copied to onboarding processes when they are created, so changes to
     * template tasks do not affect existing onboarding processes (versioning via denormalization).</p>
     * 
     * @param templateId the template ID to add task to
     * @param dto create template task request DTO (validated)
     * @return created template task response DTO with HTTP 201 Created and Location header
     * @throws com.narciarz.benew.exceptions.TemplateNotFoundException if template doesn't exist (404)
     */
    @PostMapping("/{templateId}/tasks")
    public ResponseEntity<TemplateTaskResponseDto> createTask(
            @PathVariable UUID templateId,
            @Valid @RequestBody CreateTemplateTaskRequestDto dto) {
        log.info("POST /api/templates/{}/tasks - creating task with title: {}", 
                templateId, dto.getTitle());
        
        TemplateTaskResponseDto createdTask = templateTaskService.createTask(templateId, dto);
        
        // Build Location header with the URI of the created resource
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTask.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(createdTask);
    }
    
    /**
     * PUT /api/templates/{templateId}/tasks/{taskId} - Updates an existing template task.
     * 
     * <p>Supports partial updates - only provided fields will be updated.
     * All fields are optional except the templateId and taskId path parameters.</p>
     * 
     * <p>Updatable fields:</p>
     * <ul>
     *   <li>title - task title (max 255 characters)</li>
     *   <li>description - task description</li>
     *   <li>taskOrder - display order in checklist</li>
     *   <li>ownerRole - role responsible for task: MANAGER or USER</li>
     * </ul>
     * 
     * <p>Changes to template tasks do not affect existing onboarding processes, only
     * new processes created after the update will reflect the changes.</p>
     * 
     * @param templateId the template ID
     * @param taskId the task ID to update
     * @param dto update template task request DTO (validated)
     * @return updated template task response DTO
     * @throws com.narciarz.benew.exceptions.TemplateNotFoundException if template doesn't exist (404)
     * @throws com.narciarz.benew.exceptions.TemplateTaskNotFoundException if task doesn't exist or doesn't belong to template (404)
     */
    @PutMapping("/{templateId}/tasks/{taskId}")
    public ResponseEntity<TemplateTaskResponseDto> updateTask(
            @PathVariable UUID templateId,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTemplateTaskRequestDto dto) {
        log.info("PUT /api/templates/{}/tasks/{} - updating task", templateId, taskId);
        TemplateTaskResponseDto updatedTask = templateTaskService.updateTask(templateId, taskId, dto);
        return ResponseEntity.ok(updatedTask);
    }
    
    /**
     * DELETE /api/templates/{templateId}/tasks/{taskId} - Deletes a template task.
     * 
     * <p>Validates that the task belongs to the specified template before deletion.
     * Changes do not affect existing onboarding processes, only new processes created
     * after the deletion will not include this task.</p>
     * 
     * @param templateId the template ID
     * @param taskId the task ID to delete
     * @return HTTP 204 No Content on success
     * @throws com.narciarz.benew.exceptions.TemplateNotFoundException if template doesn't exist (404)
     * @throws com.narciarz.benew.exceptions.TemplateTaskNotFoundException if task doesn't exist or doesn't belong to template (404)
     */
    @DeleteMapping("/{templateId}/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID templateId,
            @PathVariable UUID taskId) {
        log.info("DELETE /api/templates/{}/tasks/{} - deleting task", templateId, taskId);
        templateTaskService.deleteTask(templateId, taskId);
        return ResponseEntity.noContent().build();
    }
}

