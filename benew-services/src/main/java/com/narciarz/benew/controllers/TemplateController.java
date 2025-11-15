package com.narciarz.benew.controllers;

import com.narciarz.benew.models.dto.CreateTemplateRequestDto;
import com.narciarz.benew.models.dto.UpdateTemplateRequestDto;
import com.narciarz.benew.models.dto.TemplateResponseDto;
import com.narciarz.benew.services.TemplateService;
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
import java.util.UUID;

/**
 * REST controller for template management endpoints.
 * 
 * <p>Provides HTTP endpoints for CRUD operations on onboarding checklist templates.
 * Handles routing and request/response mapping while delegating business logic to
 * {@link TemplateService}.</p>
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
    
    /**
     * Constructor-based dependency injection.
     * 
     * @param templateService service for template business logic
     */
    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
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
}

