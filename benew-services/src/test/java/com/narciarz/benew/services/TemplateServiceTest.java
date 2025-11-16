package com.narciarz.benew.services;

import com.narciarz.benew.exceptions.CsvImportException;
import com.narciarz.benew.exceptions.DuplicatePositionNameException;
import com.narciarz.benew.exceptions.TemplateDeletionException;
import com.narciarz.benew.exceptions.TemplateNotFoundException;
import com.narciarz.benew.models.Template;
import com.narciarz.benew.models.TemplateTask;
import com.narciarz.benew.models.dto.CreateTemplateRequestDto;
import com.narciarz.benew.models.dto.UpdateTemplateRequestDto;
import com.narciarz.benew.models.dto.TemplateResponseDto;
import com.narciarz.benew.models.dto.TemplateImportResponseDto;
import com.narciarz.benew.repositories.TemplateRepository;
import com.narciarz.benew.repositories.TemplateTaskRepository;
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
import org.springframework.mock.web.MockMultipartFile;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TemplateService.
 * 
 * <p>Tests business logic, validation, and error handling using Mockito
 * to mock dependencies. Follows AAA (Arrange-Act-Assert) pattern.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TemplateService Unit Tests")
class TemplateServiceTest {
    
    @Mock
    private TemplateRepository templateRepository;
    
    @Mock
    private TemplateMapper templateMapper;
    
    @Mock
    private TemplateTaskRepository templateTaskRepository;
    
    @InjectMocks
    private TemplateService templateService;
    
    private Template testTemplate;
    private CreateTemplateRequestDto createTemplateDto;
    private UpdateTemplateRequestDto updateTemplateDto;
    private TemplateResponseDto templateResponseDto;
    private UUID testTemplateId;
    
    @BeforeEach
    void setUp() {
        testTemplateId = UUID.randomUUID();
        
        // Setup test template
        testTemplate = new Template();
        testTemplate.setId(testTemplateId);
        testTemplate.setPositionName("software engineer");
        testTemplate.setCreatedAt(OffsetDateTime.now());
        testTemplate.setUpdatedAt(OffsetDateTime.now());
        
        // Setup DTOs
        createTemplateDto = new CreateTemplateRequestDto("Software Engineer");
        
        updateTemplateDto = new UpdateTemplateRequestDto("Senior Software Engineer");
        
        templateResponseDto = new TemplateResponseDto(
                testTemplateId,
                "software engineer",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }
    
    // ========== GET Operations Tests ==========
    
    @Test
    @DisplayName("getAllTemplates - should return paginated templates")
    void getAllTemplates_ShouldReturnPaginatedTemplates() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Template> templatePage = new PageImpl<>(List.of(testTemplate));
        when(templateRepository.findAll(pageable)).thenReturn(templatePage);
        when(templateMapper.toResponseDto(testTemplate)).thenReturn(templateResponseDto);
        
        // Act
        Page<TemplateResponseDto> result = templateService.getAllTemplates(pageable);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getPositionName()).isEqualTo("software engineer");
        verify(templateRepository).findAll(pageable);
        verify(templateMapper).toResponseDto(testTemplate);
    }
    
    @Test
    @DisplayName("getTemplatesByPositionName - should return filtered templates")
    void getTemplatesByPositionName_ShouldReturnFilteredTemplates() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Template> templatePage = new PageImpl<>(List.of(testTemplate));
        when(templateRepository.findByPositionNameContainingIgnoreCase("engineer", pageable))
                .thenReturn(templatePage);
        when(templateMapper.toResponseDto(testTemplate)).thenReturn(templateResponseDto);
        
        // Act
        Page<TemplateResponseDto> result = templateService.getTemplatesByPositionName("engineer", pageable);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(templateRepository).findByPositionNameContainingIgnoreCase("engineer", pageable);
    }
    
    @Test
    @DisplayName("getTemplateById - should return template when found")
    void getTemplateById_ShouldReturnTemplate_WhenTemplateExists() {
        // Arrange
        when(templateRepository.findById(testTemplateId)).thenReturn(Optional.of(testTemplate));
        when(templateMapper.toResponseDto(testTemplate)).thenReturn(templateResponseDto);
        
        // Act
        TemplateResponseDto result = templateService.getTemplateById(testTemplateId);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPositionName()).isEqualTo("software engineer");
        verify(templateRepository).findById(testTemplateId);
        verify(templateMapper).toResponseDto(testTemplate);
    }
    
    @Test
    @DisplayName("getTemplateById - should throw TemplateNotFoundException when template not found")
    void getTemplateById_ShouldThrowException_WhenTemplateNotFound() {
        // Arrange
        when(templateRepository.findById(testTemplateId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> templateService.getTemplateById(testTemplateId))
                .isInstanceOf(TemplateNotFoundException.class)
                .hasMessageContaining(testTemplateId.toString());
        
        verify(templateRepository).findById(testTemplateId);
        verify(templateMapper, never()).toResponseDto(any());
    }
    
    // ========== CREATE Operation Tests ==========
    
    @Test
    @DisplayName("createTemplate - should create template successfully with normalized position name")
    void createTemplate_ShouldCreateSuccessfully_WithNormalizedPositionName() {
        // Arrange
        when(templateRepository.existsByPositionNameIgnoreCase("software engineer")).thenReturn(false);
        when(templateMapper.toEntity(createTemplateDto)).thenReturn(testTemplate);
        when(templateRepository.save(any(Template.class))).thenReturn(testTemplate);
        when(templateMapper.toResponseDto(testTemplate)).thenReturn(templateResponseDto);
        
        // Act
        TemplateResponseDto result = templateService.createTemplate(createTemplateDto);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPositionName()).isEqualTo("software engineer");
        
        verify(templateRepository).existsByPositionNameIgnoreCase("software engineer");
        verify(templateRepository).save(any(Template.class));
        verify(templateMapper).toResponseDto(testTemplate);
    }
    
    @Test
    @DisplayName("createTemplate - should normalize position name (trim + lowercase)")
    void createTemplate_ShouldNormalizePositionName() {
        // Arrange
        CreateTemplateRequestDto dtoWithSpaces = new CreateTemplateRequestDto("  Software Engineer  ");
        Template template = new Template();
        template.setPositionName("  Software Engineer  ");
        
        when(templateRepository.existsByPositionNameIgnoreCase("software engineer")).thenReturn(false);
        when(templateMapper.toEntity(dtoWithSpaces)).thenReturn(template);
        when(templateRepository.save(any(Template.class))).thenAnswer(invocation -> {
            Template savedTemplate = invocation.getArgument(0);
            assertThat(savedTemplate.getPositionName()).isEqualTo("software engineer");
            return savedTemplate;
        });
        when(templateMapper.toResponseDto(any())).thenReturn(templateResponseDto);
        
        // Act
        templateService.createTemplate(dtoWithSpaces);
        
        // Assert
        verify(templateRepository).save(any(Template.class));
    }
    
    @Test
    @DisplayName("createTemplate - should throw DuplicatePositionNameException when position name exists")
    void createTemplate_ShouldThrowException_WhenPositionNameExists() {
        // Arrange
        when(templateRepository.existsByPositionNameIgnoreCase("software engineer")).thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> templateService.createTemplate(createTemplateDto))
                .isInstanceOf(DuplicatePositionNameException.class)
                .hasMessageContaining("software engineer");
        
        verify(templateRepository).existsByPositionNameIgnoreCase("software engineer");
        verify(templateRepository, never()).save(any());
    }
    
    // ========== UPDATE Operation Tests ==========
    
    @Test
    @DisplayName("updateTemplate - should update template successfully")
    void updateTemplate_ShouldUpdateSuccessfully() {
        // Arrange
        when(templateRepository.findById(testTemplateId)).thenReturn(Optional.of(testTemplate));
        when(templateRepository.existsByPositionNameIgnoreCase("senior software engineer")).thenReturn(false);
        doNothing().when(templateMapper).updateEntityFromDto(updateTemplateDto, testTemplate);
        when(templateRepository.save(testTemplate)).thenReturn(testTemplate);
        when(templateMapper.toResponseDto(testTemplate)).thenReturn(templateResponseDto);
        
        // Act
        TemplateResponseDto result = templateService.updateTemplate(testTemplateId, updateTemplateDto);
        
        // Assert
        assertThat(result).isNotNull();
        verify(templateRepository).findById(testTemplateId);
        verify(templateMapper).updateEntityFromDto(updateTemplateDto, testTemplate);
        verify(templateRepository).save(testTemplate);
    }
    
    @Test
    @DisplayName("updateTemplate - should throw TemplateNotFoundException when template not found")
    void updateTemplate_ShouldThrowException_WhenTemplateNotFound() {
        // Arrange
        when(templateRepository.findById(testTemplateId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> templateService.updateTemplate(testTemplateId, updateTemplateDto))
                .isInstanceOf(TemplateNotFoundException.class)
                .hasMessageContaining(testTemplateId.toString());
        
        verify(templateRepository).findById(testTemplateId);
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("updateTemplate - should throw DuplicatePositionNameException when new position name exists")
    void updateTemplate_ShouldThrowException_WhenNewPositionNameExists() {
        // Arrange
        when(templateRepository.findById(testTemplateId)).thenReturn(Optional.of(testTemplate));
        when(templateRepository.existsByPositionNameIgnoreCase("senior software engineer")).thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> templateService.updateTemplate(testTemplateId, updateTemplateDto))
                .isInstanceOf(DuplicatePositionNameException.class)
                .hasMessageContaining("senior software engineer");
        
        verify(templateRepository).findById(testTemplateId);
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("updateTemplate - should not check uniqueness when position name unchanged")
    void updateTemplate_ShouldNotCheckUniqueness_WhenPositionNameUnchanged() {
        // Arrange
        UpdateTemplateRequestDto sameNameDto = new UpdateTemplateRequestDto("Software Engineer");
        when(templateRepository.findById(testTemplateId)).thenReturn(Optional.of(testTemplate));
        doNothing().when(templateMapper).updateEntityFromDto(sameNameDto, testTemplate);
        when(templateRepository.save(testTemplate)).thenReturn(testTemplate);
        when(templateMapper.toResponseDto(testTemplate)).thenReturn(templateResponseDto);
        
        // Act
        templateService.updateTemplate(testTemplateId, sameNameDto);
        
        // Assert
        verify(templateRepository).findById(testTemplateId);
        verify(templateRepository, never()).existsByPositionNameIgnoreCase(anyString());
        verify(templateRepository).save(testTemplate);
    }
    
    // ========== DELETE Operation Tests ==========
    
    @Test
    @DisplayName("deleteTemplate - should delete template successfully when no tasks")
    void deleteTemplate_ShouldDeleteSuccessfully_WhenNoTasks() {
        // Arrange
        when(templateRepository.existsById(testTemplateId)).thenReturn(true);
        when(templateTaskRepository.countByTemplateId(testTemplateId)).thenReturn(0L);
        doNothing().when(templateRepository).deleteById(testTemplateId);
        
        // Act
        templateService.deleteTemplate(testTemplateId);
        
        // Assert
        verify(templateRepository).existsById(testTemplateId);
        verify(templateTaskRepository).countByTemplateId(testTemplateId);
        verify(templateRepository).deleteById(testTemplateId);
    }
    
    @Test
    @DisplayName("deleteTemplate - should throw TemplateNotFoundException when template not found")
    void deleteTemplate_ShouldThrowException_WhenTemplateNotFound() {
        // Arrange
        when(templateRepository.existsById(testTemplateId)).thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> templateService.deleteTemplate(testTemplateId))
                .isInstanceOf(TemplateNotFoundException.class)
                .hasMessageContaining(testTemplateId.toString());
        
        verify(templateRepository).existsById(testTemplateId);
        verify(templateRepository, never()).deleteById(any());
    }
    
    @Test
    @DisplayName("deleteTemplate - should throw TemplateDeletionException when template has tasks")
    void deleteTemplate_ShouldThrowException_WhenTemplateHasTasks() {
        // Arrange
        when(templateRepository.existsById(testTemplateId)).thenReturn(true);
        when(templateTaskRepository.countByTemplateId(testTemplateId)).thenReturn(5L);
        
        // Act & Assert
        assertThatThrownBy(() -> templateService.deleteTemplate(testTemplateId))
                .isInstanceOf(TemplateDeletionException.class)
                .hasMessageContaining("5 associated task(s)");
        
        verify(templateRepository).existsById(testTemplateId);
        verify(templateTaskRepository).countByTemplateId(testTemplateId);
        verify(templateRepository, never()).deleteById(any());
    }
    
    @Test
    @DisplayName("deleteTemplate - should throw TemplateDeletionException on database error")
    void deleteTemplate_ShouldThrowException_OnDatabaseError() {
        // Arrange
        when(templateRepository.existsById(testTemplateId)).thenReturn(true);
        when(templateTaskRepository.countByTemplateId(testTemplateId)).thenReturn(0L);
        doThrow(new RuntimeException("Database error")).when(templateRepository).deleteById(testTemplateId);
        
        // Act & Assert
        assertThatThrownBy(() -> templateService.deleteTemplate(testTemplateId))
                .isInstanceOf(TemplateDeletionException.class)
                .hasMessageContaining("Failed to delete template");
        
        verify(templateRepository).deleteById(testTemplateId);
    }
    
    // ========== CSV Import Operation Tests ==========
    
    @Test
    @DisplayName("importTemplateFromCsv - should import template successfully")
    void importTemplateFromCsv_ShouldImportSuccessfully() {
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
        
        Template savedTemplate = new Template();
        savedTemplate.setId(testTemplateId);
        savedTemplate.setPositionName("software engineer");
        
        List<TemplateTask> savedTasks = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            TemplateTask task = new TemplateTask();
            task.setId(UUID.randomUUID());
            task.setTemplate(savedTemplate);
            savedTasks.add(task);
        }
        
        when(templateRepository.existsByPositionNameIgnoreCase("software engineer")).thenReturn(false);
        when(templateRepository.save(any(Template.class))).thenReturn(savedTemplate);
        when(templateTaskRepository.saveAll(anyList())).thenReturn(savedTasks);
        
        // Act
        TemplateImportResponseDto result = templateService.importTemplateFromCsv(file);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTemplateId()).isEqualTo(testTemplateId);
        assertThat(result.getPositionName()).isEqualTo("Software Engineer");
        assertThat(result.getTasksImported()).isEqualTo(3);
        assertThat(result.getTaskIds()).hasSize(3);
        assertThat(result.getMessage()).contains("Successfully imported");
        
        verify(templateRepository).existsByPositionNameIgnoreCase("software engineer");
        verify(templateRepository).save(any(Template.class));
        verify(templateTaskRepository).saveAll(anyList());
    }
    
    @Test
    @DisplayName("importTemplateFromCsv - should throw CsvImportException when file is null")
    void importTemplateFromCsv_ShouldThrowException_WhenFileIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> templateService.importTemplateFromCsv(null))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("CSV file is required");
        
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("importTemplateFromCsv - should throw CsvImportException when file is empty")
    void importTemplateFromCsv_ShouldThrowException_WhenFileIsEmpty() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "template.csv",
                "text/csv",
                new byte[0]
        );
        
        // Act & Assert
        assertThatThrownBy(() -> templateService.importTemplateFromCsv(emptyFile))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("cannot be empty");
        
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("importTemplateFromCsv - should throw CsvImportException when file is not CSV")
    void importTemplateFromCsv_ShouldThrowException_WhenFileIsNotCsv() {
        // Arrange
        MockMultipartFile txtFile = new MockMultipartFile(
                "file",
                "template.txt",
                "text/plain",
                "some content".getBytes()
        );
        
        // Act & Assert
        assertThatThrownBy(() -> templateService.importTemplateFromCsv(txtFile))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining(".csv extension");
        
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("importTemplateFromCsv - should throw CsvImportException when file is too large")
    void importTemplateFromCsv_ShouldThrowException_WhenFileIsTooLarge() {
        // Arrange - create a file larger than 5MB
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "template.csv",
                "text/csv",
                largeContent
        );
        
        // Act & Assert
        assertThatThrownBy(() -> templateService.importTemplateFromCsv(largeFile))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("exceeds maximum allowed size");
        
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("importTemplateFromCsv - should throw CsvImportException when CSV has invalid header")
    void importTemplateFromCsv_ShouldThrowException_WhenCsvHasInvalidHeader() {
        // Arrange
        String csvContent = """
                invalid_header
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
        
        // Act & Assert
        assertThatThrownBy(() -> templateService.importTemplateFromCsv(file))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("position_name");
        
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("importTemplateFromCsv - should throw CsvImportException when position name is too long")
    void importTemplateFromCsv_ShouldThrowException_WhenPositionNameIsTooLong() {
        // Arrange
        String longPositionName = "A".repeat(51);
        String csvContent = String.format("""
                position_name
                %s
                title,description,task_order,owner_role
                Setup workstation,Install required software,1,USER
                """, longPositionName);
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "template.csv",
                "text/csv",
                csvContent.getBytes()
        );
        
        // Act & Assert
        assertThatThrownBy(() -> templateService.importTemplateFromCsv(file))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("exceeds maximum length of 50 characters");
        
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("importTemplateFromCsv - should throw DuplicatePositionNameException when position name exists")
    void importTemplateFromCsv_ShouldThrowException_WhenPositionNameExists() {
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
        
        when(templateRepository.existsByPositionNameIgnoreCase("software engineer")).thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> templateService.importTemplateFromCsv(file))
                .isInstanceOf(DuplicatePositionNameException.class)
                .hasMessageContaining("software engineer");
        
        verify(templateRepository).existsByPositionNameIgnoreCase("software engineer");
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("importTemplateFromCsv - should throw CsvImportException when task header is missing")
    void importTemplateFromCsv_ShouldThrowException_WhenTaskHeaderIsMissing() {
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
        
        Template savedTemplate = new Template();
        savedTemplate.setId(testTemplateId);
        savedTemplate.setPositionName("software engineer");
        
        when(templateRepository.existsByPositionNameIgnoreCase("software engineer")).thenReturn(false);
        when(templateRepository.save(any(Template.class))).thenReturn(savedTemplate);
        
        // Act & Assert
        assertThatThrownBy(() -> templateService.importTemplateFromCsv(file))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("4 columns");
        
        verify(templateTaskRepository, never()).saveAll(anyList());
    }
    
    @Test
    @DisplayName("importTemplateFromCsv - should throw CsvImportException when task_order is invalid")
    void importTemplateFromCsv_ShouldThrowException_WhenTaskOrderIsInvalid() {
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
        
        when(templateRepository.existsByPositionNameIgnoreCase("software engineer")).thenReturn(false);
        when(templateRepository.save(any(Template.class))).thenReturn(testTemplate);
        
        // Act & Assert
        assertThatThrownBy(() -> templateService.importTemplateFromCsv(file))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("task_order must be a valid integer");
        
        verify(templateTaskRepository, never()).saveAll(anyList());
    }
    
    @Test
    @DisplayName("importTemplateFromCsv - should throw CsvImportException when owner_role is invalid")
    void importTemplateFromCsv_ShouldThrowException_WhenOwnerRoleIsInvalid() {
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
        
        when(templateRepository.existsByPositionNameIgnoreCase("software engineer")).thenReturn(false);
        when(templateRepository.save(any(Template.class))).thenReturn(testTemplate);
        
        // Act & Assert
        assertThatThrownBy(() -> templateService.importTemplateFromCsv(file))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("owner_role must be either 'MANAGER' or 'USER'");
        
        verify(templateTaskRepository, never()).saveAll(anyList());
    }
    
    @Test
    @DisplayName("importTemplateFromCsv - should throw CsvImportException when title is empty")
    void importTemplateFromCsv_ShouldThrowException_WhenTitleIsEmpty() {
        // Arrange
        String csvContent = """
                position_name
                Software Engineer
                title,description,task_order,owner_role
                ,Install required software,1,USER
                """;
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "template.csv",
                "text/csv",
                csvContent.getBytes()
        );
        
        when(templateRepository.existsByPositionNameIgnoreCase("software engineer")).thenReturn(false);
        when(templateRepository.save(any(Template.class))).thenReturn(testTemplate);
        
        // Act & Assert
        assertThatThrownBy(() -> templateService.importTemplateFromCsv(file))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("title is required");
        
        verify(templateTaskRepository, never()).saveAll(anyList());
    }
    
    @Test
    @DisplayName("importTemplateFromCsv - should throw CsvImportException when CSV has no tasks")
    void importTemplateFromCsv_ShouldThrowException_WhenCsvHasNoTasks() {
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
        
        // Act & Assert - błąd jest wykrywany podczas walidacji CSV (za mało wierszy)
        // więc nie potrzebujemy mockować repozytorium
        assertThatThrownBy(() -> templateService.importTemplateFromCsv(file))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("at least");
    }
    
    @Test
    @DisplayName("importTemplateFromCsv - should skip empty rows in CSV")
    void importTemplateFromCsv_ShouldSkipEmptyRows() {
        // Arrange
        String csvContent = """
                position_name
                Software Engineer
                title,description,task_order,owner_role
                Setup workstation,Install required software,1,USER
                
                Meet the team,Introduction meeting,2,MANAGER
                """;
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "template.csv",
                "text/csv",
                csvContent.getBytes()
        );
        
        Template savedTemplate = new Template();
        savedTemplate.setId(testTemplateId);
        savedTemplate.setPositionName("software engineer");
        
        List<TemplateTask> savedTasks = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            TemplateTask task = new TemplateTask();
            task.setId(UUID.randomUUID());
            savedTasks.add(task);
        }
        
        when(templateRepository.existsByPositionNameIgnoreCase("software engineer")).thenReturn(false);
        when(templateRepository.save(any(Template.class))).thenReturn(savedTemplate);
        when(templateTaskRepository.saveAll(anyList())).thenReturn(savedTasks);
        
        // Act
        TemplateImportResponseDto result = templateService.importTemplateFromCsv(file);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTasksImported()).isEqualTo(2);
        
        verify(templateTaskRepository).saveAll(anyList());
    }
}

