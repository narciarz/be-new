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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OnboardingService.
 * 
 * <p>Tests business logic, validation, and error handling using Mockito
 * to mock dependencies. Follows AAA (Arrange-Act-Assert) pattern.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OnboardingService Unit Tests")
class OnboardingServiceTest {
    
    @Mock
    private OnboardingProcessRepository processRepository;
    
    @Mock
    private OnboardingTaskRepository taskRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private TemplateRepository templateRepository;
    
    @Mock
    private TemplateTaskRepository templateTaskRepository;
    
    @Mock
    private OnboardingMapper onboardingMapper;
    
    @InjectMocks
    private OnboardingService onboardingService;
    
    private OnboardingProcess testProcess;
    private AppUser testUser;
    private AppUser testManager;
    private Template testTemplate;
    private TemplateTask testTemplateTask1;
    private TemplateTask testTemplateTask2;
    private CreateOnboardingProcessRequestDto createProcessDto;
    private UpdateOnboardingProcessRequestDto updateProcessDto;
    private OnboardingProcessResponseDto processResponseDto;
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
        
        // Setup test user
        testUser = new AppUser();
        testUser.setId(userId);
        testUser.setEmail("user@example.com");
        testUser.setPasswordHash("hash");
        testUser.setRole(UserRole.USER);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPositionName("Developer");
        testUser.setCreatedAt(OffsetDateTime.now());
        testUser.setUpdatedAt(OffsetDateTime.now());
        
        // Setup test manager
        testManager = new AppUser();
        testManager.setId(managerId);
        testManager.setEmail("manager@example.com");
        testManager.setPasswordHash("hash");
        testManager.setRole(UserRole.MANAGER);
        testManager.setFirstName("Jane");
        testManager.setLastName("Manager");
        testManager.setPositionName("Senior Developer");
        testManager.setCreatedAt(OffsetDateTime.now());
        testManager.setUpdatedAt(OffsetDateTime.now());
        
        // Setup test template
        testTemplate = new Template();
        testTemplate.setId(templateId);
        testTemplate.setPositionName("developer");
        testTemplate.setCreatedAt(OffsetDateTime.now());
        testTemplate.setUpdatedAt(OffsetDateTime.now());
        
        // Setup test template tasks
        testTemplateTask1 = new TemplateTask(testTemplate, "Task 1", "Description 1", 1, TaskOwnerRole.USER);
        testTemplateTask1.setId(UUID.randomUUID());
        
        testTemplateTask2 = new TemplateTask(testTemplate, "Task 2", "Description 2", 2, TaskOwnerRole.MANAGER);
        testTemplateTask2.setId(UUID.randomUUID());
        
        // Setup test process
        testProcess = new OnboardingProcess(testUser, testManager, testTemplate, OnboardingStatus.ACTIVE);
        testProcess.setId(processId);
        testProcess.setTotalTasksCount(2);
        testProcess.setCompletedTasksCount(0);
        testProcess.setCreatedAt(OffsetDateTime.now());
        testProcess.setUpdatedAt(OffsetDateTime.now());
        
        // Setup DTOs
        createProcessDto = new CreateOnboardingProcessRequestDto(userId, managerId, templateId);
        
        updateProcessDto = new UpdateOnboardingProcessRequestDto(
                OnboardingStatus.ARCHIVED,
                2,
                1
        );
        
        processResponseDto = new OnboardingProcessResponseDto(
                processId,
                userId,
                "John Doe",
                managerId,
                "Jane Manager",
                templateId,
                "developer",
                OnboardingStatus.ACTIVE,
                2,
                0,
                0.0,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }
    
    @Test
    @DisplayName("Should get all processes with pagination")
    void shouldGetAllProcessesWithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<OnboardingProcess> processes = Arrays.asList(testProcess);
        Page<OnboardingProcess> processPage = new PageImpl<>(processes, pageable, 1);
        
        when(processRepository.findAll(pageable)).thenReturn(processPage);
        when(onboardingMapper.toResponseDto(any(OnboardingProcess.class))).thenReturn(processResponseDto);
        
        // Act
        Page<OnboardingProcessResponseDto> result = onboardingService.getAllProcesses(pageable, null, null, null);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(processRepository).findAll(pageable);
        verify(onboardingMapper).toResponseDto(testProcess);
    }
    
    @Test
    @DisplayName("Should get processes filtered by status")
    void shouldGetProcessesFilteredByStatus() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<OnboardingProcess> processes = Arrays.asList(testProcess);
        Page<OnboardingProcess> processPage = new PageImpl<>(processes, pageable, 1);
        
        when(processRepository.findByStatus(OnboardingStatus.ACTIVE, pageable)).thenReturn(processPage);
        when(onboardingMapper.toResponseDto(any(OnboardingProcess.class))).thenReturn(processResponseDto);
        
        // Act
        Page<OnboardingProcessResponseDto> result = onboardingService.getAllProcesses(
                pageable, OnboardingStatus.ACTIVE, null, null);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(processRepository).findByStatus(OnboardingStatus.ACTIVE, pageable);
    }
    
    @Test
    @DisplayName("Should get processes filtered by manager")
    void shouldGetProcessesFilteredByManager() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<OnboardingProcess> processes = Arrays.asList(testProcess);
        Page<OnboardingProcess> processPage = new PageImpl<>(processes, pageable, 1);
        
        when(processRepository.findByManagerId(managerId, pageable)).thenReturn(processPage);
        when(onboardingMapper.toResponseDto(any(OnboardingProcess.class))).thenReturn(processResponseDto);
        
        // Act
        Page<OnboardingProcessResponseDto> result = onboardingService.getAllProcesses(
                pageable, null, managerId, null);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(processRepository).findByManagerId(managerId, pageable);
    }
    
    @Test
    @DisplayName("Should get process by id")
    void shouldGetProcessById() {
        // Arrange
        when(processRepository.findById(processId)).thenReturn(Optional.of(testProcess));
        when(onboardingMapper.toResponseDto(testProcess)).thenReturn(processResponseDto);
        
        // Act
        OnboardingProcessResponseDto result = onboardingService.getProcessById(processId);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(processId);
        verify(processRepository).findById(processId);
        verify(onboardingMapper).toResponseDto(testProcess);
    }
    
    @Test
    @DisplayName("Should throw exception when process not found by id")
    void shouldThrowExceptionWhenProcessNotFoundById() {
        // Arrange
        when(processRepository.findById(processId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> onboardingService.getProcessById(processId))
                .isInstanceOf(OnboardingProcessNotFoundException.class)
                .hasMessageContaining(processId.toString());
        
        verify(processRepository).findById(processId);
        verifyNoInteractions(onboardingMapper);
    }
    
    @Test
    @DisplayName("Should create process with tasks copied from template")
    void shouldCreateProcessWithTasksCopiedFromTemplate() {
        // Arrange
        List<TemplateTask> templateTasks = Arrays.asList(testTemplateTask1, testTemplateTask2);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(managerId)).thenReturn(Optional.of(testManager));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));
        when(processRepository.save(any(OnboardingProcess.class))).thenReturn(testProcess);
        when(templateTaskRepository.findByTemplateIdOrderByTaskOrderAsc(templateId)).thenReturn(templateTasks);
        when(onboardingMapper.toResponseDto(testProcess)).thenReturn(processResponseDto);
        
        // Act
        OnboardingProcessResponseDto result = onboardingService.createProcess(createProcessDto);
        
        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).findById(userId);
        verify(userRepository).findById(managerId);
        verify(templateRepository).findById(templateId);
        verify(processRepository, times(2)).save(any(OnboardingProcess.class)); // Once for process, once for counters
        verify(templateTaskRepository).findByTemplateIdOrderByTaskOrderAsc(templateId);
        verify(taskRepository, times(2)).save(any(OnboardingTask.class)); // Two tasks copied
        verify(onboardingMapper).toResponseDto(testProcess);
    }
    
    @Test
    @DisplayName("Should throw exception when user not found during create")
    void shouldThrowExceptionWhenUserNotFoundDuringCreate() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> onboardingService.createProcess(createProcessDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());
        
        verify(userRepository).findById(userId);
        verifyNoInteractions(processRepository);
    }
    
    @Test
    @DisplayName("Should throw exception when manager not found during create")
    void shouldThrowExceptionWhenManagerNotFoundDuringCreate() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(managerId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> onboardingService.createProcess(createProcessDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(managerId.toString());
        
        verify(userRepository).findById(userId);
        verify(userRepository).findById(managerId);
        verifyNoInteractions(processRepository);
    }
    
    @Test
    @DisplayName("Should throw exception when template not found during create")
    void shouldThrowExceptionWhenTemplateNotFoundDuringCreate() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(managerId)).thenReturn(Optional.of(testManager));
        when(templateRepository.findById(templateId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> onboardingService.createProcess(createProcessDto))
                .isInstanceOf(TemplateNotFoundException.class)
                .hasMessageContaining(templateId.toString());
        
        verify(templateRepository).findById(templateId);
        verifyNoInteractions(processRepository);
    }
    
    @Test
    @DisplayName("Should update process")
    void shouldUpdateProcess() {
        // Arrange
        when(processRepository.findById(processId)).thenReturn(Optional.of(testProcess));
        when(processRepository.save(testProcess)).thenReturn(testProcess);
        when(onboardingMapper.toResponseDto(testProcess)).thenReturn(processResponseDto);
        
        // Act
        OnboardingProcessResponseDto result = onboardingService.updateProcess(processId, updateProcessDto);
        
        // Assert
        assertThat(result).isNotNull();
        verify(processRepository).findById(processId);
        verify(onboardingMapper).updateEntityFromDto(updateProcessDto, testProcess);
        verify(processRepository).save(testProcess);
        verify(onboardingMapper).toResponseDto(testProcess);
    }
    
    @Test
    @DisplayName("Should throw exception when updating non-existent process")
    void shouldThrowExceptionWhenUpdatingNonExistentProcess() {
        // Arrange
        when(processRepository.findById(processId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> onboardingService.updateProcess(processId, updateProcessDto))
                .isInstanceOf(OnboardingProcessNotFoundException.class)
                .hasMessageContaining(processId.toString());
        
        verify(processRepository).findById(processId);
        verifyNoMoreInteractions(processRepository);
    }
    
    @Test
    @DisplayName("Should archive active process on delete")
    void shouldArchiveActiveProcessOnDelete() {
        // Arrange
        testProcess.setStatus(OnboardingStatus.ACTIVE);
        when(processRepository.findById(processId)).thenReturn(Optional.of(testProcess));
        when(processRepository.save(testProcess)).thenReturn(testProcess);
        
        // Act
        onboardingService.deleteProcess(processId);
        
        // Assert
        verify(processRepository).findById(processId);
        verify(processRepository).save(testProcess);
        assertThat(testProcess.getStatus()).isEqualTo(OnboardingStatus.ARCHIVED);
    }
    
    @Test
    @DisplayName("Should hard delete archived process with no tasks")
    void shouldHardDeleteArchivedProcessWithNoTasks() {
        // Arrange
        testProcess.setStatus(OnboardingStatus.ARCHIVED);
        when(processRepository.findById(processId)).thenReturn(Optional.of(testProcess));
        when(taskRepository.countByOnboardingProcessId(processId)).thenReturn(0L);
        
        // Act
        onboardingService.deleteProcess(processId);
        
        // Assert
        verify(processRepository).findById(processId);
        verify(taskRepository).countByOnboardingProcessId(processId);
        verify(processRepository).deleteById(processId);
    }
    
    @Test
    @DisplayName("Should throw exception when deleting archived process with tasks")
    void shouldThrowExceptionWhenDeletingArchivedProcessWithTasks() {
        // Arrange
        testProcess.setStatus(OnboardingStatus.ARCHIVED);
        when(processRepository.findById(processId)).thenReturn(Optional.of(testProcess));
        when(taskRepository.countByOnboardingProcessId(processId)).thenReturn(2L);
        
        // Act & Assert
        assertThatThrownBy(() -> onboardingService.deleteProcess(processId))
                .isInstanceOf(OnboardingProcessDeletionException.class)
                .hasMessageContaining(processId.toString())
                .hasMessageContaining("2");
        
        verify(processRepository).findById(processId);
        verify(taskRepository).countByOnboardingProcessId(processId);
        verify(processRepository, never()).deleteById(any());
    }
    
    @Test
    @DisplayName("Should throw exception when deleting non-existent process")
    void shouldThrowExceptionWhenDeletingNonExistentProcess() {
        // Arrange
        when(processRepository.findById(processId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> onboardingService.deleteProcess(processId))
                .isInstanceOf(OnboardingProcessNotFoundException.class)
                .hasMessageContaining(processId.toString());
        
        verify(processRepository).findById(processId);
        verifyNoMoreInteractions(processRepository);
    }
}

