package com.narciarz.benew.services;

import com.narciarz.benew.exceptions.DuplicatePositionNameException;
import com.narciarz.benew.exceptions.TemplateDeletionException;
import com.narciarz.benew.exceptions.TemplateNotFoundException;
import com.narciarz.benew.models.Template;
import com.narciarz.benew.models.dto.CreateTemplateRequestDto;
import com.narciarz.benew.models.dto.UpdateTemplateRequestDto;
import com.narciarz.benew.models.dto.TemplateResponseDto;
import com.narciarz.benew.repositories.TemplateRepository;
import com.narciarz.benew.repositories.TemplateTaskRepository;
import com.narciarz.benew.services.mappers.TemplateMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service layer for template management operations.
 * 
 * <p>Handles business logic, validation, and orchestration between the controller
 * and repository layers. All state-changing operations are transactional.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>CRUD operations for templates</li>
 *   <li>Position name uniqueness validation</li>
 *   <li>Position name normalization (trim + lowercase)</li>
 *   <li>Deletion constraint enforcement (templates with tasks cannot be deleted)</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class TemplateService {
    
    private static final Logger log = LoggerFactory.getLogger(TemplateService.class);
    
    private final TemplateRepository templateRepository;
    private final TemplateMapper templateMapper;
    private final TemplateTaskRepository templateTaskRepository;
    
    /**
     * Constructor-based dependency injection.
     * 
     * @param templateRepository repository for template data access
     * @param templateMapper mapper for entity-DTO conversion
     * @param templateTaskRepository repository for checking template tasks
     */
    public TemplateService(TemplateRepository templateRepository,
                          TemplateMapper templateMapper,
                          TemplateTaskRepository templateTaskRepository) {
        this.templateRepository = templateRepository;
        this.templateMapper = templateMapper;
        this.templateTaskRepository = templateTaskRepository;
    }
    
    /**
     * Retrieves all templates with pagination and sorting.
     * 
     * @param pageable pagination parameters (page, size, sort)
     * @return page of template response DTOs
     */
    public Page<TemplateResponseDto> getAllTemplates(Pageable pageable) {
        log.debug("Fetching all templates with pagination: {}", pageable);
        return templateRepository.findAll(pageable)
                .map(templateMapper::toResponseDto);
    }
    
    /**
     * Retrieves templates by position name (case-insensitive, partial match).
     * 
     * @param positionName position name to search for
     * @param pageable pagination parameters
     * @return page of template response DTOs
     */
    public Page<TemplateResponseDto> getTemplatesByPositionName(String positionName, Pageable pageable) {
        log.debug("Searching templates by position: {} with pagination: {}", positionName, pageable);
        return templateRepository.findByPositionNameContainingIgnoreCase(positionName, pageable)
                .map(templateMapper::toResponseDto);
    }
    
    /**
     * Retrieves a specific template by ID.
     * 
     * @param templateId the template ID
     * @return template response DTO
     * @throws TemplateNotFoundException if template doesn't exist
     */
    public TemplateResponseDto getTemplateById(UUID templateId) {
        log.debug("Fetching template by id: {}", templateId);
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException(templateId));
        return templateMapper.toResponseDto(template);
    }
    
    /**
     * Creates a new template with validation and position name normalization.
     * 
     * <p>Validation includes:</p>
     * <ul>
     *   <li>Position name uniqueness check (case-insensitive)</li>
     * </ul>
     * 
     * <p>Processing includes:</p>
     * <ul>
     *   <li>Position name normalization (trim + lowercase)</li>
     * </ul>
     * 
     * @param dto create template request DTO
     * @return created template response DTO
     * @throws DuplicatePositionNameException if position name already exists
     */
    @Transactional
    public TemplateResponseDto createTemplate(CreateTemplateRequestDto dto) {
        log.info("Creating new template with position name: {}", dto.getPositionName());
        
        // Normalize position name
        String normalizedName = normalizePositionName(dto.getPositionName());
        
        // Validate position name uniqueness
        if (templateRepository.existsByPositionNameIgnoreCase(normalizedName)) {
            log.warn("Attempt to create template with duplicate position name: {}", normalizedName);
            throw new DuplicatePositionNameException(normalizedName);
        }
        
        // Map DTO to entity
        Template template = templateMapper.toEntity(dto);
        
        // Set normalized position name
        template.setPositionName(normalizedName);
        
        // Save template
        Template savedTemplate = templateRepository.save(template);
        log.info("Successfully created template with id: {}", savedTemplate.getId());
        
        return templateMapper.toResponseDto(savedTemplate);
    }
    
    /**
     * Updates an existing template with partial update support.
     * 
     * <p>Only non-null fields in the DTO are updated. Validation includes:</p>
     * <ul>
     *   <li>Position name uniqueness check if position name is being changed</li>
     * </ul>
     * 
     * @param templateId the template ID to update
     * @param dto update template request DTO
     * @return updated template response DTO
     * @throws TemplateNotFoundException if template doesn't exist
     * @throws DuplicatePositionNameException if new position name already exists
     */
    @Transactional
    public TemplateResponseDto updateTemplate(UUID templateId, UpdateTemplateRequestDto dto) {
        log.info("Updating template with id: {}", templateId);
        
        // Fetch existing template
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException(templateId));
        
        // Validate position name uniqueness if position name is being changed
        if (dto.getPositionName() != null && !dto.getPositionName().isBlank()) {
            String normalizedNewName = normalizePositionName(dto.getPositionName());
            String currentNormalizedName = normalizePositionName(template.getPositionName());
            
            // Only check uniqueness if the position name is actually changing
            if (!normalizedNewName.equals(currentNormalizedName)) {
                if (templateRepository.existsByPositionNameIgnoreCase(normalizedNewName)) {
                    log.warn("Attempt to update template {} with duplicate position name: {}", 
                            templateId, normalizedNewName);
                    throw new DuplicatePositionNameException(normalizedNewName);
                }
                // Set normalized position name
                template.setPositionName(normalizedNewName);
                log.debug("Position name updated for template {}", templateId);
            }
        }
        
        // Update entity from DTO (partial update)
        templateMapper.updateEntityFromDto(dto, template);
        
        // Save updated template (JPA dirty checking)
        Template updatedTemplate = templateRepository.save(template);
        log.info("Successfully updated template with id: {}", templateId);
        
        return templateMapper.toResponseDto(updatedTemplate);
    }
    
    /**
     * Deletes a template by ID.
     * 
     * <p>Before deletion, validates that:</p>
     * <ul>
     *   <li>Template exists</li>
     *   <li>Template has no associated tasks (ON DELETE RESTRICT constraint)</li>
     * </ul>
     * 
     * @param templateId the template ID to delete
     * @throws TemplateNotFoundException if template doesn't exist
     * @throws TemplateDeletionException if template has associated tasks
     */
    @Transactional
    public void deleteTemplate(UUID templateId) {
        log.info("Attempting to delete template with id: {}", templateId);
        
        // Verify template exists
        if (!templateRepository.existsById(templateId)) {
            throw new TemplateNotFoundException(templateId);
        }
        
        // Check if template has associated tasks
        long taskCount = templateTaskRepository.countByTemplateId(templateId);
        if (taskCount > 0) {
            log.warn("Cannot delete template {} - still has {} task(s) associated", templateId, taskCount);
            throw new TemplateDeletionException(templateId, taskCount);
        }
        
        try {
            templateRepository.deleteById(templateId);
            log.info("Successfully deleted template with id: {}", templateId);
        } catch (Exception e) {
            log.error("Error deleting template {}: {}", templateId, e.getMessage());
            throw new TemplateDeletionException("Failed to delete template: " + e.getMessage(), e);
        }
    }
    
    /**
     * Normalizes position name by trimming whitespace and converting to lowercase.
     * 
     * <p>This ensures consistent position name matching and enforces uniqueness
     * constraints correctly.</p>
     * 
     * @param positionName the raw position name
     * @return normalized position name
     */
    private String normalizePositionName(String positionName) {
        return positionName.trim().toLowerCase();
    }
}

