package com.narciarz.benew.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a task within an onboarding process in the onboarding_task table.
 * 
 * <p>Individual tasks for a specific onboarding process. Tasks are COPIED from template_task
 * when process is created. Changes to template tasks do NOT affect these tasks
 * (versioning via denormalization).</p>
 * 
 * <p>Key design decisions:</p>
 * <ul>
 *   <li>Tasks are copied from template_task (snapshots at time of process creation)</li>
 *   <li>Template changes will NOT affect these values</li>
 *   <li>isCompleted toggle triggers counter updates in onboarding_process</li>
 *   <li>ON DELETE RESTRICT prevents deletion of processes with tasks</li>
 *   <li>Audit columns managed by Spring Data JPA Auditing (inherited from BaseEntity)</li>
 * </ul>
 */
@Entity
@Table(name = "onboarding_task", schema = "benew")
@Getter
@Setter
@NoArgsConstructor
public class OnboardingTask extends BaseEntity {
    
    /**
     * Foreign key to parent onboarding process. One process has many tasks.
     * ON DELETE RESTRICT prevents deletion of processes with tasks.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "onboarding_process_id", nullable = false, foreignKey = @ForeignKey(name = "fk_onboarding_task_process"))
    private OnboardingProcess onboardingProcess;
    
    /**
     * Task title. Copied from template_task.title at process creation.
     * Template changes do NOT update this value.
     */
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    /**
     * Task description. Copied from template_task.description at process creation.
     * Template changes do NOT update this value.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * Display order in checklist. Copied from template_task.task_order at process creation.
     */
    @Column(name = "task_order", nullable = false)
    private Integer taskOrder;
    
    /**
     * Role responsible for completing task: MANAGER or USER.
     * Copied from template_task.owner_role at process creation.
     * Stored as VARCHAR in database, validated at application level.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "owner_role", nullable = false, length = 50)
    private TaskOwnerRole ownerRole;
    
    /**
     * Task completion status. Set to true by task owner (manager or employee).
     * Triggers update of completed_tasks_count in onboarding_process table.
     */
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    public OnboardingTask(OnboardingProcess onboardingProcess, String title, String description,
                         Integer taskOrder, TaskOwnerRole ownerRole) {
        this.onboardingProcess = onboardingProcess;
        this.title = title;
        this.description = description;
        this.taskOrder = taskOrder;
        this.ownerRole = ownerRole;
        this.isCompleted = false;
    }
}


