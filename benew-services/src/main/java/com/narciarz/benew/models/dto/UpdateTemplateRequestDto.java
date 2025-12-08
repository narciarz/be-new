package com.narciarz.benew.models.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for updating an existing template.
 * 
 * <p>Used by PUT /templates/{templateId} to update template details.
 * All fields are optional - only provided fields will be updated.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTemplateRequestDto {
    
    /**
     * Job position name. Must be unique if provided.
     * Application normalizes this value (trim + toLowerCase) before save.
     */
    @Size(max = 50, message = "Position name must not exceed 50 characters")
    private String positionName;
}


