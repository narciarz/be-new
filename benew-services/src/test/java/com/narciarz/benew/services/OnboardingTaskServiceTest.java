package com.narciarz.benew.services;

import com.narciarz.benew.exceptions.OnboardingProcessNotFoundException;
import com.narciarz.benew.exceptions.OnboardingTaskNotFoundException;
import com.narciarz.benew.models.*;
import com.narciarz.benew.models.dto.OnboardingTaskResponseDto;
import com.narciarz.benew.models.dto.UpdateOnboardingTaskRequestDto;
import com.narciarz.benew.repositories.OnboardingProcessRepository;
import com.narciarz.benew.repositories.OnboardingTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OnboardingTaskService.
 * 
 * <p>Tests business logic, validation, and error handling using Mockito
 * to mock dependencies. Follows AAA (Arrange-Act-Assert) pattern.</p>
 * 
 * <p>Special focus on counter synchronization logic when tasks are marked complete.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OnboardingTaskService Unit Tests")
class OnboardingTaskServiceTest {
    
    @Mock
    private OnboardingTaskRepository taskRepository;
    
    @Mock
    private OnboardingProcessRepository processRepository;
    
    @Mock
    private OnboardingTaskMapper taskMapper;
    
    @InjectMocks
    private OnboardingTaskService taskService;
    
    private OnboardingProcess testProcess;
    private OnboardingTask testTask1;
    private OnboardingTask testTask2;
    private OnboardingTaskResponseDto taskResponseDto1;
    private OnboardingTaskResponseDto taskResponseDto2;
    private UpdateOnboardingTaskRequestDto updateTaskDto;
    private UUID processId;
    private UUID taskId1;
    private UUID taskId2;
    
    @BeforeEach
    void setUp() {
        processId = UUID.randomUUID();
        taskId1 = UUID.randomUUID();
        taskId2 = UUID.randomUUID();
        
        // Setup test process
        testProcess = new OnboardingProcess();
        testProcess.setId(processId);
        testProcess.setStatus(OnboardingStatus.ACTIVE);
        testProcess.setTotalTasksCount(2);
        testProcess.setCompletedTasksCount(0);
        testProcess.setCreatedAt(OffsetDateTime.now());
        testProcess.setUpdatedAt(OffsetDateTime.now());
        
        // Setup test tasks
        testTask1 = new OnboardingTask();
        testTask1.setId(taskId1);
        testTask1.setOnboardingProcess(testProcess);
        testTask1.setTitle("Task 1");
        testTask1.setDescription("Description 1");
        testTask1.setTaskOrder(1);
        testTask1.setOwnerRole(TaskOwnerRole.USER);
        testTask1.setIsCompleted(false);
        testTask1.setCreatedAt(OffsetDateTime.now());
        testTask1.setUpdatedAt(OffsetDateTime.now());
        
        testTask2 = new OnboardingTask();
        testTask2.setId(taskId2);
        testTask2.setOnboardingProcess(testProcess);
        testTask2.setTitle("Task 2");
        testTask2.setDescription("Description 2");
        testTask2.setTaskOrder(2);
        testTask2.setOwnerRole(TaskOwnerRole.MANAGER);
        testTask2.setIsCompleted(false);
        testTask2.setCreatedAt(OffsetDateTime.now());
        testTask2.setUpdatedAt(OffsetDateTime.now());
        
        // Setup DTOs
        taskResponseDto1 = new OnboardingTaskResponseDto(
                taskId1, processId, "Task 1", "Description 1", 1,
                TaskOwnerRole.USER, false, OffsetDateTime.now(), OffsetDateTime.now()
        );
        
        taskResponseDto2 = new OnboardingTaskResponseDto(
                taskId2, processId, "Task 2", "Description 2", 2,
                TaskOwnerRole.MANAGER, false, OffsetDateTime.now(), OffsetDateTime.now()
        );
        
        updateTaskDto = new UpdateOnboardingTaskRequestDto(true);
    }
    
    @Test
    @DisplayName("Should get all tasks for process ordered by taskOrder")
    void shouldGetTasksByProcessId() {
        // Arrange
        List<OnboardingTask> tasks = Arrays.asList(testTask1, testTask2);
        
        when(processRepository.existsById(processId)).thenReturn(true);
        when(taskRepository.findByOnboardingProcessIdOrderByTaskOrderAsc(processId)).thenReturn(tasks);
        when(taskMapper.toResponseDto(testTask1)).thenReturn(taskResponseDto1);
        when(taskMapper.toResponseDto(testTask2)).thenReturn(taskResponseDto2);
        
        // Act
        List<OnboardingTaskResponseDto> result = taskService.getTasksByProcessId(processId);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTaskOrder()).isEqualTo(1);
        assertThat(result.get(1).getTaskOrder()).isEqualTo(2);
        
        verify(processRepository).existsById(processId);
        verify(taskRepository).findByOnboardingProcessIdOrderByTaskOrderAsc(processId);
        verify(taskMapper, times(2)).toResponseDto(any(OnboardingTask.class));
    }
    
    @Test
    @DisplayName("Should throw exception when process not found")
    void shouldThrowExceptionWhenProcessNotFound() {
        // Arrange
        when(processRepository.existsById(processId)).thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> taskService.getTasksByProcessId(processId))
                .isInstanceOf(OnboardingProcessNotFoundException.class)
                .hasMessageContaining(processId.toString());
        
        verify(processRepository).existsById(processId);
        verifyNoInteractions(taskRepository);
    }
    
    @Test
    @DisplayName("Should get task by id and process id")
    void shouldGetTaskById() {
        // Arrange
        when(taskRepository.findByIdAndOnboardingProcessId(taskId1, processId))
                .thenReturn(Optional.of(testTask1));
        when(taskMapper.toResponseDto(testTask1)).thenReturn(taskResponseDto1);
        
        // Act
        OnboardingTaskResponseDto result = taskService.getTaskById(processId, taskId1);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(taskId1);
        assertThat(result.getOnboardingProcessId()).isEqualTo(processId);
        
        verify(taskRepository).findByIdAndOnboardingProcessId(taskId1, processId);
        verify(taskMapper).toResponseDto(testTask1);
    }
    
    @Test
    @DisplayName("Should throw exception when task not found in process")
    void shouldThrowExceptionWhenTaskNotFoundInProcess() {
        // Arrange
        when(taskRepository.findByIdAndOnboardingProcessId(taskId1, processId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> taskService.getTaskById(processId, taskId1))
                .isInstanceOf(OnboardingTaskNotFoundException.class)
                .hasMessageContaining(taskId1.toString())
                .hasMessageContaining(processId.toString());
        
        verify(taskRepository).findByIdAndOnboardingProcessId(taskId1, processId);
        verifyNoInteractions(taskMapper);
    }
    
    @Test
    @DisplayName("Should update task and synchronize counters when completion status changes")
    void shouldUpdateTaskAndSynchronizeCounters() {
        // Arrange - task is not completed initially
        testTask1.setIsCompleted(false);
        
        when(taskRepository.findByIdAndOnboardingProcessId(taskId1, processId))
                .thenReturn(Optional.of(testTask1));
        when(taskRepository.save(testTask1)).thenAnswer(invocation -> {
            OnboardingTask task = invocation.getArgument(0);
            task.setIsCompleted(true); // Simulate update
            return task;
        });
        when(processRepository.findById(processId)).thenReturn(Optional.of(testProcess));
        when(processRepository.save(testProcess)).thenReturn(testProcess);
        when(taskRepository.countByOnboardingProcessIdAndIsCompletedTrue(processId)).thenReturn(1L);
        when(taskRepository.countByOnboardingProcessId(processId)).thenReturn(2L);
        when(taskMapper.toResponseDto(testTask1)).thenReturn(taskResponseDto1);
        
        // Act
        taskService.updateTask(processId, taskId1, updateTaskDto);
        
        // Assert
        verify(taskRepository).findByIdAndOnboardingProcessId(taskId1, processId);
        verify(taskMapper).updateEntityFromDto(updateTaskDto, testTask1);
        verify(taskRepository).save(testTask1);
        
        // Verify counter synchronization was triggered
        verify(processRepository).findById(processId);
        verify(taskRepository).countByOnboardingProcessIdAndIsCompletedTrue(processId);
        verify(taskRepository).countByOnboardingProcessId(processId);
        verify(processRepository).save(testProcess);
        
        // Verify process counters were updated
        assertThat(testProcess.getCompletedTasksCount()).isEqualTo(1);
        assertThat(testProcess.getTotalTasksCount()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Should update task without synchronizing counters when completion status unchanged")
    void shouldUpdateTaskWithoutSynchronizingWhenStatusUnchanged() {
        // Arrange - task is already completed
        testTask1.setIsCompleted(true);
        UpdateOnboardingTaskRequestDto unchangedDto = new UpdateOnboardingTaskRequestDto(true);
        
        when(taskRepository.findByIdAndOnboardingProcessId(taskId1, processId))
                .thenReturn(Optional.of(testTask1));
        when(taskRepository.save(testTask1)).thenReturn(testTask1);
        when(taskMapper.toResponseDto(testTask1)).thenReturn(taskResponseDto1);
        
        // Act
        taskService.updateTask(processId, taskId1, unchangedDto);
        
        // Assert
        verify(taskRepository).findByIdAndOnboardingProcessId(taskId1, processId);
        verify(taskMapper).updateEntityFromDto(unchangedDto, testTask1);
        verify(taskRepository).save(testTask1);
        
        // Verify counter synchronization was NOT triggered
        verify(processRepository, never()).findById(any());
        verify(taskRepository, never()).countByOnboardingProcessIdAndIsCompletedTrue(any());
        verify(processRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should synchronize counters when marking task as incomplete")
    void shouldSynchronizeCountersWhenMarkingIncomplete() {
        // Arrange - task is completed, marking as incomplete
        testTask1.setIsCompleted(true);
        UpdateOnboardingTaskRequestDto incompleteDto = new UpdateOnboardingTaskRequestDto(false);
        
        when(taskRepository.findByIdAndOnboardingProcessId(taskId1, processId))
                .thenReturn(Optional.of(testTask1));
        when(taskRepository.save(testTask1)).thenAnswer(invocation -> {
            OnboardingTask task = invocation.getArgument(0);
            task.setIsCompleted(false); // Simulate update
            return task;
        });
        when(processRepository.findById(processId)).thenReturn(Optional.of(testProcess));
        when(processRepository.save(testProcess)).thenReturn(testProcess);
        when(taskRepository.countByOnboardingProcessIdAndIsCompletedTrue(processId)).thenReturn(0L);
        when(taskRepository.countByOnboardingProcessId(processId)).thenReturn(2L);
        when(taskMapper.toResponseDto(testTask1)).thenReturn(taskResponseDto1);
        
        // Act
        taskService.updateTask(processId, taskId1, incompleteDto);
        
        // Assert
        verify(taskRepository).findByIdAndOnboardingProcessId(taskId1, processId);
        verify(taskRepository).save(testTask1);
        
        // Verify counter synchronization was triggered
        verify(processRepository).findById(processId);
        verify(taskRepository).countByOnboardingProcessIdAndIsCompletedTrue(processId);
        verify(processRepository).save(testProcess);
        
        // Verify counters were updated correctly
        assertThat(testProcess.getCompletedTasksCount()).isEqualTo(0);
        assertThat(testProcess.getTotalTasksCount()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Should throw exception when updating task not in process")
    void shouldThrowExceptionWhenUpdatingTaskNotInProcess() {
        // Arrange
        when(taskRepository.findByIdAndOnboardingProcessId(taskId1, processId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> taskService.updateTask(processId, taskId1, updateTaskDto))
                .isInstanceOf(OnboardingTaskNotFoundException.class)
                .hasMessageContaining(taskId1.toString())
                .hasMessageContaining(processId.toString());
        
        verify(taskRepository).findByIdAndOnboardingProcessId(taskId1, processId);
        verify(taskRepository, never()).save(any());
        verifyNoInteractions(processRepository);
    }
    
    @Test
    @DisplayName("Should handle null isCompleted value gracefully")
    void shouldHandleNullIsCompletedGracefully() {
        // Arrange - task has null isCompleted
        testTask1.setIsCompleted(null);
        
        when(taskRepository.findByIdAndOnboardingProcessId(taskId1, processId))
                .thenReturn(Optional.of(testTask1));
        when(taskRepository.save(testTask1)).thenAnswer(invocation -> {
            OnboardingTask task = invocation.getArgument(0);
            task.setIsCompleted(true);
            return task;
        });
        when(processRepository.findById(processId)).thenReturn(Optional.of(testProcess));
        when(processRepository.save(testProcess)).thenReturn(testProcess);
        when(taskRepository.countByOnboardingProcessIdAndIsCompletedTrue(processId)).thenReturn(1L);
        when(taskRepository.countByOnboardingProcessId(processId)).thenReturn(2L);
        when(taskMapper.toResponseDto(testTask1)).thenReturn(taskResponseDto1);
        
        // Act
        taskService.updateTask(processId, taskId1, updateTaskDto);
        
        // Assert - should trigger synchronization (null -> true is a change)
        verify(processRepository).findById(processId);
        verify(processRepository).save(testProcess);
    }
    
    @Test
    @DisplayName("Should return empty list when process has no tasks")
    void shouldReturnEmptyListWhenNoTasks() {
        // Arrange
        when(processRepository.existsById(processId)).thenReturn(true);
        when(taskRepository.findByOnboardingProcessIdOrderByTaskOrderAsc(processId))
                .thenReturn(Arrays.asList());
        
        // Act
        List<OnboardingTaskResponseDto> result = taskService.getTasksByProcessId(processId);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        verify(processRepository).existsById(processId);
        verify(taskRepository).findByOnboardingProcessIdOrderByTaskOrderAsc(processId);
        verifyNoInteractions(taskMapper);
    }
}

