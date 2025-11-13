package com.narciarz.benew.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.narciarz.benew.exceptions.DuplicatePositionNameException;
import com.narciarz.benew.exceptions.TemplateDeletionException;
import com.narciarz.benew.exceptions.TemplateNotFoundException;
import com.narciarz.benew.models.dto.CreateTemplateRequestDto;
import com.narciarz.benew.models.dto.UpdateTemplateRequestDto;
import com.narciarz.benew.models.dto.TemplateResponseDto;
import com.narciarz.benew.services.TemplateService;
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
}

