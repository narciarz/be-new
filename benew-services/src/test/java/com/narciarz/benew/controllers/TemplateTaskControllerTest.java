package com.narciarz.benew.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.narciarz.benew.exceptions.TemplateNotFoundException;
import com.narciarz.benew.exceptions.TemplateTaskNotFoundException;
import com.narciarz.benew.models.TaskOwnerRole;
import com.narciarz.benew.models.dto.CreateTemplateTaskRequestDto;
import com.narciarz.benew.models.dto.UpdateTemplateTaskRequestDto;
import com.narciarz.benew.models.dto.TemplateTaskResponseDto;
import com.narciarz.benew.services.TemplateService;
import com.narciarz.benew.services.TemplateTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TemplateController template task endpoints using @WebMvcTest.
 * 
 * <p>Tests the web layer (controller) in isolation with mocked service layer.
 * Uses MockMvc to perform HTTP requests and verify responses.</p>
 */
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(TemplateController.class)
@DisplayName("TemplateController Template Task Integration Tests")
class TemplateTaskControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private TemplateService templateService;
    
    @MockitoBean
    private TemplateTaskService templateTaskService;
    
    private UUID testTemplateId;
    private UUID testTaskId;
    private TemplateTaskResponseDto taskResponseDto;
    
    @BeforeEach
    void setUp() {
        testTemplateId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        testTaskId = UUID.fromString("660e8400-e29b-41d4-a716-446655440000");
        
        taskResponseDto = createTaskResponseDto(
                testTaskId,
                testTemplateId,
                "Complete paperwork",
                "Fill out all HR forms",
                1,
                TaskOwnerRole.USER
        );
    }
    
    // ========== GET /api/templates/{templateId}/tasks Tests ==========
    
    @Test
    @DisplayName("GET /api/templates/{templateId}/tasks - should return tasks when template exists")
    void getTasksForTemplate_ShouldReturnTasks_WhenTemplateExists() throws Exception {
        // Arrange
        TemplateTaskResponseDto task2 = createTaskResponseDto(
                UUID.randomUUID(), 
                testTemplateId, 
                "Setup workstation", 
                "Configure laptop and accounts",
                2,
                TaskOwnerRole.MANAGER
        );
        
        List<TemplateTaskResponseDto> tasks = Arrays.asList(taskResponseDto, task2);
        when(templateTaskService.getAllTasksForTemplate(testTemplateId)).thenReturn(tasks);
        
        // Act & Assert
        mockMvc.perform(get("/api/templates/{templateId}/tasks", testTemplateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(testTaskId.toString()))
                .andExpect(jsonPath("$[0].title").value("Complete paperwork"))
                .andExpect(jsonPath("$[0].taskOrder").value(1))
                .andExpect(jsonPath("$[0].ownerRole").value("USER"))
                .andExpect(jsonPath("$[1].title").value("Setup workstation"))
                .andExpect(jsonPath("$[1].taskOrder").value(2))
                .andExpect(jsonPath("$[1].ownerRole").value("MANAGER"));
        
        verify(templateTaskService).getAllTasksForTemplate(testTemplateId);
    }
    
    @Test
    @DisplayName("GET /api/templates/{templateId}/tasks - should return empty list when template has no tasks")
    void getTasksForTemplate_ShouldReturnEmptyList_WhenTemplateHasNoTasks() throws Exception {
        // Arrange
        when(templateTaskService.getAllTasksForTemplate(testTemplateId))
                .thenReturn(Arrays.asList());
        
        // Act & Assert
        mockMvc.perform(get("/api/templates/{templateId}/tasks", testTemplateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        
        verify(templateTaskService).getAllTasksForTemplate(testTemplateId);
    }
    
    @Test
    @DisplayName("GET /api/templates/{templateId}/tasks - should return 404 when template not found")
    void getTasksForTemplate_ShouldReturn404_WhenTemplateNotFound() throws Exception {
        // Arrange
        when(templateTaskService.getAllTasksForTemplate(testTemplateId))
                .thenThrow(new TemplateNotFoundException(testTemplateId));
        
        // Act & Assert
        mockMvc.perform(get("/api/templates/{templateId}/tasks", testTemplateId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
        
        verify(templateTaskService).getAllTasksForTemplate(testTemplateId);
    }
    
    // ========== POST /api/templates/{templateId}/tasks Tests ==========
    
    @Test
    @DisplayName("POST /api/templates/{templateId}/tasks - should create task when valid request")
    void createTask_ShouldCreateSuccessfully() throws Exception {
        // Arrange
        CreateTemplateTaskRequestDto request = new CreateTemplateTaskRequestDto(
                "Complete paperwork",
                "Fill out all HR forms",
                1,
                TaskOwnerRole.USER
        );
        
        when(templateTaskService.createTask(eq(testTemplateId), any(CreateTemplateTaskRequestDto.class)))
                .thenReturn(taskResponseDto);
        
        // Act & Assert
        mockMvc.perform(post("/api/templates/{templateId}/tasks", testTemplateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(testTaskId.toString()))
                .andExpect(jsonPath("$.templateId").value(testTemplateId.toString()))
                .andExpect(jsonPath("$.title").value("Complete paperwork"))
                .andExpect(jsonPath("$.description").value("Fill out all HR forms"))
                .andExpect(jsonPath("$.taskOrder").value(1))
                .andExpect(jsonPath("$.ownerRole").value("USER"));
        
        verify(templateTaskService).createTask(eq(testTemplateId), any(CreateTemplateTaskRequestDto.class));
    }
    
    @Test
    @DisplayName("POST /api/templates/{templateId}/tasks - should return 400 when missing required fields")
    void createTask_ShouldReturn400_ForMissingRequiredFields() throws Exception {
        // Arrange
        String invalidRequest = """
                {
                    "description": "Some description"
                }
                """;
        
        // Act & Assert
        mockMvc.perform(post("/api/templates/{templateId}/tasks", testTemplateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        
        verify(templateTaskService, never()).createTask(any(), any());
    }
    
    @Test
    @DisplayName("POST /api/templates/{templateId}/tasks - should return 400 when title is blank")
    void createTask_ShouldReturn400_ForBlankTitle() throws Exception {
        // Arrange
        CreateTemplateTaskRequestDto request = new CreateTemplateTaskRequestDto(
                "   ",  // blank title
                "Description",
                1,
                TaskOwnerRole.USER
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/templates/{templateId}/tasks", testTemplateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        
        verify(templateTaskService, never()).createTask(any(), any());
    }
    
    @Test
    @DisplayName("POST /api/templates/{templateId}/tasks - should return 404 when template not found")
    void createTask_ShouldReturn404_WhenTemplateNotFound() throws Exception {
        // Arrange
        CreateTemplateTaskRequestDto request = new CreateTemplateTaskRequestDto(
                "Complete paperwork",
                "Fill out all HR forms",
                1,
                TaskOwnerRole.USER
        );
        
        when(templateTaskService.createTask(eq(testTemplateId), any(CreateTemplateTaskRequestDto.class)))
                .thenThrow(new TemplateNotFoundException(testTemplateId));
        
        // Act & Assert
        mockMvc.perform(post("/api/templates/{templateId}/tasks", testTemplateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        
        verify(templateTaskService).createTask(eq(testTemplateId), any(CreateTemplateTaskRequestDto.class));
    }
    
    // ========== PUT /api/templates/{templateId}/tasks/{taskId} Tests ==========
    
    @Test
    @DisplayName("PUT /api/templates/{templateId}/tasks/{taskId} - should update task when valid request")
    void updateTask_ShouldUpdateSuccessfully() throws Exception {
        // Arrange
        UpdateTemplateTaskRequestDto request = new UpdateTemplateTaskRequestDto(
                "Updated title",
                "Updated description",
                2,
                TaskOwnerRole.MANAGER
        );
        
        TemplateTaskResponseDto updatedTask = createTaskResponseDto(
                testTaskId,
                testTemplateId,
                "Updated title",
                "Updated description",
                2,
                TaskOwnerRole.MANAGER
        );
        
        when(templateTaskService.updateTask(
                eq(testTemplateId), 
                eq(testTaskId), 
                any(UpdateTemplateTaskRequestDto.class)
        )).thenReturn(updatedTask);
        
        // Act & Assert
        mockMvc.perform(put("/api/templates/{templateId}/tasks/{taskId}", testTemplateId, testTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTaskId.toString()))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.taskOrder").value(2))
                .andExpect(jsonPath("$.ownerRole").value("MANAGER"));
        
        verify(templateTaskService).updateTask(eq(testTemplateId), eq(testTaskId), any(UpdateTemplateTaskRequestDto.class));
    }
    
    @Test
    @DisplayName("PUT /api/templates/{templateId}/tasks/{taskId} - should support partial update")
    void updateTask_ShouldSupportPartialUpdate() throws Exception {
        // Arrange
        UpdateTemplateTaskRequestDto request = new UpdateTemplateTaskRequestDto();
        request.setTitle("Only title updated");
        
        TemplateTaskResponseDto partialUpdate = createTaskResponseDto(
                testTaskId,
                testTemplateId,
                "Only title updated",
                "Original description",
                1,
                TaskOwnerRole.USER
        );
        
        when(templateTaskService.updateTask(
                eq(testTemplateId), 
                eq(testTaskId), 
                any(UpdateTemplateTaskRequestDto.class)
        )).thenReturn(partialUpdate);
        
        // Act & Assert
        mockMvc.perform(put("/api/templates/{templateId}/tasks/{taskId}", testTemplateId, testTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Only title updated"));
        
        verify(templateTaskService).updateTask(eq(testTemplateId), eq(testTaskId), any(UpdateTemplateTaskRequestDto.class));
    }
    
    @Test
    @DisplayName("PUT /api/templates/{templateId}/tasks/{taskId} - should return 404 when task not found")
    void updateTask_ShouldReturn404_WhenTaskNotFound() throws Exception {
        // Arrange
        UpdateTemplateTaskRequestDto request = new UpdateTemplateTaskRequestDto();
        request.setTitle("Updated title");
        
        when(templateTaskService.updateTask(
                eq(testTemplateId), 
                eq(testTaskId), 
                any(UpdateTemplateTaskRequestDto.class)
        )).thenThrow(new TemplateTaskNotFoundException(testTaskId, testTemplateId));
        
        // Act & Assert
        mockMvc.perform(put("/api/templates/{templateId}/tasks/{taskId}", testTemplateId, testTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        
        verify(templateTaskService).updateTask(eq(testTemplateId), eq(testTaskId), any(UpdateTemplateTaskRequestDto.class));
    }
    
    // ========== DELETE /api/templates/{templateId}/tasks/{taskId} Tests ==========
    
    @Test
    @DisplayName("DELETE /api/templates/{templateId}/tasks/{taskId} - should delete task successfully")
    void deleteTask_ShouldDeleteSuccessfully() throws Exception {
        // Arrange - service method returns successfully (void)
        
        // Act & Assert
        mockMvc.perform(delete("/api/templates/{templateId}/tasks/{taskId}", testTemplateId, testTaskId))
                .andExpect(status().isNoContent());
        
        verify(templateTaskService).deleteTask(testTemplateId, testTaskId);
    }
    
    @Test
    @DisplayName("DELETE /api/templates/{templateId}/tasks/{taskId} - should return 404 when task not found")
    void deleteTask_ShouldReturn404_WhenTaskNotFound() throws Exception {
        // Arrange
        doThrow(new TemplateTaskNotFoundException(testTaskId, testTemplateId))
                .when(templateTaskService).deleteTask(testTemplateId, testTaskId);
        
        // Act & Assert
        mockMvc.perform(delete("/api/templates/{templateId}/tasks/{taskId}", testTemplateId, testTaskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        
        verify(templateTaskService).deleteTask(testTemplateId, testTaskId);
    }
    
    @Test
    @DisplayName("DELETE /api/templates/{templateId}/tasks/{taskId} - should return 404 when template not found")
    void deleteTask_ShouldReturn404_WhenTemplateNotFound() throws Exception {
        // Arrange
        doThrow(new TemplateNotFoundException(testTemplateId))
                .when(templateTaskService).deleteTask(testTemplateId, testTaskId);
        
        // Act & Assert
        mockMvc.perform(delete("/api/templates/{templateId}/tasks/{taskId}", testTemplateId, testTaskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        
        verify(templateTaskService).deleteTask(testTemplateId, testTaskId);
    }
    
    // ========== Helper Methods ==========
    
    private TemplateTaskResponseDto createTaskResponseDto(
            UUID id, 
            UUID templateId, 
            String title, 
            String description,
            Integer taskOrder,
            TaskOwnerRole ownerRole) {
        
        return new TemplateTaskResponseDto(
                id,
                templateId,
                title,
                description,
                taskOrder,
                ownerRole,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }
}

