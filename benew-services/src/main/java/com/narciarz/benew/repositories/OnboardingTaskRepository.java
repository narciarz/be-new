package com.narciarz.benew.repositories;

import com.narciarz.benew.models.OnboardingTask;
import com.narciarz.benew.models.TaskOwnerRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for OnboardingTask entity.
 * 
 * <p>Provides CRUD operations and custom query methods for onboarding task management.
 * Uses Spring Data JPA for automatic implementation.</p>
 */
@Repository
public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, UUID> {
    
    /**
     * Find all tasks associated with a specific onboarding process.
     * Tasks are returned ordered by taskOrder field for checklist display.
     * 
     * @param onboardingProcessId the onboarding process ID
     * @return list of tasks ordered by taskOrder
     */
    List<OnboardingTask> findByOnboardingProcessIdOrderByTaskOrderAsc(UUID onboardingProcessId);
    
    /**
     * Find a specific task by ID and onboarding process ID.
     * Useful for validating that a task belongs to the specified process.
     * 
     * @param id the task ID
     * @param onboardingProcessId the onboarding process ID
     * @return Optional containing the task if found
     */
    Optional<OnboardingTask> findByIdAndOnboardingProcessId(UUID id, UUID onboardingProcessId);
    
    /**
     * Count tasks associated with a specific onboarding process.
     * Used for validation and counter synchronization.
     * 
     * @param onboardingProcessId the onboarding process ID
     * @return count of tasks in the process
     */
    long countByOnboardingProcessId(UUID onboardingProcessId);
    
    /**
     * Count completed tasks for a specific onboarding process.
     * Used for progress calculation and counter synchronization.
     * 
     * @param onboardingProcessId the onboarding process ID
     * @return count of completed tasks in the process
     */
    long countByOnboardingProcessIdAndIsCompletedTrue(UUID onboardingProcessId);
    
    /**
     * Delete all tasks associated with a specific onboarding process.
     * Called when process is permanently deleted (cascade operation).
     * 
     * @param onboardingProcessId the onboarding process ID
     */
    void deleteByOnboardingProcessId(UUID onboardingProcessId);
    
    /**
     * Find all tasks with specific owner role for active onboarding processes managed by a specific manager.
     * Used by managers to view and complete tasks assigned to them.
     * 
     * @param managerId the manager's user ID
     * @param status the process status (typically ACTIVE)
     * @param ownerRole the task owner role (typically MANAGER)
     * @return list of tasks ordered by process creation date and task order
     */
    @Query("SELECT t FROM OnboardingTask t " +
           "JOIN t.onboardingProcess p " +
           "WHERE p.manager.id = :managerId " +
           "AND p.status = :status " +
           "AND t.ownerRole = :ownerRole " +
           "ORDER BY p.createdAt DESC, t.taskOrder ASC")
    List<OnboardingTask> findManagerTasks(@Param("managerId") UUID managerId,
                                          @Param("status") com.narciarz.benew.models.OnboardingStatus status,
                                          @Param("ownerRole") TaskOwnerRole ownerRole);
}

