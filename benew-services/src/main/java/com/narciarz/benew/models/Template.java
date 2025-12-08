package com.narciarz.benew.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing an onboarding checklist template in the template table.
 * 
 * <p>Header table for onboarding checklists, one per job position. Tasks are defined
 * in the TemplateTask entity and copied to OnboardingTask when a process starts.</p>
 * 
 * <p>Key design decisions:</p>
 * <ul>
 *   <li>Templates are tied to job positions (positionName must be unique)</li>
 *   <li>Template versioning via denormalization: tasks are COPIED to onboarding_task
 *       when process is created, so changes to template don't affect active onboardings</li>
 *   <li>Application normalizes positionName (trim + toLowerCase) before save/search</li>
 *   <li>Case-insensitive uniqueness enforced via database functional index</li>
 *   <li>Audit columns managed by Spring Data JPA Auditing (inherited from BaseEntity)</li>
 * </ul>
 */
@Entity
@Table(name = "template", schema = "benew")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Template extends BaseEntity {
    
    /**
     * Unique job position name that this template applies to (e.g., "Software Engineer").
     * Application normalizes this value (trim + toLowerCase) before save/search.
     * Case-insensitive uniqueness enforced via functional index.
     */
    @Column(name = "position_name", nullable = false, unique = true, length = 50)
    private String positionName;
}


