package com.narciarz.benew.services;

import com.narciarz.benew.exceptions.DuplicatePositionNameException;
import com.narciarz.benew.exceptions.TemplateDeletionException;
import com.narciarz.benew.exceptions.TemplateNotFoundException;
import com.narciarz.benew.models.Template;
import com.narciarz.benew.models.dto.CreateTemplateRequestDto;
import com.narciarz.benew.models.dto.UpdateTemplateRequestDto;
import com.narciarz.benew.models.dto.TemplateResponseDto;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
}

