package com.narciarz.benew.repositories;

import com.narciarz.benew.models.TemplateTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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
    
    /**
     * Find all tasks associated with a specific template.
     * Tasks are returned in order by taskOrder field.
     * 
     * @param templateId the template ID
     * @return list of template tasks ordered by taskOrder
     */
    List<TemplateTask> findByTemplateIdOrderByTaskOrderAsc(UUID templateId);
    
    /**
     * Find a specific task by ID and template ID.
     * Useful for validating that a task belongs to the specified template.
     * 
     * @param id the task ID
     * @param templateId the template ID
     * @return Optional containing the task if found
     */
    Optional<TemplateTask> findByIdAndTemplateId(UUID id, UUID templateId);
}

