package com.narciarz.benew.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a task within a template checklist in the template_task table.
 * 
 * <p>Individual tasks that make up a template checklist. When an onboarding process
 * is created, these tasks are COPIED to the onboarding_task table. Changes to template
 * tasks do NOT affect already active onboarding processes (versioning via denormalization).</p>
 * 
 * <p>Key design decisions:</p>
 * <ul>
 *   <li>Tasks belong to a parent template (one template can have many tasks)</li>
 *   <li>task_order controls display sequence in checklist</li>
 *   <li>owner_role (MANAGER or USER) determines who is responsible for completing task</li>
 *   <li>ON DELETE RESTRICT prevents deletion of templates that still have tasks</li>
 *   <li>Audit columns managed by Spring Data JPA Auditing (inherited from BaseEntity)</li>
 * </ul>
 */
@Entity
@Table(name = "template_task", schema = "benew")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateTask extends BaseEntity {
    
    /**
     * Foreign key to parent template. One template has many tasks.
     * ON DELETE RESTRICT prevents deletion of templates with tasks.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false, foreignKey = @ForeignKey(name = "fk_template_task_template"))
    private Template template;
    
    /**
     * Short task title displayed in checklist (max 255 chars). Required.
     */
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    /**
     * Optional detailed task description/instructions. Can be lengthy (TEXT type).
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * Display order in checklist. Lower numbers appear first.
     * Allows task reordering by administrators.
     */
    @Column(name = "task_order", nullable = false)
    private Integer taskOrder;
    
    /**
     * Role responsible for completing task: MANAGER or USER.
     * Stored as VARCHAR in database, validated at application level.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "owner_role", nullable = false, length = 50)
    private TaskOwnerRole ownerRole;
}


