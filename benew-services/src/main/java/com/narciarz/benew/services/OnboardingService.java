package com.narciarz.benew.services;

import com.narciarz.benew.exceptions.OnboardingProcessDeletionException;
import com.narciarz.benew.exceptions.OnboardingProcessNotFoundException;
import com.narciarz.benew.exceptions.TemplateNotFoundException;
import com.narciarz.benew.exceptions.UserNotFoundException;
import com.narciarz.benew.models.*;
import com.narciarz.benew.models.dto.CreateOnboardingProcessRequestDto;
import com.narciarz.benew.models.dto.OnboardingProcessResponseDto;
import com.narciarz.benew.models.dto.UpdateOnboardingProcessRequestDto;
import com.narciarz.benew.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service layer for onboarding process management operations.
 * 
 * <p>Handles business logic, validation, and orchestration between the controller
 * and repository layers. All state-changing operations are transactional.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>CRUD operations for onboarding processes</li>
 *   <li>Automatic task copying from templates when creating processes</li>
 *   <li>Progress counter management (totalTasksCount, completedTasksCount)</li>
 *   <li>Process archiving/deletion with constraint enforcement</li>
 *   <li>Filtering by status, manager, and user</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class OnboardingService {
    
    private static final Logger log = LoggerFactory.getLogger(OnboardingService.class);
    
    private final OnboardingProcessRepository processRepository;
    private final OnboardingTaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final TemplateTaskRepository templateTaskRepository;
    private final OnboardingMapper onboardingMapper;
    
    /**
     * Constructor-based dependency injection.
     * 
     * @param processRepository repository for onboarding process data access
     * @param taskRepository repository for onboarding task data access
     * @param userRepository repository for user data access
     * @param templateRepository repository for template data access
     * @param templateTaskRepository repository for template task data access
     * @param onboardingMapper mapper for entity-DTO conversion
     */
    public OnboardingService(OnboardingProcessRepository processRepository,
                            OnboardingTaskRepository taskRepository,
                            UserRepository userRepository,
                            TemplateRepository templateRepository,
                            TemplateTaskRepository templateTaskRepository,
                            OnboardingMapper onboardingMapper) {
        this.processRepository = processRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.templateRepository = templateRepository;
        this.templateTaskRepository = templateTaskRepository;
        this.onboardingMapper = onboardingMapper;
    }
    
    /**
     * Retrieves all onboarding processes with pagination and optional filtering.
     * 
     * <p>Supports filtering by:</p>
     * <ul>
     *   <li>Status (ACTIVE or ARCHIVED)</li>
     *   <li>Manager ID (for manager dashboard)</li>
     *   <li>User ID (for specific employee)</li>
     * </ul>
     * 
     * @param pageable pagination parameters (page, size, sort)
     * @param status optional status filter
     * @param managerId optional manager filter
     * @param userId optional user filter
     * @return page of onboarding process response DTOs
     */
    public Page<OnboardingProcessResponseDto> getAllProcesses(Pageable pageable, 
                                                              OnboardingStatus status,
                                                              UUID managerId,
                                                              UUID userId) {
        log.debug("Fetching onboarding processes with filters - status: {}, managerId: {}, userId: {}", 
                 status, managerId, userId);
        
        Page<OnboardingProcess> processes;
        
        // Apply filters based on provided parameters
        if (managerId != null && status != null) {
            processes = processRepository.findByManagerIdAndStatus(managerId, status, pageable);
        } else if (managerId != null) {
            processes = processRepository.findByManagerId(managerId, pageable);
        } else if (userId != null) {
            processes = processRepository.findByUserId(userId, pageable);
        } else if (status != null) {
            processes = processRepository.findByStatus(status, pageable);
        } else {
            processes = processRepository.findAll(pageable);
        }
        
        return processes.map(onboardingMapper::toResponseDto);
    }
    
    /**
     * Retrieves a specific onboarding process by ID.
     * 
     * @param processId the process ID
     * @return onboarding process response DTO
     * @throws OnboardingProcessNotFoundException if process doesn't exist
     */
    public OnboardingProcessResponseDto getProcessById(UUID processId) {
        log.debug("Fetching onboarding process by id: {}", processId);
        OnboardingProcess process = processRepository.findById(processId)
                .orElseThrow(() -> new OnboardingProcessNotFoundException(processId));
        return onboardingMapper.toResponseDto(process);
    }
    
    /**
     * Creates a new onboarding process with automatic task copying from template.
     * 
     * <p>Process creation involves:</p>
     * <ol>
     *   <li>Validate user, manager, and template exist</li>
     *   <li>Create onboarding process in ACTIVE status</li>
     *   <li>Copy all tasks from source template to onboarding tasks</li>
     *   <li>Update process task counters (totalTasksCount)</li>
     * </ol>
     * 
     * <p>Tasks are COPIED (not referenced) to achieve versioning via denormalization.
     * Template changes will not affect active processes.</p>
     * 
     * @param dto create onboarding process request DTO
     * @return created onboarding process response DTO
     * @throws UserNotFoundException if user or manager doesn't exist
     * @throws TemplateNotFoundException if template doesn't exist
     */
    @Transactional
    public OnboardingProcessResponseDto createProcess(CreateOnboardingProcessRequestDto dto) {
        log.info("Creating new onboarding process for userId: {}, managerId: {}, templateId: {}",
                dto.getUserId(), dto.getManagerId(), dto.getSourceTemplateId());
        
        // Validate user exists
        AppUser user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException(dto.getUserId()));
        
        // Validate manager exists
        AppUser manager = userRepository.findById(dto.getManagerId())
                .orElseThrow(() -> new UserNotFoundException(dto.getManagerId()));
        
        // Validate template exists
        Template template = templateRepository.findById(dto.getSourceTemplateId())
                .orElseThrow(() -> new TemplateNotFoundException(dto.getSourceTemplateId()));
        
        // Create onboarding process
        OnboardingProcess process = new OnboardingProcess(user, manager, template, OnboardingStatus.ACTIVE);
        OnboardingProcess savedProcess = processRepository.save(process);
        log.debug("Created onboarding process with id: {}", savedProcess.getId());
        
        // Copy tasks from template to onboarding process
        List<TemplateTask> templateTasks = templateTaskRepository
                .findByTemplateIdOrderByTaskOrderAsc(template.getId());
        
        if (!templateTasks.isEmpty()) {
            log.debug("Copying {} tasks from template {} to process {}", 
                     templateTasks.size(), template.getId(), savedProcess.getId());
            
            for (TemplateTask templateTask : templateTasks) {
                OnboardingTask onboardingTask = new OnboardingTask(
                        savedProcess,
                        templateTask.getTitle(),
                        templateTask.getDescription(),
                        templateTask.getTaskOrder(),
                        templateTask.getOwnerRole()
                );
                taskRepository.save(onboardingTask);
            }
            
            // Update process task counters
            savedProcess.setTotalTasksCount(templateTasks.size());
            savedProcess.setCompletedTasksCount(0);
            processRepository.save(savedProcess);
            
            log.info("Successfully created onboarding process {} with {} tasks", 
                    savedProcess.getId(), templateTasks.size());
        } else {
            log.warn("Template {} has no tasks - created empty onboarding process {}", 
                    template.getId(), savedProcess.getId());
        }
        
        return onboardingMapper.toResponseDto(savedProcess);
    }
    
    /**
     * Updates an existing onboarding process.
     * 
     * <p>Supports partial updates including:</p>
     * <ul>
     *   <li>Status changes (e.g., ACTIVE to ARCHIVED)</li>
     *   <li>Manual adjustment of task counters if needed</li>
     * </ul>
     * 
     * <p>Note: Task counters are typically managed automatically by the application
     * when tasks are marked complete. Manual adjustment should be rare.</p>
     * 
     * @param processId the process ID to update
     * @param dto update onboarding process request DTO
     * @return updated onboarding process response DTO
     * @throws OnboardingProcessNotFoundException if process doesn't exist
     */
    @Transactional
    public OnboardingProcessResponseDto updateProcess(UUID processId, UpdateOnboardingProcessRequestDto dto) {
        log.info("Updating onboarding process with id: {}", processId);
        
        // Fetch existing process
        OnboardingProcess process = processRepository.findById(processId)
                .orElseThrow(() -> new OnboardingProcessNotFoundException(processId));
        
        // Update entity from DTO (partial update)
        onboardingMapper.updateEntityFromDto(dto, process);
        
        // Save updated process (JPA dirty checking)
        OnboardingProcess updatedProcess = processRepository.save(process);
        log.info("Successfully updated onboarding process with id: {}", processId);
        
        return onboardingMapper.toResponseDto(updatedProcess);
    }
    
    /**
     * Deletes an onboarding process by ID.
     * 
     * <p>Implements soft deletion by archiving the process rather than hard deletion.
     * This preserves historical data for reporting.</p>
     * 
     * <p>If hard deletion is needed, validates that:</p>
     * <ul>
     *   <li>Process exists</li>
     *   <li>All tasks are deleted first (ON DELETE RESTRICT constraint)</li>
     * </ul>
     * 
     * @param processId the process ID to delete
     * @throws OnboardingProcessNotFoundException if process doesn't exist
     * @throws OnboardingProcessDeletionException if process has associated tasks
     */
    @Transactional
    public void deleteProcess(UUID processId) {
        log.info("Attempting to delete onboarding process with id: {}", processId);
        
        // Verify process exists
        OnboardingProcess process = processRepository.findById(processId)
                .orElseThrow(() -> new OnboardingProcessNotFoundException(processId));
        
        // Soft deletion: archive the process instead of hard deletion
        if (process.getStatus() == OnboardingStatus.ACTIVE) {
            process.setStatus(OnboardingStatus.ARCHIVED);
            processRepository.save(process);
            log.info("Archived onboarding process with id: {}", processId);
            return;
        }
        
        // Hard deletion (if already archived and explicit deletion requested)
        // Check if process has associated tasks
        long taskCount = taskRepository.countByOnboardingProcessId(processId);
        if (taskCount > 0) {
            log.warn("Cannot delete process {} - still has {} task(s) associated", processId, taskCount);
            throw new OnboardingProcessDeletionException(processId, taskCount);
        }
        
        try {
            processRepository.deleteById(processId);
            log.info("Successfully deleted onboarding process with id: {}", processId);
        } catch (Exception e) {
            log.error("Error deleting onboarding process {}: {}", processId, e.getMessage());
            throw new OnboardingProcessDeletionException("Failed to delete process: " + e.getMessage(), e);
        }
    }
}

