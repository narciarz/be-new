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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer slice test for template task endpoints in {@link TemplateController}.
 * 
 * <p>Uses {@code @WebMvcTest} to test only the web layer (controller) in isolation
 * without loading the full application context. Service dependencies are mocked
 * with {@code @MockBean}.</p>
 * 
 * <p>Tests cover:</p>
 * <ul>
 *   <li>GET /api/templates/{templateId}/tasks - retrieve tasks</li>
 *   <li>POST /api/templates/{templateId}/tasks - create task</li>
 *   <li>PUT /api/templates/{templateId}/tasks/{taskId} - update task</li>
 *   <li>DELETE /api/templates/{templateId}/tasks/{taskId} - delete task</li>
 *   <li>Validation and error handling scenarios</li>
 * </ul>
 */
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(TemplateController.class)
class TemplateTaskControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private TemplateService templateService;
    
    @MockitoBean
    private TemplateTaskService templateTaskService;
    
    private static final UUID TEMPLATE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID TASK_ID = UUID.fromString("660e8400-e29b-41d4-a716-446655440000");
    
    // ==================== GET /api/templates/{templateId}/tasks ====================
    
    @Test
    void shouldReturnTasksWhenTemplateExists() throws Exception {
        // Given
        TemplateTaskResponseDto task1 = createTaskResponseDto(
                TASK_ID, 
                TEMPLATE_ID, 
                "Complete paperwork", 
                "Fill out all HR forms",
                1,
                TaskOwnerRole.USER
        );
        
        TemplateTaskResponseDto task2 = createTaskResponseDto(
                UUID.randomUUID(), 
                TEMPLATE_ID, 
                "Setup workstation", 
                "Configure laptop and accounts",
                2,
                TaskOwnerRole.MANAGER
        );
        
        List<TemplateTaskResponseDto> tasks = Arrays.asList(task1, task2);
        when(templateTaskService.getAllTasksForTemplate(TEMPLATE_ID)).thenReturn(tasks);
        
        // When & Then
        mockMvc.perform(get("/api/templates/{templateId}/tasks", TEMPLATE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(TASK_ID.toString()))
                .andExpect(jsonPath("$[0].title").value("Complete paperwork"))
                .andExpect(jsonPath("$[0].taskOrder").value(1))
                .andExpect(jsonPath("$[0].ownerRole").value("USER"))
                .andExpect(jsonPath("$[1].title").value("Setup workstation"))
                .andExpect(jsonPath("$[1].taskOrder").value(2))
                .andExpect(jsonPath("$[1].ownerRole").value("MANAGER"));
    }
    
    @Test
    void shouldReturnEmptyListWhenTemplateHasNoTasks() throws Exception {
        // Given
        when(templateTaskService.getAllTasksForTemplate(TEMPLATE_ID))
                .thenReturn(Arrays.asList());
        
        // When & Then
        mockMvc.perform(get("/api/templates/{templateId}/tasks", TEMPLATE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
    
    @Test
    void shouldReturn404WhenTemplateNotFoundForGetTasks() throws Exception {
        // Given
        when(templateTaskService.getAllTasksForTemplate(TEMPLATE_ID))
                .thenThrow(new TemplateNotFoundException(TEMPLATE_ID));
        
        // When & Then
        mockMvc.perform(get("/api/templates/{templateId}/tasks", TEMPLATE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
    
    // ==================== POST /api/templates/{templateId}/tasks ====================
    
    @Test
    void shouldCreateTaskWhenValidRequest() throws Exception {
        // Given
        CreateTemplateTaskRequestDto request = new CreateTemplateTaskRequestDto(
                "Complete paperwork",
                "Fill out all HR forms",
                1,
                TaskOwnerRole.USER
        );
        
        TemplateTaskResponseDto response = createTaskResponseDto(
                TASK_ID, 
                TEMPLATE_ID, 
                request.getTitle(),
                request.getDescription(),
                request.getTaskOrder(),
                request.getOwnerRole()
        );
        
        when(templateTaskService.createTask(eq(TEMPLATE_ID), any(CreateTemplateTaskRequestDto.class)))
                .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/templates/{templateId}/tasks", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(TASK_ID.toString()))
                .andExpect(jsonPath("$.templateId").value(TEMPLATE_ID.toString()))
                .andExpect(jsonPath("$.title").value("Complete paperwork"))
                .andExpect(jsonPath("$.description").value("Fill out all HR forms"))
                .andExpect(jsonPath("$.taskOrder").value(1))
                .andExpect(jsonPath("$.ownerRole").value("USER"));
    }
    
    @Test
    void shouldReturn400WhenCreateTaskWithMissingRequiredFields() throws Exception {
        // Given - missing title, taskOrder, and ownerRole
        String invalidRequest = """
                {
                    "description": "Some description"
                }
                """;
        
        // When & Then
        mockMvc.perform(post("/api/templates/{templateId}/tasks", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
    
    @Test
    void shouldReturn400WhenCreateTaskWithBlankTitle() throws Exception {
        // Given
        CreateTemplateTaskRequestDto request = new CreateTemplateTaskRequestDto(
                "   ",  // blank title
                "Description",
                1,
                TaskOwnerRole.USER
        );
        
        // When & Then
        mockMvc.perform(post("/api/templates/{templateId}/tasks", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
    
    @Test
    void shouldReturn404WhenCreateTaskForNonExistentTemplate() throws Exception {
        // Given
        CreateTemplateTaskRequestDto request = new CreateTemplateTaskRequestDto(
                "Complete paperwork",
                "Fill out all HR forms",
                1,
                TaskOwnerRole.USER
        );
        
        when(templateTaskService.createTask(eq(TEMPLATE_ID), any(CreateTemplateTaskRequestDto.class)))
                .thenThrow(new TemplateNotFoundException(TEMPLATE_ID));
        
        // When & Then
        mockMvc.perform(post("/api/templates/{templateId}/tasks", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
    
    // ==================== PUT /api/templates/{templateId}/tasks/{taskId} ====================
    
    @Test
    void shouldUpdateTaskWhenValidRequest() throws Exception {
        // Given
        UpdateTemplateTaskRequestDto request = new UpdateTemplateTaskRequestDto(
                "Updated title",
                "Updated description",
                2,
                TaskOwnerRole.MANAGER
        );
        
        TemplateTaskResponseDto response = createTaskResponseDto(
                TASK_ID,
                TEMPLATE_ID,
                request.getTitle(),
                request.getDescription(),
                request.getTaskOrder(),
                request.getOwnerRole()
        );
        
        when(templateTaskService.updateTask(
                eq(TEMPLATE_ID), 
                eq(TASK_ID), 
                any(UpdateTemplateTaskRequestDto.class)
        )).thenReturn(response);
        
        // When & Then
        mockMvc.perform(put("/api/templates/{templateId}/tasks/{taskId}", TEMPLATE_ID, TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TASK_ID.toString()))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.taskOrder").value(2))
                .andExpect(jsonPath("$.ownerRole").value("MANAGER"));
    }
    
    @Test
    void shouldSupportPartialUpdateForTask() throws Exception {
        // Given - only updating title
        UpdateTemplateTaskRequestDto request = new UpdateTemplateTaskRequestDto();
        request.setTitle("Only title updated");
        
        TemplateTaskResponseDto response = createTaskResponseDto(
                TASK_ID,
                TEMPLATE_ID,
                "Only title updated",
                "Original description",
                1,
                TaskOwnerRole.USER
        );
        
        when(templateTaskService.updateTask(
                eq(TEMPLATE_ID), 
                eq(TASK_ID), 
                any(UpdateTemplateTaskRequestDto.class)
        )).thenReturn(response);
        
        // When & Then
        mockMvc.perform(put("/api/templates/{templateId}/tasks/{taskId}", TEMPLATE_ID, TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Only title updated"));
    }
    
    @Test
    void shouldReturn404WhenUpdateNonExistentTask() throws Exception {
        // Given
        UpdateTemplateTaskRequestDto request = new UpdateTemplateTaskRequestDto();
        request.setTitle("Updated title");
        
        when(templateTaskService.updateTask(
                eq(TEMPLATE_ID), 
                eq(TASK_ID), 
                any(UpdateTemplateTaskRequestDto.class)
        )).thenThrow(new TemplateTaskNotFoundException(TASK_ID, TEMPLATE_ID));
        
        // When & Then
        mockMvc.perform(put("/api/templates/{templateId}/tasks/{taskId}", TEMPLATE_ID, TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
    
    // ==================== DELETE /api/templates/{templateId}/tasks/{taskId} ====================
    
    @Test
    void shouldDeleteTaskWhenExists() throws Exception {
        // Given - service method returns successfully (void)
        
        // When & Then
        mockMvc.perform(delete("/api/templates/{templateId}/tasks/{taskId}", TEMPLATE_ID, TASK_ID))
                .andExpect(status().isNoContent());
    }
    
    @Test
    void shouldReturn404WhenDeleteNonExistentTask() throws Exception {
        // Given
        doThrow(new TemplateTaskNotFoundException(TASK_ID, TEMPLATE_ID))
                .when(templateTaskService).deleteTask(TEMPLATE_ID, TASK_ID);
        
        // When & Then
        mockMvc.perform(delete("/api/templates/{templateId}/tasks/{taskId}", TEMPLATE_ID, TASK_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
    
    @Test
    void shouldReturn404WhenDeleteTaskFromNonExistentTemplate() throws Exception {
        // Given
        doThrow(new TemplateNotFoundException(TEMPLATE_ID))
                .when(templateTaskService).deleteTask(TEMPLATE_ID, TASK_ID);
        
        // When & Then
        mockMvc.perform(delete("/api/templates/{templateId}/tasks/{taskId}", TEMPLATE_ID, TASK_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
    
    // ==================== Helper Methods ====================
    
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

