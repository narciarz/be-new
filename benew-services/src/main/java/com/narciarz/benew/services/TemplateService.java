package com.narciarz.benew.services;

import com.narciarz.benew.exceptions.CsvImportException;
import com.narciarz.benew.exceptions.DuplicatePositionNameException;
import com.narciarz.benew.exceptions.TemplateDeletionException;
import com.narciarz.benew.exceptions.TemplateNotFoundException;
import com.narciarz.benew.models.TaskOwnerRole;
import com.narciarz.benew.models.Template;
import com.narciarz.benew.models.TemplateTask;
import com.narciarz.benew.models.dto.CreateTemplateRequestDto;
import com.narciarz.benew.models.dto.UpdateTemplateRequestDto;
import com.narciarz.benew.models.dto.TemplateResponseDto;
import com.narciarz.benew.models.dto.TemplateImportResponseDto;
import com.narciarz.benew.repositories.TemplateRepository;
import com.narciarz.benew.repositories.TemplateTaskRepository;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    
    /**
     * Imports a template with tasks from a CSV file.
     * 
     * <p>CSV format expected:</p>
     * <ul>
     *   <li>First line: "position_name,task_order,task_title,task_description,owner_role" (header)</li>
     *   <li>Subsequent lines: task data with position name repeated for each task</li>
     * </ul>
     * 
     * <p>Example:</p>
     * <pre>
     * position_name,task_order,task_title,task_description,owner_role
     * IT DevOps,1,Workspace setup,Configure laptop and tools,USER
     * IT DevOps,2,Team meeting,Meet with the team,MANAGER
     * </pre>
     * 
     * <p>Validation includes:</p>
     * <ul>
     *   <li>File presence and non-empty check</li>
     *   <li>CSV structure validation</li>
     *   <li>Position name uniqueness</li>
     *   <li>Task data validation (required fields, data types)</li>
     * </ul>
     * 
     * <p>All operations are transactional - if any part fails, entire import is rolled back.</p>
     * 
     * @param file the CSV file to import
     * @return import summary with created template ID and task count
     * @throws CsvImportException if file is invalid or has format errors
     * @throws DuplicatePositionNameException if position name already exists
     */
    @Transactional
    public TemplateImportResponseDto importTemplateFromCsv(MultipartFile file) {
        log.info("Starting CSV import for template");
        
        // Validate file presence and type
        validateCsvFile(file);
        
        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            // Parse CSV with proper configuration
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(',')
                    .withIgnoreQuotations(false)
                    .build();

            List<String[]> rows;
            try (CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(parser)
                    .build()) {

                rows = csvReader.readAll();
            }

            // Validate minimum rows (header + at least 1 task)
            if (rows.size() < 2) {
                throw new CsvImportException(
                    "CSV file must contain at least 2 rows: header and at least one task");
            }
            
            // Validate header
            String[] header = rows.get(0);
            validateNewFormatHeader(header);
            
            // Extract position name from first data row
            if (rows.size() < 2 || isEmptyRow(rows.get(1))) {
                throw new CsvImportException("CSV file must contain at least one task row");
            }
            
            String positionName = rows.get(1)[0].trim();
            if (positionName.isEmpty()) {
                throw new CsvImportException("Position name is required in the first data row");
            }
            
            // Validate position name length
            if (positionName.length() > 50) {
                throw new CsvImportException(
                    "Position name exceeds maximum length of 50 characters: " + positionName
                );
            }
            
            // Validate position name uniqueness
            String normalizedName = normalizePositionName(positionName);
            if (templateRepository.existsByPositionNameIgnoreCase(normalizedName)) {
                log.warn("Attempt to import template with duplicate position name: {}", normalizedName);
                throw new DuplicatePositionNameException(normalizedName);
            }
            
            // Create template
            Template template = new Template();
            template.setPositionName(normalizedName);
            Template savedTemplate = templateRepository.save(template);
            log.info("Created template with id: {} for position: {}", savedTemplate.getId(), normalizedName);
            
            // Parse and create tasks
            List<TemplateTask> tasks = new ArrayList<>();
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                
                // Skip empty rows
                if (isEmptyRow(row)) {
                    continue;
                }
                
                TemplateTask task = parseTaskFromNewFormatRow(row, savedTemplate, i + 1);
                tasks.add(task);
            }
            
            // Validate at least one task
            if (tasks.isEmpty()) {
                throw new CsvImportException("CSV file must contain at least one task");
            }
            
            // Save all tasks
            List<TemplateTask> savedTasks = templateTaskRepository.saveAll(tasks);
            log.info("Successfully imported {} tasks for template {}", savedTasks.size(), savedTemplate.getId());
            
            // Prepare response
            List<UUID> taskIds = savedTasks.stream()
                    .map(TemplateTask::getId)
                    .collect(Collectors.toList());
            
            String message = String.format(
                "Successfully imported template '%s' with %d task(s)", 
                positionName, 
                savedTasks.size()
            );
            
            return new TemplateImportResponseDto(
                savedTemplate.getId(),
                positionName,
                savedTasks.size(),
                taskIds,
                message
            );
            
        } catch (IOException e) {
            log.error("Error reading CSV file: {}", e.getMessage());
            throw new CsvImportException("Failed to read CSV file: " + e.getMessage(), e);
        } catch (CsvException e) {
            log.error("Error parsing CSV file: {}", e.getMessage());
            throw new CsvImportException("Failed to parse CSV file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates the uploaded CSV file.
     * 
     * @param file the file to validate
     * @throws CsvImportException if file is invalid
     */
    private void validateCsvFile(MultipartFile file) {
        // Check file presence
        if (file == null || file.isEmpty()) {
            throw new CsvImportException("CSV file is required and cannot be empty");
        }
        
        // Check file name
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
            throw new CsvImportException("File must be a CSV file with .csv extension");
        }
        
        // Check file size (max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new CsvImportException(
                String.format("File size exceeds maximum allowed size of %d MB", maxSize / (1024 * 1024))
            );
        }
        
        log.debug("CSV file validation passed: {}", originalFilename);
    }
    
    /**
     * Extracts position name from CSV rows.
     * 
     * @param rows all CSV rows
     * @return position name
     * @throws CsvImportException if position name is invalid
     */
    private String extractPositionName(List<String[]> rows) {
        // First row should be "position_name" header
        String[] firstRow = rows.get(0);
        if (firstRow.length == 0 || !firstRow[0].trim().equalsIgnoreCase("position_name")) {
            throw new CsvImportException(
                "First row must contain 'position_name' header, found: " + 
                (firstRow.length > 0 ? firstRow[0] : "empty")
            );
        }
        
        // Second row should contain the actual position name
        String[] secondRow = rows.get(1);
        if (secondRow.length == 0 || secondRow[0] == null || secondRow[0].trim().isEmpty()) {
            throw new CsvImportException("Position name value is required in second row");
        }
        
        String positionName = secondRow[0].trim();
        
        // Validate position name length
        if (positionName.length() > 50) {
            throw new CsvImportException(
                "Position name exceeds maximum length of 50 characters: " + positionName
            );
        }
        
        log.debug("Extracted position name: {}", positionName);
        return positionName;
    }
    
    /**
     * Validates task header row (OLD FORMAT - kept for backward compatibility).
     * 
     * @param header the header row
     * @throws CsvImportException if header is invalid
     */
    private void validateTaskHeader(String[] header) {
        if (header.length < 4) {
            throw new CsvImportException(
                "Task header must contain at least 4 columns: title, description, task_order, owner_role"
            );
        }
        
        // Check required columns (case-insensitive)
        String[] requiredColumns = {"title", "description", "task_order", "owner_role"};
        for (int i = 0; i < requiredColumns.length; i++) {
            if (!header[i].trim().equalsIgnoreCase(requiredColumns[i])) {
                throw new CsvImportException(
                    String.format("Column %d must be '%s', found: '%s'", 
                        i + 1, requiredColumns[i], header[i])
                );
            }
        }
        
        log.debug("Task header validation passed");
    }
    
    /**
     * Validates NEW FORMAT header row.
     * 
     * @param header the header row
     * @throws CsvImportException if header is invalid
     */
    private void validateNewFormatHeader(String[] header) {
        if (header.length < 5) {
            throw new CsvImportException(
                "CSV header must contain 5 columns: position_name, task_order, task_title, task_description, owner_role"
            );
        }
        
        // Check required columns (case-insensitive)
        String[] requiredColumns = {"position_name", "task_order", "task_title", "task_description", "owner_role"};
        for (int i = 0; i < requiredColumns.length; i++) {
            String actual = header[i].trim().toLowerCase();
            String expected = requiredColumns[i].toLowerCase();
            if (!actual.equals(expected)) {
                throw new CsvImportException(
                    String.format("Column %d must be '%s', found: '%s'", 
                        i + 1, requiredColumns[i], header[i])
                );
            }
        }
        
        log.debug("New format header validation passed");
    }
    
    /**
     * Parses a task from a CSV row in NEW FORMAT.
     * Row format: position_name, task_order, task_title, task_description, owner_role
     * 
     * @param row the CSV row
     * @param template the parent template
     * @param rowNumber the row number (for error messages)
     * @return parsed TemplateTask
     * @throws CsvImportException if row data is invalid
     */
    private TemplateTask parseTaskFromNewFormatRow(String[] row, Template template, int rowNumber) {
        if (row.length < 5) {
            throw new CsvImportException(
                String.format("Row %d must contain 5 columns (position_name, task_order, task_title, task_description, owner_role)", 
                    rowNumber)
            );
        }
        
        // Extract fields - skip position_name (column 0) as we already have the template
        String taskOrderStr = row[1] != null ? row[1].trim() : "";
        String title = row[2] != null ? row[2].trim() : "";
        String description = row[3] != null ? row[3].trim() : "";
        String ownerRoleStr = row[4] != null ? row[4].trim() : "";
        
        // Validate title (required)
        if (title.isEmpty()) {
            throw new CsvImportException(
                String.format("Row %d: task_title is required", rowNumber)
            );
        }
        
        if (title.length() > 255) {
            throw new CsvImportException(
                String.format("Row %d: task_title exceeds maximum length of 255 characters", rowNumber)
            );
        }
        
        // Validate description length (optional field)
        if (description != null && description.length() > 500) {
            throw new CsvImportException(
                String.format("Row %d: task_description exceeds maximum length of 500 characters", rowNumber)
            );
        }
        
        // Parse and validate task_order
        int taskOrder;
        try {
            taskOrder = Integer.parseInt(taskOrderStr);
            if (taskOrder < 1) {
                throw new CsvImportException(
                    String.format("Row %d: task_order must be a positive number, found: %d", 
                        rowNumber, taskOrder)
                );
            }
        } catch (NumberFormatException e) {
            throw new CsvImportException(
                String.format("Row %d: task_order must be a valid number, found: '%s'", 
                    rowNumber, taskOrderStr)
            );
        }
        
        // Parse and validate owner_role
        TaskOwnerRole ownerRole;
        try {
            ownerRole = TaskOwnerRole.valueOf(ownerRoleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CsvImportException(
                String.format("Row %d: Invalid owner_role '%s'. Allowed values: USER, MANAGER", 
                    rowNumber, ownerRoleStr)
            );
        }
        
        // Create and return task
        TemplateTask task = new TemplateTask();
        task.setTemplate(template);
        task.setTitle(title);
        task.setDescription(description.isEmpty() ? null : description);
        task.setTaskOrder(taskOrder);
        task.setOwnerRole(ownerRole);
        
        log.debug("Parsed task: order={}, title={}, ownerRole={}", taskOrder, title, ownerRole);
        return task;
    }
    
    /**
     * Parses a task from a CSV row (OLD FORMAT - kept for backward compatibility).
     * 
     * @param row the CSV row
     * @param template the parent template
     * @param rowNumber the row number (for error messages)
     * @return parsed TemplateTask
     * @throws CsvImportException if row data is invalid
     */
    private TemplateTask parseTaskFromRow(String[] row, Template template, int rowNumber) {
        if (row.length < 4) {
            throw new CsvImportException(
                String.format("Row %d must contain at least 4 columns (title, description, task_order, owner_role)", 
                    rowNumber)
            );
        }
        
        // Extract fields
        String title = row[0] != null ? row[0].trim() : "";
        String description = row[1] != null ? row[1].trim() : "";
        String taskOrderStr = row[2] != null ? row[2].trim() : "";
        String ownerRoleStr = row[3] != null ? row[3].trim() : "";
        
        // Validate title (required)
        if (title.isEmpty()) {
            throw new CsvImportException(
                String.format("Row %d: title is required", rowNumber)
            );
        }
        
        if (title.length() > 255) {
            throw new CsvImportException(
                String.format("Row %d: title exceeds maximum length of 255 characters", rowNumber)
            );
        }
        
        // Parse task order (required, must be positive integer)
        int taskOrder;
        try {
            taskOrder = Integer.parseInt(taskOrderStr);
            if (taskOrder <= 0) {
                throw new CsvImportException(
                    String.format("Row %d: task_order must be a positive integer, found: %d", 
                        rowNumber, taskOrder)
                );
            }
        } catch (NumberFormatException e) {
            throw new CsvImportException(
                String.format("Row %d: task_order must be a valid integer, found: '%s'", 
                    rowNumber, taskOrderStr)
            );
        }
        
        // Parse owner role (required, must be MANAGER or USER)
        TaskOwnerRole ownerRole;
        try {
            ownerRole = TaskOwnerRole.valueOf(ownerRoleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CsvImportException(
                String.format("Row %d: owner_role must be either 'MANAGER' or 'USER', found: '%s'", 
                    rowNumber, ownerRoleStr)
            );
        }
        
        // Create task
        TemplateTask task = new TemplateTask();
        task.setTemplate(template);
        task.setTitle(title);
        task.setDescription(description.isEmpty() ? null : description);
        task.setTaskOrder(taskOrder);
        task.setOwnerRole(ownerRole);
        
        log.debug("Parsed task from row {}: {}", rowNumber, title);
        return task;
    }
    
    /**
     * Checks if a CSV row is empty.
     * 
     * @param row the row to check
     * @return true if row is empty or contains only empty strings
     */
    private boolean isEmptyRow(String[] row) {
        if (row == null) {
            return true;
        }
        
        for (String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
}

