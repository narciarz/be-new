package com.narciarz.benew.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.narciarz.benew.exceptions.CsvImportException;
import com.narciarz.benew.exceptions.DuplicatePositionNameException;
import com.narciarz.benew.exceptions.TemplateDeletionException;
import com.narciarz.benew.exceptions.TemplateNotFoundException;
import com.narciarz.benew.models.dto.CreateTemplateRequestDto;
import com.narciarz.benew.models.dto.UpdateTemplateRequestDto;
import com.narciarz.benew.models.dto.TemplateResponseDto;
import com.narciarz.benew.models.dto.TemplateImportResponseDto;
import com.narciarz.benew.services.TemplateService;
import com.narciarz.benew.services.TemplateTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TemplateController using @WebMvcTest.
 * 
 * <p>Tests the web layer (controller) in isolation with mocked service layer.
 * Uses MockMvc to perform HTTP requests and verify responses.</p>
 */
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(TemplateController.class)
@DisplayName("TemplateController Integration Tests")
class TemplateControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private TemplateService templateService;
    @MockitoBean
    private TemplateTaskService templateTaskService;

    private TemplateResponseDto templateResponseDto;
    private CreateTemplateRequestDto createTemplateDto;
    private UpdateTemplateRequestDto updateTemplateDto;
    private UUID testTemplateId;
    
    @BeforeEach
    void setUp() {
        testTemplateId = UUID.randomUUID();
        
        templateResponseDto = new TemplateResponseDto(
                testTemplateId,
                "software engineer",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
        
        createTemplateDto = new CreateTemplateRequestDto("Software Engineer");
        
        updateTemplateDto = new UpdateTemplateRequestDto("Senior Software Engineer");
    }
    
    // ========== GET /api/templates Tests ==========
    
    @Test
    @DisplayName("GET /api/templates - should return paginated templates")
    void getAllTemplates_ShouldReturnPaginatedTemplates() throws Exception {
        // Arrange
        Page<TemplateResponseDto> templatePage = new PageImpl<>(List.of(templateResponseDto));
        when(templateService.getAllTemplates(any(Pageable.class))).thenReturn(templatePage);
        
        // Act & Assert
        mockMvc.perform(get("/api/templates")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(testTemplateId.toString()))
                .andExpect(jsonPath("$.content[0].positionName").value("software engineer"));
        
        verify(templateService).getAllTemplates(any(Pageable.class));
    }
    
    @Test
    @DisplayName("GET /api/templates?positionName=engineer - should filter by position name")
    void getAllTemplates_ShouldFilterByPositionName() throws Exception {
        // Arrange
        Page<TemplateResponseDto> templatePage = new PageImpl<>(List.of(templateResponseDto));
        when(templateService.getTemplatesByPositionName(eq("engineer"), any(Pageable.class)))
                .thenReturn(templatePage);
        
        // Act & Assert
        mockMvc.perform(get("/api/templates")
                        .param("positionName", "engineer")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
        
        verify(templateService).getTemplatesByPositionName(eq("engineer"), any(Pageable.class));
        verify(templateService, never()).getAllTemplates(any(Pageable.class));
    }
    
    @Test
    @DisplayName("GET /api/templates?positionName= - should return all when filter is blank")
    void getAllTemplates_ShouldReturnAll_WhenFilterIsBlank() throws Exception {
        // Arrange
        Page<TemplateResponseDto> templatePage = new PageImpl<>(List.of(templateResponseDto));
        when(templateService.getAllTemplates(any(Pageable.class))).thenReturn(templatePage);
        
        // Act & Assert
        mockMvc.perform(get("/api/templates")
                        .param("positionName", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        verify(templateService).getAllTemplates(any(Pageable.class));
        verify(templateService, never()).getTemplatesByPositionName(anyString(), any(Pageable.class));
    }
    
    // ========== GET /api/templates/{templateId} Tests ==========
    
    @Test
    @DisplayName("GET /api/templates/{templateId} - should return template when found")
    void getTemplateById_ShouldReturnTemplate_WhenExists() throws Exception {
        // Arrange
        when(templateService.getTemplateById(testTemplateId)).thenReturn(templateResponseDto);
        
        // Act & Assert
        mockMvc.perform(get("/api/templates/{templateId}", testTemplateId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTemplateId.toString()))
                .andExpect(jsonPath("$.positionName").value("software engineer"));
        
        verify(templateService).getTemplateById(testTemplateId);
    }
    
    @Test
    @DisplayName("GET /api/templates/{templateId} - should return 404 when template not found")
    void getTemplateById_ShouldReturn404_WhenNotFound() throws Exception {
        // Arrange
        when(templateService.getTemplateById(testTemplateId))
                .thenThrow(new TemplateNotFoundException(testTemplateId));
        
        // Act & Assert
        mockMvc.perform(get("/api/templates/{templateId}", testTemplateId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(containsString(testTemplateId.toString())));
        
        verify(templateService).getTemplateById(testTemplateId);
    }
    
    // ========== POST /api/templates Tests ==========
    
    @Test
    @DisplayName("POST /api/templates - should create template successfully")
    void createTemplate_ShouldCreateSuccessfully() throws Exception {
        // Arrange
        when(templateService.createTemplate(any(CreateTemplateRequestDto.class)))
                .thenReturn(templateResponseDto);
        
        // Act & Assert
        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTemplateDto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/api/templates/" + testTemplateId)))
                .andExpect(jsonPath("$.id").value(testTemplateId.toString()))
                .andExpect(jsonPath("$.positionName").value("software engineer"));
        
        verify(templateService).createTemplate(any(CreateTemplateRequestDto.class));
    }
    
    @Test
    @DisplayName("POST /api/templates - should return 400 for blank position name")
    void createTemplate_ShouldReturn400_ForBlankPositionName() throws Exception {
        // Arrange
        CreateTemplateRequestDto invalidDto = new CreateTemplateRequestDto("");
        
        // Act & Assert
        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[*].field", hasItem("positionName")));
        
        verify(templateService, never()).createTemplate(any());
    }
    
    @Test
    @DisplayName("POST /api/templates - should return 400 for null position name")
    void createTemplate_ShouldReturn400_ForNullPositionName() throws Exception {
        // Arrange
        CreateTemplateRequestDto invalidDto = new CreateTemplateRequestDto(null);
        
        // Act & Assert
        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[*].field", hasItem("positionName")));
        
        verify(templateService, never()).createTemplate(any());
    }
    
    @Test
    @DisplayName("POST /api/templates - should return 400 for position name exceeding 50 characters")
    void createTemplate_ShouldReturn400_ForTooLongPositionName() throws Exception {
        // Arrange
        String longPositionName = "A".repeat(51);
        CreateTemplateRequestDto invalidDto = new CreateTemplateRequestDto(longPositionName);
        
        // Act & Assert
        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[*].field", hasItem("positionName")));
        
        verify(templateService, never()).createTemplate(any());
    }
    
    @Test
    @DisplayName("POST /api/templates - should return 400 when position name exists")
    void createTemplate_ShouldReturn400_WhenPositionNameExists() throws Exception {
        // Arrange
        when(templateService.createTemplate(any(CreateTemplateRequestDto.class)))
                .thenThrow(new DuplicatePositionNameException("software engineer"));
        
        // Act & Assert
        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTemplateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("software engineer")));
        
        verify(templateService).createTemplate(any(CreateTemplateRequestDto.class));
    }
    
    // ========== PUT /api/templates/{templateId} Tests ==========
    
    @Test
    @DisplayName("PUT /api/templates/{templateId} - should update template successfully")
    void updateTemplate_ShouldUpdateSuccessfully() throws Exception {
        // Arrange
        when(templateService.updateTemplate(eq(testTemplateId), any(UpdateTemplateRequestDto.class)))
                .thenReturn(templateResponseDto);
        
        // Act & Assert
        mockMvc.perform(put("/api/templates/{templateId}", testTemplateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTemplateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTemplateId.toString()));
        
        verify(templateService).updateTemplate(eq(testTemplateId), any(UpdateTemplateRequestDto.class));
    }
    
    @Test
    @DisplayName("PUT /api/templates/{templateId} - should return 404 when template not found")
    void updateTemplate_ShouldReturn404_WhenNotFound() throws Exception {
        // Arrange
        when(templateService.updateTemplate(eq(testTemplateId), any(UpdateTemplateRequestDto.class)))
                .thenThrow(new TemplateNotFoundException(testTemplateId));
        
        // Act & Assert
        mockMvc.perform(put("/api/templates/{templateId}", testTemplateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTemplateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        
        verify(templateService).updateTemplate(eq(testTemplateId), any(UpdateTemplateRequestDto.class));
    }
    
    @Test
    @DisplayName("PUT /api/templates/{templateId} - should return 400 when new position name exists")
    void updateTemplate_ShouldReturn400_WhenNewPositionNameExists() throws Exception {
        // Arrange
        when(templateService.updateTemplate(eq(testTemplateId), any(UpdateTemplateRequestDto.class)))
                .thenThrow(new DuplicatePositionNameException("senior software engineer"));
        
        // Act & Assert
        mockMvc.perform(put("/api/templates/{templateId}", testTemplateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTemplateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("senior software engineer")));
        
        verify(templateService).updateTemplate(eq(testTemplateId), any(UpdateTemplateRequestDto.class));
    }
    
    @Test
    @DisplayName("PUT /api/templates/{templateId} - should return 400 for position name exceeding 50 characters")
    void updateTemplate_ShouldReturn400_ForTooLongPositionName() throws Exception {
        // Arrange
        String longPositionName = "A".repeat(51);
        UpdateTemplateRequestDto invalidDto = new UpdateTemplateRequestDto(longPositionName);
        
        // Act & Assert
        mockMvc.perform(put("/api/templates/{templateId}", testTemplateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[*].field", hasItem("positionName")));
        
        verify(templateService, never()).updateTemplate(any(), any());
    }
    
    // ========== DELETE /api/templates/{templateId} Tests ==========
    
    @Test
    @DisplayName("DELETE /api/templates/{templateId} - should delete template successfully")
    void deleteTemplate_ShouldDeleteSuccessfully() throws Exception {
        // Arrange
        doNothing().when(templateService).deleteTemplate(testTemplateId);
        
        // Act & Assert
        mockMvc.perform(delete("/api/templates/{templateId}", testTemplateId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        
        verify(templateService).deleteTemplate(testTemplateId);
    }
    
    @Test
    @DisplayName("DELETE /api/templates/{templateId} - should return 404 when template not found")
    void deleteTemplate_ShouldReturn404_WhenNotFound() throws Exception {
        // Arrange
        doThrow(new TemplateNotFoundException(testTemplateId))
                .when(templateService).deleteTemplate(testTemplateId);
        
        // Act & Assert
        mockMvc.perform(delete("/api/templates/{templateId}", testTemplateId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        
        verify(templateService).deleteTemplate(testTemplateId);
    }
    
    @Test
    @DisplayName("DELETE /api/templates/{templateId} - should return 400 when template has tasks")
    void deleteTemplate_ShouldReturn400_WhenTemplateHasTasks() throws Exception {
        // Arrange
        doThrow(new TemplateDeletionException(testTemplateId, 5L))
                .when(templateService).deleteTemplate(testTemplateId);
        
        // Act & Assert
        mockMvc.perform(delete("/api/templates/{templateId}", testTemplateId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("5 associated task(s)")));
        
        verify(templateService).deleteTemplate(testTemplateId);
    }
    
    // ========== POST /api/templates/import Tests ==========
    
    @Test
    @DisplayName("POST /api/templates/import - should import template successfully")
    void importTemplateFromCsv_ShouldImportSuccessfully() throws Exception {
        // Arrange
        String csvContent = """
                position_name
                Software Engineer
                title,description,task_order,owner_role
                Setup workstation,Install required software and tools,1,USER
                Meet the team,Introduction meeting with team members,2,MANAGER
                Review codebase,Familiarize with main repositories,3,USER
                """;
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "template.csv",
                "text/csv",
                csvContent.getBytes()
        );
        
        UUID taskId1 = UUID.randomUUID();
        UUID taskId2 = UUID.randomUUID();
        UUID taskId3 = UUID.randomUUID();
        
        TemplateImportResponseDto importResponse = new TemplateImportResponseDto(
                testTemplateId,
                "Software Engineer",
                3,
                List.of(taskId1, taskId2, taskId3),
                "Successfully imported template 'Software Engineer' with 3 task(s)"
        );
        
        when(templateService.importTemplateFromCsv(any())).thenReturn(importResponse);
        
        // Act & Assert
        mockMvc.perform(multipart("/api/templates/import")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/api/templates/" + testTemplateId)))
                .andExpect(jsonPath("$.templateId").value(testTemplateId.toString()))
                .andExpect(jsonPath("$.positionName").value("Software Engineer"))
                .andExpect(jsonPath("$.tasksImported").value(3))
                .andExpect(jsonPath("$.taskIds", hasSize(3)))
                .andExpect(jsonPath("$.message").value(containsString("Successfully imported")));
        
        verify(templateService).importTemplateFromCsv(any());
    }
    
    @Test
    @DisplayName("POST /api/templates/import - should return 400 when file is missing")
    void importTemplateFromCsv_ShouldReturn400_WhenFileIsMissing() throws Exception {
        // Arrange
        when(templateService.importTemplateFromCsv(any()))
                .thenThrow(new CsvImportException("CSV file is required and cannot be empty"));
        
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "template.csv",
                "text/csv",
                new byte[0]
        );
        
        // Act & Assert
        mockMvc.perform(multipart("/api/templates/import")
                        .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("CSV file is required")));
        
        verify(templateService).importTemplateFromCsv(any());
    }
    
    @Test
    @DisplayName("POST /api/templates/import - should return 400 when file is not CSV")
    void importTemplateFromCsv_ShouldReturn400_WhenFileIsNotCsv() throws Exception {
        // Arrange
        when(templateService.importTemplateFromCsv(any()))
                .thenThrow(new CsvImportException("File must be a CSV file with .csv extension"));
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "template.txt",
                "text/plain",
                "some content".getBytes()
        );
        
        // Act & Assert
        mockMvc.perform(multipart("/api/templates/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString(".csv extension")));
        
        verify(templateService).importTemplateFromCsv(any());
    }
    
    @Test
    @DisplayName("POST /api/templates/import - should return 400 when CSV format is invalid")
    void importTemplateFromCsv_ShouldReturn400_WhenCsvFormatIsInvalid() throws Exception {
        // Arrange
        String invalidCsvContent = """
                invalid_header
                Some Value
                """;
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "template.csv",
                "text/csv",
                invalidCsvContent.getBytes()
        );
        
        when(templateService.importTemplateFromCsv(any()))
                .thenThrow(new CsvImportException("First row must contain 'position_name' header, found: invalid_header"));
        
        // Act & Assert
        mockMvc.perform(multipart("/api/templates/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("position_name")));
        
        verify(templateService).importTemplateFromCsv(any());
    }
    
    @Test
    @DisplayName("POST /api/templates/import - should return 400 when CSV has missing required columns")
    void importTemplateFromCsv_ShouldReturn400_WhenCsvHasMissingColumns() throws Exception {
        // Arrange
        String csvContent = """
                position_name
                Software Engineer
                title,description
                Setup workstation,Install required software
                """;
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "template.csv",
                "text/csv",
                csvContent.getBytes()
        );
        
        when(templateService.importTemplateFromCsv(any()))
                .thenThrow(new CsvImportException("Task header must contain at least 4 columns: title, description, task_order, owner_role"));
        
        // Act & Assert
        mockMvc.perform(multipart("/api/templates/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("4 columns")));
        
        verify(templateService).importTemplateFromCsv(any());
    }
    
    @Test
    @DisplayName("POST /api/templates/import - should return 400 when position name already exists")
    void importTemplateFromCsv_ShouldReturn400_WhenPositionNameExists() throws Exception {
        // Arrange
        String csvContent = """
                position_name
                Software Engineer
                title,description,task_order,owner_role
                Setup workstation,Install required software,1,USER
                """;
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "template.csv",
                "text/csv",
                csvContent.getBytes()
        );
        
        when(templateService.importTemplateFromCsv(any()))
                .thenThrow(new DuplicatePositionNameException("software engineer"));
        
        // Act & Assert
        mockMvc.perform(multipart("/api/templates/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("software engineer")));
        
        verify(templateService).importTemplateFromCsv(any());
    }
    
    @Test
    @DisplayName("POST /api/templates/import - should return 400 when task data is invalid")
    void importTemplateFromCsv_ShouldReturn400_WhenTaskDataIsInvalid() throws Exception {
        // Arrange
        String csvContent = """
                position_name
                Software Engineer
                title,description,task_order,owner_role
                Setup workstation,Install required software,invalid,USER
                """;
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "template.csv",
                "text/csv",
                csvContent.getBytes()
        );
        
        when(templateService.importTemplateFromCsv(any()))
                .thenThrow(new CsvImportException("Row 4: task_order must be a valid integer, found: 'invalid'"));
        
        // Act & Assert
        mockMvc.perform(multipart("/api/templates/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("task_order")));
        
        verify(templateService).importTemplateFromCsv(any());
    }
    
    @Test
    @DisplayName("POST /api/templates/import - should return 400 when owner_role is invalid")
    void importTemplateFromCsv_ShouldReturn400_WhenOwnerRoleIsInvalid() throws Exception {
        // Arrange
        String csvContent = """
                position_name
                Software Engineer
                title,description,task_order,owner_role
                Setup workstation,Install required software,1,INVALID_ROLE
                """;
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "template.csv",
                "text/csv",
                csvContent.getBytes()
        );
        
        when(templateService.importTemplateFromCsv(any()))
                .thenThrow(new CsvImportException("Row 4: owner_role must be either 'MANAGER' or 'USER', found: 'INVALID_ROLE'"));
        
        // Act & Assert
        mockMvc.perform(multipart("/api/templates/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("owner_role")));
        
        verify(templateService).importTemplateFromCsv(any());
    }
    
    @Test
    @DisplayName("POST /api/templates/import - should return 400 when CSV has no tasks")
    void importTemplateFromCsv_ShouldReturn400_WhenCsvHasNoTasks() throws Exception {
        // Arrange
        String csvContent = """
                position_name
                Software Engineer
                title,description,task_order,owner_role
                """;
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "template.csv",
                "text/csv",
                csvContent.getBytes()
        );
        
        when(templateService.importTemplateFromCsv(any()))
                .thenThrow(new CsvImportException("CSV file must contain at least one task"));
        
        // Act & Assert
        mockMvc.perform(multipart("/api/templates/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("at least one task")));
        
        verify(templateService).importTemplateFromCsv(any());
    }
}

