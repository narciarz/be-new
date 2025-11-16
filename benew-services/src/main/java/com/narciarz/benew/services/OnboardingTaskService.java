package com.narciarz.benew.services;

import com.narciarz.benew.exceptions.OnboardingProcessNotFoundException;
import com.narciarz.benew.exceptions.OnboardingTaskNotFoundException;
import com.narciarz.benew.models.OnboardingProcess;
import com.narciarz.benew.models.OnboardingTask;
import com.narciarz.benew.models.dto.OnboardingTaskResponseDto;
import com.narciarz.benew.models.dto.UpdateOnboardingTaskRequestDto;
import com.narciarz.benew.repositories.OnboardingProcessRepository;
import com.narciarz.benew.repositories.OnboardingTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for onboarding task management operations.
 * 
 * <p>Handles business logic, validation, and orchestration between the controller
 * and repository layers. All state-changing operations are transactional.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Read operations for onboarding tasks</li>
 *   <li>Task status updates (isCompleted)</li>
 *   <li>Automatic counter synchronization with parent process</li>
 *   <li>Validation that tasks belong to specified process</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class OnboardingTaskService {
    
    private static final Logger log = LoggerFactory.getLogger(OnboardingTaskService.class);
    
    private final OnboardingTaskRepository taskRepository;
    private final OnboardingProcessRepository processRepository;
    private final OnboardingTaskMapper taskMapper;
    
    /**
     * Constructor-based dependency injection.
     * 
     * @param taskRepository repository for onboarding task data access
     * @param processRepository repository for onboarding process data access
     * @param taskMapper mapper for entity-DTO conversion
     */
    public OnboardingTaskService(OnboardingTaskRepository taskRepository,
                                 OnboardingProcessRepository processRepository,
                                 OnboardingTaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.processRepository = processRepository;
        this.taskMapper = taskMapper;
    }
    
    /**
     * Retrieves all tasks for a specific onboarding process.
     * 
     * <p>Tasks are returned ordered by taskOrder for proper checklist display.</p>
     * 
     * @param processId the onboarding process ID
     * @return list of onboarding task response DTOs ordered by taskOrder
     * @throws OnboardingProcessNotFoundException if process doesn't exist
     */
    public List<OnboardingTaskResponseDto> getTasksByProcessId(UUID processId) {
        log.debug("Fetching tasks for onboarding process: {}", processId);
        
        // Verify process exists
        if (!processRepository.existsById(processId)) {
            throw new OnboardingProcessNotFoundException(processId);
        }
        
        List<OnboardingTask> tasks = taskRepository
                .findByOnboardingProcessIdOrderByTaskOrderAsc(processId);
        
        log.debug("Found {} tasks for process {}", tasks.size(), processId);
        
        return tasks.stream()
                .map(taskMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Retrieves a specific task by ID and validates it belongs to the specified process.
     * 
     * @param processId the onboarding process ID
     * @param taskId the task ID
     * @return onboarding task response DTO
     * @throws OnboardingTaskNotFoundException if task doesn't exist or doesn't belong to process
     */
    public OnboardingTaskResponseDto getTaskById(UUID processId, UUID taskId) {
        log.debug("Fetching task {} for process {}", taskId, processId);
        
        OnboardingTask task = taskRepository.findByIdAndOnboardingProcessId(taskId, processId)
                .orElseThrow(() -> new OnboardingTaskNotFoundException(taskId, processId));
        
        return taskMapper.toResponseDto(task);
    }
    
    /**
     * Updates a task's completion status.
     * 
     * <p>This method performs critical synchronization:</p>
     * <ol>
     *   <li>Updates the task's isCompleted field</li>
     *   <li>Recalculates completedTasksCount in parent OnboardingProcess</li>
     *   <li>Updates process counters for accurate progress tracking</li>
     * </ol>
     * 
     * <p>The counter synchronization ensures dashboard progress percentages
     * remain accurate without expensive COUNT() queries.</p>
     * 
     * @param processId the onboarding process ID
     * @param taskId the task ID
     * @param dto update request DTO containing new isCompleted value
     * @return updated onboarding task response DTO
     * @throws OnboardingTaskNotFoundException if task doesn't exist or doesn't belong to process
     */
    @Transactional
    public OnboardingTaskResponseDto updateTask(UUID processId, UUID taskId, 
                                               UpdateOnboardingTaskRequestDto dto) {
        log.info("Updating task {} in process {}", taskId, processId);
        
        // Fetch task and validate it belongs to process
        OnboardingTask task = taskRepository.findByIdAndOnboardingProcessId(taskId, processId)
                .orElseThrow(() -> new OnboardingTaskNotFoundException(taskId, processId));
        
        // Store old completion status for counter logic
        boolean wasCompleted = task.getIsCompleted() != null && task.getIsCompleted();
        
        // Update task entity from DTO
        taskMapper.updateEntityFromDto(dto, task);
        
        // Save task (JPA dirty checking)
        OnboardingTask updatedTask = taskRepository.save(task);
        
        // Check if completion status changed
        boolean isNowCompleted = updatedTask.getIsCompleted() != null && updatedTask.getIsCompleted();
        
        if (wasCompleted != isNowCompleted) {
            log.info("Task {} completion status changed: {} -> {}", 
                    taskId, wasCompleted, isNowCompleted);
            
            // Update parent process counters
            synchronizeProcessCounters(processId);
        }
        
        log.info("Successfully updated task {}", taskId);
        return taskMapper.toResponseDto(updatedTask);
    }
    
    /**
     * Synchronizes task counters in the parent onboarding process.
     * 
     * <p>Recalculates completedTasksCount by querying actual task completion status.
     * This ensures denormalized counters remain accurate after task updates.</p>
     * 
     * <p>Called automatically when task completion status changes.</p>
     * 
     * @param processId the onboarding process ID
     */
    private void synchronizeProcessCounters(UUID processId) {
        log.debug("Synchronizing counters for process {}", processId);
        
        // Fetch process
        OnboardingProcess process = processRepository.findById(processId)
                .orElseThrow(() -> new OnboardingProcessNotFoundException(processId));
        
        // Count actual completed tasks
        long completedCount = taskRepository.countByOnboardingProcessIdAndIsCompletedTrue(processId);
        long totalCount = taskRepository.countByOnboardingProcessId(processId);
        
        // Update process counters
        process.setCompletedTasksCount((int) completedCount);
        process.setTotalTasksCount((int) totalCount);
        
        // Save process (JPA dirty checking + auditing will update updatedAt)
        processRepository.save(process);
        
        log.info("Synchronized process {} counters: {}/{} tasks completed", 
                processId, completedCount, totalCount);
    }
}

