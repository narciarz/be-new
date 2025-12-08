package com.narciarz.benew.services;

import com.narciarz.benew.models.Template;
import com.narciarz.benew.models.TemplateTask;
import com.narciarz.benew.models.dto.TemplateImportResponseDto;
import com.narciarz.benew.repositories.TemplateRepository;
import com.narciarz.benew.repositories.TemplateTaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for CSV import using real PostgreSQL via Testcontainers.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class TemplateImportIntegrationTest {

    @Container
    @SuppressWarnings("resource") // Managed by Testcontainers/JUnit lifecycle
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("benew")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("sql/init-benew-schema.sql"); // ensure schema "benew" exists

    @DynamicPropertySource
    static void overrideDataSourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private TemplateService templateService;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private TemplateTaskRepository templateTaskRepository;

    @Test
    @SuppressWarnings("resource") // MockMultipartFile triggers false positive in static analysis
    @DisplayName("importTemplateFromCsv persists template and tasks in PostgreSQL")
    void importTemplateFromCsv_PersistsData() {
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

        // Act
        TemplateImportResponseDto response = templateService.importTemplateFromCsv(file);

        // Assert
        Optional<Template> templateOpt = templateRepository.findByPositionNameIgnoreCase("software engineer");
        assertThat(templateOpt).isPresent();
        Template template = templateOpt.get();
        assertThat(response.getTemplateId()).isEqualTo(template.getId());
        assertThat(response.getTasksImported()).isEqualTo(3);

        List<TemplateTask> tasks = templateTaskRepository.findByTemplateIdOrderByTaskOrderAsc(template.getId());
        assertThat(tasks).hasSize(3);
        assertThat(tasks)
                .extracting(TemplateTask::getTaskOrder)
                .containsExactly(1, 2, 3);
        assertThat(tasks)
                .extracting(TemplateTask::getOwnerRole)
                .extracting(Enum::name)
                .containsExactly("USER", "MANAGER", "USER");
    }
}

