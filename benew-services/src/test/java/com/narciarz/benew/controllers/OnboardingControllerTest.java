package com.narciarz.benew.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.narciarz.benew.exceptions.OnboardingProcessDeletionException;
import com.narciarz.benew.exceptions.OnboardingProcessNotFoundException;
import com.narciarz.benew.exceptions.TemplateNotFoundException;
import com.narciarz.benew.exceptions.UserNotFoundException;
import com.narciarz.benew.models.OnboardingStatus;
import com.narciarz.benew.models.dto.CreateOnboardingProcessRequestDto;
import com.narciarz.benew.models.dto.OnboardingProcessResponseDto;
import com.narciarz.benew.models.dto.UpdateOnboardingProcessRequestDto;
import com.narciarz.benew.services.OnboardingService;
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
 * Integration tests for OnboardingController using @WebMvcTest.
 * 
 * <p>Tests the web layer (controller) in isolation with mocked service layer.
 * Uses MockMvc to perform HTTP requests and verify responses.</p>
 */
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(OnboardingController.class)
@DisplayName("OnboardingController Integration Tests")
class OnboardingControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private OnboardingService onboardingService;
    
    private OnboardingProcessResponseDto processResponseDto;
    private CreateOnboardingProcessRequestDto createProcessDto;
    private UpdateOnboardingProcessRequestDto updateProcessDto;
    private UUID processId;
    private UUID userId;
    private UUID managerId;
    private UUID templateId;
    
    @BeforeEach
    void setUp() {
        processId = UUID.randomUUID();
        userId = UUID.randomUUID();
        managerId = UUID.randomUUID();
        templateId = UUID.randomUUID();
        
        processResponseDto = new OnboardingProcessResponseDto(
                processId,
                userId,
                "John Doe",
                managerId,
                "Jane Manager",
                templateId,
                "developer",
                OnboardingStatus.ACTIVE,
                5,
                2,
                40.0,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
        
        createProcessDto = new CreateOnboardingProcessRequestDto(userId, managerId, templateId);
        
        updateProcessDto = new UpdateOnboardingProcessRequestDto(
                OnboardingStatus.ARCHIVED,
                5,
                5
        );
    }
    
    @Test
    @DisplayName("GET /onboarding should return page of processes")
    void shouldGetAllProcesses() throws Exception {
        // Arrange
        List<OnboardingProcessResponseDto> processes = Arrays.asList(processResponseDto);
        Page<OnboardingProcessResponseDto> processPage = new PageImpl<>(processes);
        
        when(onboardingService.getAllProcesses(any(Pageable.class), eq(null), eq(null), eq(null)))
                .thenReturn(processPage);
        
        // Act & Assert
        mockMvc.perform(get("/onboarding")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(processId.toString()))
                .andExpect(jsonPath("$.content[0].userName").value("John Doe"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.content[0].progressPercentage").value(40.0));
        
        verify(onboardingService).getAllProcesses(any(Pageable.class), eq(null), eq(null), eq(null));
    }
    
    @Test
    @DisplayName("GET /onboarding with status filter should return filtered processes")
    void shouldGetProcessesFilteredByStatus() throws Exception {
        // Arrange
        List<OnboardingProcessResponseDto> processes = Arrays.asList(processResponseDto);
        Page<OnboardingProcessResponseDto> processPage = new PageImpl<>(processes);
        
        when(onboardingService.getAllProcesses(any(Pageable.class), eq(OnboardingStatus.ACTIVE), eq(null), eq(null)))
                .thenReturn(processPage);
        
        // Act & Assert
        mockMvc.perform(get("/onboarding")
                        .param("status", "ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
        
        verify(onboardingService).getAllProcesses(any(Pageable.class), eq(OnboardingStatus.ACTIVE), eq(null), eq(null));
    }
    
    @Test
    @DisplayName("GET /onboarding with managerId filter should return filtered processes")
    void shouldGetProcessesFilteredByManager() throws Exception {
        // Arrange
        List<OnboardingProcessResponseDto> processes = Arrays.asList(processResponseDto);
        Page<OnboardingProcessResponseDto> processPage = new PageImpl<>(processes);
        
        when(onboardingService.getAllProcesses(any(Pageable.class), eq(null), eq(managerId), eq(null)))
                .thenReturn(processPage);
        
        // Act & Assert
        mockMvc.perform(get("/onboarding")
                        .param("managerId", managerId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].managerId").value(managerId.toString()));
        
        verify(onboardingService).getAllProcesses(any(Pageable.class), eq(null), eq(managerId), eq(null));
    }
    
    @Test
    @DisplayName("GET /onboarding/{processId} should return process details")
    void shouldGetProcessById() throws Exception {
        // Arrange
        when(onboardingService.getProcessById(processId)).thenReturn(processResponseDto);
        
        // Act & Assert
        mockMvc.perform(get("/onboarding/{processId}", processId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(processId.toString()))
                .andExpect(jsonPath("$.userName").value("John Doe"))
                .andExpect(jsonPath("$.managerName").value("Jane Manager"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.totalTasksCount").value(5))
                .andExpect(jsonPath("$.completedTasksCount").value(2))
                .andExpect(jsonPath("$.progressPercentage").value(40.0));
        
        verify(onboardingService).getProcessById(processId);
    }
    
    @Test
    @DisplayName("GET /onboarding/{processId} should return 404 when process not found")
    void shouldReturn404WhenProcessNotFound() throws Exception {
        // Arrange
        when(onboardingService.getProcessById(processId))
                .thenThrow(new OnboardingProcessNotFoundException(processId));
        
        // Act & Assert
        mockMvc.perform(get("/onboarding/{processId}", processId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString(processId.toString())));
        
        verify(onboardingService).getProcessById(processId);
    }
    
    @Test
    @DisplayName("POST /onboarding should create new process")
    void shouldCreateProcess() throws Exception {
        // Arrange
        when(onboardingService.createProcess(any(CreateOnboardingProcessRequestDto.class)))
                .thenReturn(processResponseDto);
        
        // Act & Assert
        mockMvc.perform(post("/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createProcessDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(processId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.managerId").value(managerId.toString()))
                .andExpect(jsonPath("$.sourceTemplateId").value(templateId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        
        verify(onboardingService).createProcess(any(CreateOnboardingProcessRequestDto.class));
    }
    
    @Test
    @DisplayName("POST /onboarding should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        // Arrange - create DTO with missing required fields
        CreateOnboardingProcessRequestDto invalidDto = new CreateOnboardingProcessRequestDto(null, null, null);
        
        // Act & Assert
        mockMvc.perform(post("/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
        
        verifyNoInteractions(onboardingService);
    }
    
    @Test
    @DisplayName("POST /onboarding should return 404 when user not found")
    void shouldReturn404WhenUserNotFoundDuringCreate() throws Exception {
        // Arrange
        when(onboardingService.createProcess(any(CreateOnboardingProcessRequestDto.class)))
                .thenThrow(new UserNotFoundException(userId));
        
        // Act & Assert
        mockMvc.perform(post("/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createProcessDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString(userId.toString())));
        
        verify(onboardingService).createProcess(any(CreateOnboardingProcessRequestDto.class));
    }
    
    @Test
    @DisplayName("POST /onboarding should return 404 when template not found")
    void shouldReturn404WhenTemplateNotFoundDuringCreate() throws Exception {
        // Arrange
        when(onboardingService.createProcess(any(CreateOnboardingProcessRequestDto.class)))
                .thenThrow(new TemplateNotFoundException(templateId));
        
        // Act & Assert
        mockMvc.perform(post("/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createProcessDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString(templateId.toString())));
        
        verify(onboardingService).createProcess(any(CreateOnboardingProcessRequestDto.class));
    }
    
    @Test
    @DisplayName("PUT /onboarding/{processId} should update process")
    void shouldUpdateProcess() throws Exception {
        // Arrange
        OnboardingProcessResponseDto updatedDto = new OnboardingProcessResponseDto(
                processId, userId, "John Doe", managerId, "Jane Manager",
                templateId, "developer", OnboardingStatus.ARCHIVED,
                5, 5, 100.0, OffsetDateTime.now(), OffsetDateTime.now()
        );
        
        when(onboardingService.updateProcess(eq(processId), any(UpdateOnboardingProcessRequestDto.class)))
                .thenReturn(updatedDto);
        
        // Act & Assert
        mockMvc.perform(put("/onboarding/{processId}", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProcessDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(processId.toString()))
                .andExpect(jsonPath("$.status").value("ARCHIVED"))
                .andExpect(jsonPath("$.progressPercentage").value(100.0));
        
        verify(onboardingService).updateProcess(eq(processId), any(UpdateOnboardingProcessRequestDto.class));
    }
    
    @Test
    @DisplayName("PUT /onboarding/{processId} should return 404 when process not found")
    void shouldReturn404WhenUpdatingNonExistentProcess() throws Exception {
        // Arrange
        when(onboardingService.updateProcess(eq(processId), any(UpdateOnboardingProcessRequestDto.class)))
                .thenThrow(new OnboardingProcessNotFoundException(processId));
        
        // Act & Assert
        mockMvc.perform(put("/onboarding/{processId}", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProcessDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString(processId.toString())));
        
        verify(onboardingService).updateProcess(eq(processId), any(UpdateOnboardingProcessRequestDto.class));
    }
    
    @Test
    @DisplayName("DELETE /onboarding/{processId} should delete process")
    void shouldDeleteProcess() throws Exception {
        // Arrange
        doNothing().when(onboardingService).deleteProcess(processId);
        
        // Act & Assert
        mockMvc.perform(delete("/onboarding/{processId}", processId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        
        verify(onboardingService).deleteProcess(processId);
    }
    
    @Test
    @DisplayName("DELETE /onboarding/{processId} should return 404 when process not found")
    void shouldReturn404WhenDeletingNonExistentProcess() throws Exception {
        // Arrange
        doThrow(new OnboardingProcessNotFoundException(processId))
                .when(onboardingService).deleteProcess(processId);
        
        // Act & Assert
        mockMvc.perform(delete("/onboarding/{processId}", processId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString(processId.toString())));
        
        verify(onboardingService).deleteProcess(processId);
    }
    
    @Test
    @DisplayName("DELETE /onboarding/{processId} should return 400 when deletion violates constraints")
    void shouldReturn400WhenDeletionViolatesConstraints() throws Exception {
        // Arrange
        doThrow(new OnboardingProcessDeletionException(processId, 3L))
                .when(onboardingService).deleteProcess(processId);
        
        // Act & Assert
        mockMvc.perform(delete("/onboarding/{processId}", processId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString(processId.toString())))
                .andExpect(jsonPath("$.message").value(containsString("3")));
        
        verify(onboardingService).deleteProcess(processId);
    }
    
    @Test
    @DisplayName("GET /onboarding with pagination should use default values")
    void shouldUseDefaultPaginationValues() throws Exception {
        // Arrange
        Page<OnboardingProcessResponseDto> processPage = new PageImpl<>(Arrays.asList(processResponseDto));
        
        when(onboardingService.getAllProcesses(any(Pageable.class), eq(null), eq(null), eq(null)))
                .thenReturn(processPage);
        
        // Act & Assert
        mockMvc.perform(get("/onboarding")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        
        verify(onboardingService).getAllProcesses(any(Pageable.class), eq(null), eq(null), eq(null));
    }
    
    @Test
    @DisplayName("GET /onboarding with custom pagination should use provided values")
    void shouldUseCustomPaginationValues() throws Exception {
        // Arrange
        Page<OnboardingProcessResponseDto> processPage = new PageImpl<>(Arrays.asList(processResponseDto));
        
        when(onboardingService.getAllProcesses(any(Pageable.class), eq(null), eq(null), eq(null)))
                .thenReturn(processPage);
        
        // Act & Assert
        mockMvc.perform(get("/onboarding")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "updatedAt,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        verify(onboardingService).getAllProcesses(any(Pageable.class), eq(null), eq(null), eq(null));
    }
}

