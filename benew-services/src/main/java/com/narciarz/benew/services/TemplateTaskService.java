package com.narciarz.benew.services;

import com.narciarz.benew.exceptions.TemplateNotFoundException;
import com.narciarz.benew.exceptions.TemplateTaskNotFoundException;
import com.narciarz.benew.models.Template;
import com.narciarz.benew.models.TemplateTask;
import com.narciarz.benew.models.dto.CreateTemplateTaskRequestDto;
import com.narciarz.benew.models.dto.UpdateTemplateTaskRequestDto;
import com.narciarz.benew.models.dto.TemplateTaskResponseDto;
import com.narciarz.benew.repositories.TemplateRepository;
import com.narciarz.benew.repositories.TemplateTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for template task management operations.
 * 
 * <p>Handles business logic, validation, and orchestration between the controller
 * and repository layers. All state-changing operations are transactional.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>CRUD operations for template tasks</li>
 *   <li>Validation that tasks belong to specified template</li>
 *   <li>Ensuring parent template exists before creating tasks</li>
 *   <li>Maintaining referential integrity between templates and tasks</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class TemplateTaskService {
    
    private static final Logger log = LoggerFactory.getLogger(TemplateTaskService.class);
    
    private final TemplateTaskRepository templateTaskRepository;
    private final TemplateRepository templateRepository;
    private final TemplateTaskMapper templateTaskMapper;
    
    /**
     * Constructor-based dependency injection.
     * 
     * @param templateTaskRepository repository for template task data access
     * @param templateRepository repository for template data access
     * @param templateTaskMapper mapper for entity-DTO conversion
     */
    public TemplateTaskService(TemplateTaskRepository templateTaskRepository,
                               TemplateRepository templateRepository,
                               TemplateTaskMapper templateTaskMapper) {
        this.templateTaskRepository = templateTaskRepository;
        this.templateRepository = templateRepository;
        this.templateTaskMapper = templateTaskMapper;
    }
    
    /**
     * Retrieves all tasks for a specific template ordered by task order.
     * 
     * @param templateId the template ID
     * @return list of template task response DTOs
     * @throws TemplateNotFoundException if template doesn't exist
     */
    public List<TemplateTaskResponseDto> getAllTasksForTemplate(UUID templateId) {
        log.debug("Fetching all tasks for template: {}", templateId);
        
        // Verify template exists
        if (!templateRepository.existsById(templateId)) {
            throw new TemplateNotFoundException(templateId);
        }
        
        return templateTaskRepository.findByTemplateIdOrderByTaskOrderAsc(templateId)
                .stream()
                .map(templateTaskMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Creates a new task for a template.
     * 
     * <p>Validation includes:</p>
     * <ul>
     *   <li>Parent template existence check</li>
     * </ul>
     * 
     * @param templateId the parent template ID
     * @param dto create template task request DTO
     * @return created template task response DTO
     * @throws TemplateNotFoundException if parent template doesn't exist
     */
    @Transactional
    public TemplateTaskResponseDto createTask(UUID templateId, CreateTemplateTaskRequestDto dto) {
        log.info("Creating new task for template: {}", templateId);
        
        // Fetch parent template (throws TemplateNotFoundException if not found)
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException(templateId));
        
        // Map DTO to entity
        TemplateTask templateTask = templateTaskMapper.toEntity(dto);
        
        // Set template reference
        templateTask.setTemplate(template);
        
        // Save task
        TemplateTask savedTask = templateTaskRepository.save(templateTask);
        log.info("Successfully created task with id: {} for template: {}", savedTask.getId(), templateId);
        
        return templateTaskMapper.toResponseDto(savedTask);
    }
    
    /**
     * Updates an existing template task with partial update support.
     * 
     * <p>Only non-null fields in the DTO are updated. Validation includes:</p>
     * <ul>
     *   <li>Task existence check</li>
     *   <li>Task belongs to specified template check</li>
     * </ul>
     * 
     * @param templateId the template ID
     * @param taskId the task ID to update
     * @param dto update template task request DTO
     * @return updated template task response DTO
     * @throws TemplateNotFoundException if template doesn't exist
     * @throws TemplateTaskNotFoundException if task doesn't exist or doesn't belong to template
     */
    @Transactional
    public TemplateTaskResponseDto updateTask(UUID templateId, UUID taskId, UpdateTemplateTaskRequestDto dto) {
        log.info("Updating task {} for template: {}", taskId, templateId);
        
        // Verify template exists
        if (!templateRepository.existsById(templateId)) {
            throw new TemplateNotFoundException(templateId);
        }
        
        // Fetch task and verify it belongs to the specified template
        TemplateTask templateTask = templateTaskRepository.findByIdAndTemplateId(taskId, templateId)
                .orElseThrow(() -> new TemplateTaskNotFoundException(taskId, templateId));
        
        // Update entity from DTO (partial update)
        templateTaskMapper.updateEntityFromDto(dto, templateTask);
        
        // Save updated task (JPA dirty checking)
        TemplateTask updatedTask = templateTaskRepository.save(templateTask);
        log.info("Successfully updated task {} for template: {}", taskId, templateId);
        
        return templateTaskMapper.toResponseDto(updatedTask);
    }
    
    /**
     * Deletes a template task by ID.
     * 
     * <p>Before deletion, validates that:</p>
     * <ul>
     *   <li>Template exists</li>
     *   <li>Task exists and belongs to the specified template</li>
     * </ul>
     * 
     * @param templateId the template ID
     * @param taskId the task ID to delete
     * @throws TemplateNotFoundException if template doesn't exist
     * @throws TemplateTaskNotFoundException if task doesn't exist or doesn't belong to template
     */
    @Transactional
    public void deleteTask(UUID templateId, UUID taskId) {
        log.info("Attempting to delete task {} from template: {}", taskId, templateId);
        
        // Verify template exists
        if (!templateRepository.existsById(templateId)) {
            throw new TemplateNotFoundException(templateId);
        }
        
        // Verify task exists and belongs to the specified template
        TemplateTask templateTask = templateTaskRepository.findByIdAndTemplateId(taskId, templateId)
                .orElseThrow(() -> new TemplateTaskNotFoundException(taskId, templateId));
        
        try {
            templateTaskRepository.delete(templateTask);
            log.info("Successfully deleted task {} from template: {}", taskId, templateId);
        } catch (Exception e) {
            log.error("Error deleting task {} from template {}: {}", taskId, templateId, e.getMessage());
            throw new RuntimeException("Failed to delete template task: " + e.getMessage(), e);
        }
    }
}

