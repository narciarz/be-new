package com.narciarz.benew.repositories;

import com.narciarz.benew.models.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Template entity.
 * 
 * <p>Provides CRUD operations and custom query methods for template management.
 * Uses Spring Data JPA for automatic implementation.</p>
 * 
 * <p>Position names are normalized (trim + toLowerCase) before storage and
 * uniqueness is enforced by a functional database index on LOWER(TRIM(position_name)).</p>
 */
@Repository
public interface TemplateRepository extends JpaRepository<Template, UUID> {
    
    /**
     * Find template by position name (case-insensitive).
     * 
     * @param positionName position name to search for
     * @return Optional containing template if found
     */
    Optional<Template> findByPositionNameIgnoreCase(String positionName);
    
    /**
     * Check if template exists by position name (case-insensitive).
     * Useful for validation without fetching the full entity.
     * 
     * @param positionName position name to check
     * @return true if template exists with given position name
     */
    boolean existsByPositionNameIgnoreCase(String positionName);
    
    /**
     * Find templates by position name containing (case-insensitive, partial match).
     * Leverages database index on position_name for performance.
     * 
     * @param positionName position name substring to search
     * @param pageable pagination parameters
     * @return page of templates matching the position name
     */
    Page<Template> findByPositionNameContainingIgnoreCase(String positionName, Pageable pageable);
}

