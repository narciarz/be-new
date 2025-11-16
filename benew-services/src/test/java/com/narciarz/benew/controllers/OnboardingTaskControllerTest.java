package com.narciarz.benew.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.narciarz.benew.exceptions.OnboardingProcessNotFoundException;
import com.narciarz.benew.exceptions.OnboardingTaskNotFoundException;
import com.narciarz.benew.models.TaskOwnerRole;
import com.narciarz.benew.models.dto.OnboardingTaskResponseDto;
import com.narciarz.benew.models.dto.UpdateOnboardingTaskRequestDto;
import com.narciarz.benew.services.OnboardingTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for OnboardingTaskController using @WebMvcTest.
 * 
 * <p>Tests the web layer (controller) in isolation with mocked service layer.
 * Uses MockMvc to perform HTTP requests and verify responses.</p>
 */
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(OnboardingTaskController.class)
@DisplayName("OnboardingTaskController Integration Tests")
class OnboardingTaskControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private OnboardingTaskService taskService;
    
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
        
        taskResponseDto1 = new OnboardingTaskResponseDto(
                taskId1,
                processId,
                "Setup development environment",
                "Install IDE, configure git, setup local database",
                1,
                TaskOwnerRole.USER,
                false,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
        
        taskResponseDto2 = new OnboardingTaskResponseDto(
                taskId2,
                processId,
                "Introduction to team",
                "Schedule 1:1 meetings with team members",
                2,
                TaskOwnerRole.MANAGER,
                false,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
        
        updateTaskDto = new UpdateOnboardingTaskRequestDto(true);
    }
    
    @Test
    @DisplayName("GET /onboarding/{processId}/tasks - should return list of tasks")
    void shouldGetAllTasksForProcess() throws Exception {
        // Arrange
        List<OnboardingTaskResponseDto> tasks = Arrays.asList(taskResponseDto1, taskResponseDto2);
        when(taskService.getTasksByProcessId(processId)).thenReturn(tasks);
        
        // Act & Assert
        mockMvc.perform(get("/onboarding/{processId}/tasks", processId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(taskId1.toString()))
                .andExpect(jsonPath("$[0].title").value("Setup development environment"))
                .andExpect(jsonPath("$[0].taskOrder").value(1))
                .andExpect(jsonPath("$[0].ownerRole").value("USER"))
                .andExpect(jsonPath("$[0].isCompleted").value(false))
                .andExpect(jsonPath("$[1].id").value(taskId2.toString()))
                .andExpect(jsonPath("$[1].title").value("Introduction to team"))
                .andExpect(jsonPath("$[1].taskOrder").value(2))
                .andExpect(jsonPath("$[1].ownerRole").value("MANAGER"));
        
        verify(taskService).getTasksByProcessId(processId);
    }
    
    @Test
    @DisplayName("GET /onboarding/{processId}/tasks - should return 404 when process not found")
    void shouldReturn404WhenProcessNotFound() throws Exception {
        // Arrange
        when(taskService.getTasksByProcessId(processId))
                .thenThrow(new OnboardingProcessNotFoundException(processId));
        
        // Act & Assert
        mockMvc.perform(get("/onboarding/{processId}/tasks", processId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString(processId.toString())));
        
        verify(taskService).getTasksByProcessId(processId);
    }
    
    @Test
    @DisplayName("GET /onboarding/{processId}/tasks - should return empty array when no tasks")
    void shouldReturnEmptyArrayWhenNoTasks() throws Exception {
        // Arrange
        when(taskService.getTasksByProcessId(processId)).thenReturn(Arrays.asList());
        
        // Act & Assert
        mockMvc.perform(get("/onboarding/{processId}/tasks", processId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
        
        verify(taskService).getTasksByProcessId(processId);
    }
    
    @Test
    @DisplayName("GET /onboarding/{processId}/tasks/{taskId} - should return task details")
    void shouldGetTaskById() throws Exception {
        // Arrange
        when(taskService.getTaskById(processId, taskId1)).thenReturn(taskResponseDto1);
        
        // Act & Assert
        mockMvc.perform(get("/onboarding/{processId}/tasks/{taskId}", processId, taskId1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId1.toString()))
                .andExpect(jsonPath("$.onboardingProcessId").value(processId.toString()))
                .andExpect(jsonPath("$.title").value("Setup development environment"))
                .andExpect(jsonPath("$.description").value("Install IDE, configure git, setup local database"))
                .andExpect(jsonPath("$.taskOrder").value(1))
                .andExpect(jsonPath("$.ownerRole").value("USER"))
                .andExpect(jsonPath("$.isCompleted").value(false));
        
        verify(taskService).getTaskById(processId, taskId1);
    }
    
    @Test
    @DisplayName("GET /onboarding/{processId}/tasks/{taskId} - should return 404 when task not found")
    void shouldReturn404WhenTaskNotFound() throws Exception {
        // Arrange
        when(taskService.getTaskById(processId, taskId1))
                .thenThrow(new OnboardingTaskNotFoundException(taskId1, processId));
        
        // Act & Assert
        mockMvc.perform(get("/onboarding/{processId}/tasks/{taskId}", processId, taskId1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString(taskId1.toString())))
                .andExpect(jsonPath("$.message").value(containsString(processId.toString())));
        
        verify(taskService).getTaskById(processId, taskId1);
    }
    
    @Test
    @DisplayName("PUT /onboarding/{processId}/tasks/{taskId} - should update task")
    void shouldUpdateTask() throws Exception {
        // Arrange
        OnboardingTaskResponseDto completedTask = new OnboardingTaskResponseDto(
                taskId1, processId, "Setup development environment",
                "Install IDE, configure git, setup local database",
                1, TaskOwnerRole.USER, true,
                OffsetDateTime.now(), OffsetDateTime.now()
        );
        
        when(taskService.updateTask(eq(processId), eq(taskId1), any(UpdateOnboardingTaskRequestDto.class)))
                .thenReturn(completedTask);
        
        // Act & Assert
        mockMvc.perform(put("/onboarding/{processId}/tasks/{taskId}", processId, taskId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTaskDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId1.toString()))
                .andExpect(jsonPath("$.isCompleted").value(true));
        
        verify(taskService).updateTask(eq(processId), eq(taskId1), any(UpdateOnboardingTaskRequestDto.class));
    }
    
    @Test
    @DisplayName("PUT /onboarding/{processId}/tasks/{taskId} - should mark task as incomplete")
    void shouldMarkTaskAsIncomplete() throws Exception {
        // Arrange
        UpdateOnboardingTaskRequestDto incompleteDto = new UpdateOnboardingTaskRequestDto(false);
        OnboardingTaskResponseDto incompleteTask = new OnboardingTaskResponseDto(
                taskId1, processId, "Setup development environment",
                "Install IDE, configure git, setup local database",
                1, TaskOwnerRole.USER, false,
                OffsetDateTime.now(), OffsetDateTime.now()
        );
        
        when(taskService.updateTask(eq(processId), eq(taskId1), any(UpdateOnboardingTaskRequestDto.class)))
                .thenReturn(incompleteTask);
        
        // Act & Assert
        mockMvc.perform(put("/onboarding/{processId}/tasks/{taskId}", processId, taskId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incompleteDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCompleted").value(false));
        
        verify(taskService).updateTask(eq(processId), eq(taskId1), any(UpdateOnboardingTaskRequestDto.class));
    }
    
    @Test
    @DisplayName("PUT /onboarding/{processId}/tasks/{taskId} - should return 404 when task not found")
    void shouldReturn404WhenUpdatingNonExistentTask() throws Exception {
        // Arrange
        when(taskService.updateTask(eq(processId), eq(taskId1), any(UpdateOnboardingTaskRequestDto.class)))
                .thenThrow(new OnboardingTaskNotFoundException(taskId1, processId));
        
        // Act & Assert
        mockMvc.perform(put("/onboarding/{processId}/tasks/{taskId}", processId, taskId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTaskDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString(taskId1.toString())));
        
        verify(taskService).updateTask(eq(processId), eq(taskId1), any(UpdateOnboardingTaskRequestDto.class));
    }
    
    @Test
    @DisplayName("GET /onboarding/{processId}/tasks - should use correct processId for different processes")
    void shouldUseCorrectProcessIdForDifferentProcesses() throws Exception {
        // Arrange
        UUID anotherProcessId = UUID.randomUUID();
        List<OnboardingTaskResponseDto> tasks = Arrays.asList(taskResponseDto1);
        
        when(taskService.getTasksByProcessId(anotherProcessId)).thenReturn(tasks);
        
        // Act & Assert
        mockMvc.perform(get("/onboarding/{processId}/tasks", anotherProcessId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        verify(taskService).getTasksByProcessId(anotherProcessId);
        verify(taskService, never()).getTasksByProcessId(processId);
    }
    
    @Test
    @DisplayName("GET /onboarding/{processId}/tasks - should handle tasks with all owner roles")
    void shouldHandleTasksWithAllOwnerRoles() throws Exception {
        // Arrange
        OnboardingTaskResponseDto userTask = new OnboardingTaskResponseDto(
                taskId1, processId, "User Task", "Description", 1,
                TaskOwnerRole.USER, false, OffsetDateTime.now(), OffsetDateTime.now()
        );
        
        OnboardingTaskResponseDto managerTask = new OnboardingTaskResponseDto(
                taskId2, processId, "Manager Task", "Description", 2,
                TaskOwnerRole.MANAGER, false, OffsetDateTime.now(), OffsetDateTime.now()
        );
        
        List<OnboardingTaskResponseDto> tasks = Arrays.asList(userTask, managerTask);
        when(taskService.getTasksByProcessId(processId)).thenReturn(tasks);
        
        // Act & Assert
        mockMvc.perform(get("/onboarding/{processId}/tasks", processId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ownerRole").value("USER"))
                .andExpect(jsonPath("$[1].ownerRole").value("MANAGER"));
        
        verify(taskService).getTasksByProcessId(processId);
    }
    
    @Test
    @DisplayName("GET /onboarding/{processId}/tasks - should return tasks with proper timestamps")
    void shouldReturnTasksWithTimestamps() throws Exception {
        // Arrange
        when(taskService.getTasksByProcessId(processId)).thenReturn(Arrays.asList(taskResponseDto1));
        
        // Act & Assert
        mockMvc.perform(get("/onboarding/{processId}/tasks", processId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].updatedAt").exists());
        
        verify(taskService).getTasksByProcessId(processId);
    }
}

