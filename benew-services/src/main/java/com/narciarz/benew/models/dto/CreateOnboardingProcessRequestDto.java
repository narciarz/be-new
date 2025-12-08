package com.narciarz.benew.models.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for creating a new onboarding process.
 * 
 * <p>Used by POST /onboarding to create a new onboarding process.
 * Typically triggered upon creation of a user account. The backend copies tasks
 * from the relevant template.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOnboardingProcessRequestDto {
    
    /**
     * Employee user ID being onboarded.
     */
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    /**
     * Manager user ID overseeing this onboarding.
     */
    @NotNull(message = "Manager ID is required")
    private UUID managerId;
    
    /**
     * Template ID to use as source for copying tasks.
     */
    @NotNull(message = "Source template ID is required")
    private UUID sourceTemplateId;
}


