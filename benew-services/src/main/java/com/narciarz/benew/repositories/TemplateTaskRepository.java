package com.narciarz.benew.repositories;

import com.narciarz.benew.models.TemplateTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for TemplateTask entity.
 * 
 * <p>Provides CRUD operations and custom query methods for template task management.
 * Uses Spring Data JPA for automatic implementation.</p>
 */
@Repository
public interface TemplateTaskRepository extends JpaRepository<TemplateTask, UUID> {
    
    /**
     * Count template tasks associated with a specific template.
     * Useful for checking if a template has tasks before deletion.
     * 
     * @param templateId the template ID
     * @return count of tasks associated with the template
     */
    long countByTemplateId(UUID templateId);
}

