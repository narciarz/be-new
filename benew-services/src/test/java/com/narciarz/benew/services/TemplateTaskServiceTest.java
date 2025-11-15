package com.narciarz.benew.services;

import com.narciarz.benew.exceptions.TemplateNotFoundException;
import com.narciarz.benew.exceptions.TemplateTaskNotFoundException;
import com.narciarz.benew.models.TaskOwnerRole;
import com.narciarz.benew.models.Template;
import com.narciarz.benew.models.TemplateTask;
import com.narciarz.benew.models.dto.CreateTemplateTaskRequestDto;
import com.narciarz.benew.models.dto.UpdateTemplateTaskRequestDto;
import com.narciarz.benew.models.dto.TemplateTaskResponseDto;
import com.narciarz.benew.repositories.TemplateRepository;
import com.narciarz.benew.repositories.TemplateTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TemplateTaskService}.
 * 
 * <p>Tests service layer business logic using Mockito to mock repository dependencies.
 * Validates service behavior, exception handling, and proper delegation to repositories.</p>
 */
@ExtendWith(MockitoExtension.class)
class TemplateTaskServiceTest {
    
    @Mock
    private TemplateTaskRepository templateTaskRepository;
    
    @Mock
    private TemplateRepository templateRepository;
    
    @Mock
    private TemplateTaskMapper templateTaskMapper;
    
    @InjectMocks
    private TemplateTaskService templateTaskService;
    
    private static final UUID TEMPLATE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID TASK_ID = UUID.fromString("660e8400-e29b-41d4-a716-446655440000");
    
    private Template template;
    private TemplateTask templateTask;
    private TemplateTaskResponseDto templateTaskResponseDto;
    
    @BeforeEach
    void setUp() {
        template = new Template("Software Engineer");
        template.setId(TEMPLATE_ID);
        
        templateTask = new TemplateTask(
                template,
                "Complete paperwork",
                "Fill out all HR forms",
                1,
                TaskOwnerRole.USER
        );
        templateTask.setId(TASK_ID);
        
        templateTaskResponseDto = new TemplateTaskResponseDto(
                TASK_ID,
                TEMPLATE_ID,
                "Complete paperwork",
                "Fill out all HR forms",
                1,
                TaskOwnerRole.USER,
                null,
                null
        );
    }
    
    // ==================== getAllTasksForTemplate Tests ====================
    
    @Test
    void shouldReturnAllTasksForTemplateOrderedByTaskOrder() {
        // Given
        TemplateTask task1 = new TemplateTask(template, "Task 1", "Desc 1", 1, TaskOwnerRole.USER);
        TemplateTask task2 = new TemplateTask(template, "Task 2", "Desc 2", 2, TaskOwnerRole.MANAGER);
        
        List<TemplateTask> tasks = Arrays.asList(task1, task2);
        
        when(templateRepository.existsById(TEMPLATE_ID)).thenReturn(true);
        when(templateTaskRepository.findByTemplateIdOrderByTaskOrderAsc(TEMPLATE_ID))
                .thenReturn(tasks);
        when(templateTaskMapper.toResponseDto(any(TemplateTask.class)))
                .thenReturn(new TemplateTaskResponseDto());
        
        // When
        List<TemplateTaskResponseDto> result = templateTaskService.getAllTasksForTemplate(TEMPLATE_ID);
        
        // Then
        assertThat(result).hasSize(2);
        verify(templateRepository).existsById(TEMPLATE_ID);
        verify(templateTaskRepository).findByTemplateIdOrderByTaskOrderAsc(TEMPLATE_ID);
        verify(templateTaskMapper, times(2)).toResponseDto(any(TemplateTask.class));
    }
    
    @Test
    void shouldReturnEmptyListWhenTemplateHasNoTasks() {
        // Given
        when(templateRepository.existsById(TEMPLATE_ID)).thenReturn(true);
        when(templateTaskRepository.findByTemplateIdOrderByTaskOrderAsc(TEMPLATE_ID))
                .thenReturn(Arrays.asList());
        
        // When
        List<TemplateTaskResponseDto> result = templateTaskService.getAllTasksForTemplate(TEMPLATE_ID);
        
        // Then
        assertThat(result).isEmpty();
        verify(templateRepository).existsById(TEMPLATE_ID);
        verify(templateTaskRepository).findByTemplateIdOrderByTaskOrderAsc(TEMPLATE_ID);
    }
    
    @Test
    void shouldThrowTemplateNotFoundExceptionWhenTemplateDoesNotExist() {
        // Given
        when(templateRepository.existsById(TEMPLATE_ID)).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> templateTaskService.getAllTasksForTemplate(TEMPLATE_ID))
                .isInstanceOf(TemplateNotFoundException.class)
                .hasMessageContaining(TEMPLATE_ID.toString());
        
        verify(templateRepository).existsById(TEMPLATE_ID);
        verify(templateTaskRepository, never()).findByTemplateIdOrderByTaskOrderAsc(any());
    }
    
    // ==================== createTask Tests ====================
    
    @Test
    void shouldCreateTaskSuccessfully() {
        // Given
        CreateTemplateTaskRequestDto dto = new CreateTemplateTaskRequestDto(
                "Complete paperwork",
                "Fill out all HR forms",
                1,
                TaskOwnerRole.USER
        );
        
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(templateTaskMapper.toEntity(dto)).thenReturn(templateTask);
        when(templateTaskRepository.save(any(TemplateTask.class))).thenReturn(templateTask);
        when(templateTaskMapper.toResponseDto(templateTask)).thenReturn(templateTaskResponseDto);
        
        // When
        TemplateTaskResponseDto result = templateTaskService.createTask(TEMPLATE_ID, dto);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TASK_ID);
        assertThat(result.getTemplateId()).isEqualTo(TEMPLATE_ID);
        
        verify(templateRepository).findById(TEMPLATE_ID);
        verify(templateTaskMapper).toEntity(dto);
        verify(templateTaskRepository).save(any(TemplateTask.class));
        verify(templateTaskMapper).toResponseDto(templateTask);
    }
    
    @Test
    void shouldSetTemplateReferenceWhenCreatingTask() {
        // Given
        CreateTemplateTaskRequestDto dto = new CreateTemplateTaskRequestDto(
                "Complete paperwork",
                "Description",
                1,
                TaskOwnerRole.USER
        );
        
        TemplateTask newTask = new TemplateTask();
        
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(templateTaskMapper.toEntity(dto)).thenReturn(newTask);
        when(templateTaskRepository.save(any(TemplateTask.class))).thenReturn(templateTask);
        when(templateTaskMapper.toResponseDto(any(TemplateTask.class))).thenReturn(templateTaskResponseDto);
        
        // When
        templateTaskService.createTask(TEMPLATE_ID, dto);
        
        // Then
        assertThat(newTask.getTemplate()).isEqualTo(template);
        verify(templateTaskRepository).save(newTask);
    }
    
    @Test
    void shouldThrowTemplateNotFoundExceptionWhenCreatingTaskForNonExistentTemplate() {
        // Given
        CreateTemplateTaskRequestDto dto = new CreateTemplateTaskRequestDto(
                "Task",
                "Description",
                1,
                TaskOwnerRole.USER
        );
        
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> templateTaskService.createTask(TEMPLATE_ID, dto))
                .isInstanceOf(TemplateNotFoundException.class)
                .hasMessageContaining(TEMPLATE_ID.toString());
        
        verify(templateRepository).findById(TEMPLATE_ID);
        verify(templateTaskRepository, never()).save(any());
    }
    
    // ==================== updateTask Tests ====================
    
    @Test
    void shouldUpdateTaskSuccessfully() {
        // Given
        UpdateTemplateTaskRequestDto dto = new UpdateTemplateTaskRequestDto(
                "Updated title",
                "Updated description",
                2,
                TaskOwnerRole.MANAGER
        );
        
        when(templateRepository.existsById(TEMPLATE_ID)).thenReturn(true);
        when(templateTaskRepository.findByIdAndTemplateId(TASK_ID, TEMPLATE_ID))
                .thenReturn(Optional.of(templateTask));
        when(templateTaskRepository.save(templateTask)).thenReturn(templateTask);
        when(templateTaskMapper.toResponseDto(templateTask)).thenReturn(templateTaskResponseDto);
        
        // When
        TemplateTaskResponseDto result = templateTaskService.updateTask(TEMPLATE_ID, TASK_ID, dto);
        
        // Then
        assertThat(result).isNotNull();
        verify(templateRepository).existsById(TEMPLATE_ID);
        verify(templateTaskRepository).findByIdAndTemplateId(TASK_ID, TEMPLATE_ID);
        verify(templateTaskMapper).updateEntityFromDto(dto, templateTask);
        verify(templateTaskRepository).save(templateTask);
    }
    
    @Test
    void shouldThrowTemplateNotFoundExceptionWhenUpdatingTaskForNonExistentTemplate() {
        // Given
        UpdateTemplateTaskRequestDto dto = new UpdateTemplateTaskRequestDto();
        dto.setTitle("Updated title");
        
        when(templateRepository.existsById(TEMPLATE_ID)).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> templateTaskService.updateTask(TEMPLATE_ID, TASK_ID, dto))
                .isInstanceOf(TemplateNotFoundException.class)
                .hasMessageContaining(TEMPLATE_ID.toString());
        
        verify(templateRepository).existsById(TEMPLATE_ID);
        verify(templateTaskRepository, never()).findByIdAndTemplateId(any(), any());
    }
    
    @Test
    void shouldThrowTemplateTaskNotFoundExceptionWhenTaskDoesNotExist() {
        // Given
        UpdateTemplateTaskRequestDto dto = new UpdateTemplateTaskRequestDto();
        dto.setTitle("Updated title");
        
        when(templateRepository.existsById(TEMPLATE_ID)).thenReturn(true);
        when(templateTaskRepository.findByIdAndTemplateId(TASK_ID, TEMPLATE_ID))
                .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> templateTaskService.updateTask(TEMPLATE_ID, TASK_ID, dto))
                .isInstanceOf(TemplateTaskNotFoundException.class)
                .hasMessageContaining(TASK_ID.toString())
                .hasMessageContaining(TEMPLATE_ID.toString());
        
        verify(templateTaskRepository, never()).save(any());
    }
    
    @Test
    void shouldThrowTemplateTaskNotFoundExceptionWhenTaskBelongsToDifferentTemplate() {
        // Given
        UpdateTemplateTaskRequestDto dto = new UpdateTemplateTaskRequestDto();
        dto.setTitle("Updated title");
        
        when(templateRepository.existsById(TEMPLATE_ID)).thenReturn(true);
        when(templateTaskRepository.findByIdAndTemplateId(TASK_ID, TEMPLATE_ID))
                .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> templateTaskService.updateTask(TEMPLATE_ID, TASK_ID, dto))
                .isInstanceOf(TemplateTaskNotFoundException.class);
        
        verify(templateTaskRepository, never()).save(any());
    }
    
    // ==================== deleteTask Tests ====================
    
    @Test
    void shouldDeleteTaskSuccessfully() {
        // Given
        when(templateRepository.existsById(TEMPLATE_ID)).thenReturn(true);
        when(templateTaskRepository.findByIdAndTemplateId(TASK_ID, TEMPLATE_ID))
                .thenReturn(Optional.of(templateTask));
        
        // When
        templateTaskService.deleteTask(TEMPLATE_ID, TASK_ID);
        
        // Then
        verify(templateRepository).existsById(TEMPLATE_ID);
        verify(templateTaskRepository).findByIdAndTemplateId(TASK_ID, TEMPLATE_ID);
        verify(templateTaskRepository).delete(templateTask);
    }
    
    @Test
    void shouldThrowTemplateNotFoundExceptionWhenDeletingTaskForNonExistentTemplate() {
        // Given
        when(templateRepository.existsById(TEMPLATE_ID)).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> templateTaskService.deleteTask(TEMPLATE_ID, TASK_ID))
                .isInstanceOf(TemplateNotFoundException.class)
                .hasMessageContaining(TEMPLATE_ID.toString());
        
        verify(templateRepository).existsById(TEMPLATE_ID);
        verify(templateTaskRepository, never()).delete(any());
    }
    
    @Test
    void shouldThrowTemplateTaskNotFoundExceptionWhenDeletingNonExistentTask() {
        // Given
        when(templateRepository.existsById(TEMPLATE_ID)).thenReturn(true);
        when(templateTaskRepository.findByIdAndTemplateId(TASK_ID, TEMPLATE_ID))
                .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> templateTaskService.deleteTask(TEMPLATE_ID, TASK_ID))
                .isInstanceOf(TemplateTaskNotFoundException.class)
                .hasMessageContaining(TASK_ID.toString())
                .hasMessageContaining(TEMPLATE_ID.toString());
        
        verify(templateTaskRepository, never()).delete(any());
    }
}

