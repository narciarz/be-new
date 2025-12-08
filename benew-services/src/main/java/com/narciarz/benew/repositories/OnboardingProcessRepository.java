package com.narciarz.benew.repositories;

import com.narciarz.benew.models.OnboardingProcess;
import com.narciarz.benew.models.OnboardingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for OnboardingProcess entity.
 * 
 * <p>Provides CRUD operations and custom query methods for onboarding process management.
 * Uses Spring Data JPA for automatic implementation.</p>
 * 
 * <p>Supports filtering by:</p>
 * <ul>
 *   <li>Status (ACTIVE, ARCHIVED)</li>
 *   <li>Manager (to view all processes for managed employees)</li>
 *   <li>User (to view specific employee's process)</li>
 * </ul>
 */
@Repository
public interface OnboardingProcessRepository extends JpaRepository<OnboardingProcess, UUID> {
    
    /**
     * Find all onboarding processes by status with pagination.
     * 
     * @param status process status filter (ACTIVE or ARCHIVED)
     * @param pageable pagination parameters
     * @return page of processes with given status
     */
    Page<OnboardingProcess> findByStatus(OnboardingStatus status, Pageable pageable);
    
    /**
     * Find all onboarding processes managed by a specific manager.
     * Manager dashboard uses this to view all their employees' onboarding.
     * 
     * @param managerId manager's user ID
     * @param pageable pagination parameters
     * @return page of processes managed by the manager
     */
    Page<OnboardingProcess> findByManagerId(UUID managerId, Pageable pageable);
    
    /**
     * Find all onboarding processes for a specific user (employee).
     * Supports use case where user changes positions (multiple processes).
     * 
     * @param userId employee's user ID
     * @param pageable pagination parameters
     * @return page of processes for the user
     */
    Page<OnboardingProcess> findByUserId(UUID userId, Pageable pageable);
    
    /**
     * Find all onboarding processes by manager and status.
     * Allows manager to filter between active and archived processes.
     * 
     * @param managerId manager's user ID
     * @param status process status filter
     * @param pageable pagination parameters
     * @return page of processes matching criteria
     */
    Page<OnboardingProcess> findByManagerIdAndStatus(UUID managerId, OnboardingStatus status, 
                                                      Pageable pageable);
    
    /**
     * Find all onboarding processes using a specific template.
     * Useful for reporting and preventing template deletion.
     * 
     * @param sourceTemplateId template ID
     * @param pageable pagination parameters
     * @return page of processes using the template
     */
    Page<OnboardingProcess> findBySourceTemplateId(UUID sourceTemplateId, Pageable pageable);
    
    /**
     * Count onboarding processes using a specific template.
     * Used to enforce deletion constraint (templates in use cannot be deleted).
     * 
     * @param sourceTemplateId template ID
     * @return count of processes using the template
     */
    long countBySourceTemplateId(UUID sourceTemplateId);
    
    /**
     * Find all onboarding processes with eager loading of user, manager, and template.
     * Prevents N+1 query problem when accessing related entities.
     * 
     * @param pageable pagination parameters
     * @return page of processes with related entities loaded
     */
    @Query("SELECT op FROM OnboardingProcess op " +
           "LEFT JOIN FETCH op.user " +
           "LEFT JOIN FETCH op.manager " +
           "LEFT JOIN FETCH op.sourceTemplate " +
           "WHERE op IN (SELECT op2 FROM OnboardingProcess op2)")
    Page<OnboardingProcess> findAllWithRelations(Pageable pageable);
    
    /**
     * Count onboarding processes for a specific user.
     * Used to check if user has processes before deletion.
     * 
     * @param userId user's ID
     * @return count of processes for the user
     */
    long countByUserId(UUID userId);
    
    /**
     * Delete all onboarding processes for a specific user.
     * Used for cascade deletion when removing a user.
     * 
     * @param userId user's ID
     */
    void deleteByUserId(UUID userId);
}

