package com.narciarz.benew.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing an employee onboarding process in the onboarding_process table.
 * 
 * <p>Represents one employee's onboarding journey from start to completion/archive.
 * Automatically created when employee account is created. Tasks are copied from
 * template_task to onboarding_task at creation time.</p>
 * 
 * <p>Key design decisions:</p>
 * <ul>
 *   <li>Process is automatically created when employee account is created</li>
 *   <li>Tasks are COPIED from template_task (not referenced), so template changes
 *       don't affect active processes (versioning via denormalization)</li>
 *   <li>Progress counters (totalTasksCount, completedTasksCount) are denormalized
 *       for fast dashboard reads without expensive COUNT() queries</li>
 *   <li>status field supports ACTIVE and ARCHIVED states</li>
 *   <li>ON DELETE RESTRICT on all foreign keys forces application to handle deletions</li>
 *   <li>Audit columns managed by Spring Data JPA Auditing (inherited from BaseEntity)</li>
 * </ul>
 */
@Entity
@Table(name = "onboarding_process", schema = "benew")
@Getter
@Setter
@NoArgsConstructor
public class OnboardingProcess extends BaseEntity {
    
    /**
     * Foreign key to employee being onboarded (benew.app_user).
     * One user can have multiple processes (e.g., if changing positions).
     * ON DELETE RESTRICT prevents deletion of users with active onboarding.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_onboarding_process_user"))
    private AppUser user;
    
    /**
     * Foreign key to manager overseeing this onboarding (benew.app_user).
     * Manager sees all their employees' processes on dashboard.
     * ON DELETE RESTRICT prevents deletion of managers overseeing processes.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manager_id", nullable = false, foreignKey = @ForeignKey(name = "fk_onboarding_process_manager"))
    private AppUser manager;
    
    /**
     * Foreign key to template used to create this process (benew.template).
     * Kept for audit/reporting purposes only. Actual tasks are copied to onboarding_task.
     * ON DELETE RESTRICT prevents deletion of templates used in processes.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_template_id", nullable = false, foreignKey = @ForeignKey(name = "fk_onboarding_process_template"))
    private Template sourceTemplate;
    
    /**
     * Process status: ACTIVE or ARCHIVED.
     * ACTIVE = currently ongoing, ARCHIVED = completed and moved to archive.
     * Stored as VARCHAR in database, validated at application level.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OnboardingStatus status;
    
    /**
     * Denormalized count of all tasks. Updated by application when tasks added.
     * Enables fast progress calculation without COUNT() query.
     */
    @Column(name = "total_tasks_count", nullable = false)
    private Integer totalTasksCount = 0;
    
    /**
     * Denormalized count of completed tasks. Updated by application when tasks marked done.
     * Progress % = (completedTasksCount / totalTasksCount) * 100.
     */
    @Column(name = "completed_tasks_count", nullable = false)
    private Integer completedTasksCount = 0;

    public OnboardingProcess(AppUser user, AppUser manager, Template sourceTemplate, 
                            OnboardingStatus status) {
        this.user = user;
        this.manager = manager;
        this.sourceTemplate = sourceTemplate;
        this.status = status;
        this.totalTasksCount = 0;
        this.completedTasksCount = 0;
    }
}


