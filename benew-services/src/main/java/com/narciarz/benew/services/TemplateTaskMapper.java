package com.narciarz.benew.services;

import com.narciarz.benew.models.TemplateTask;
import com.narciarz.benew.models.dto.CreateTemplateTaskRequestDto;
import com.narciarz.benew.models.dto.UpdateTemplateTaskRequestDto;
import com.narciarz.benew.models.dto.TemplateTaskResponseDto;
import org.mapstruct.*;

/**
 * MapStruct mapper for converting between TemplateTask entity and TemplateTask DTOs.
 * 
 * <p>MapStruct generates the implementation at compile time, providing type-safe
 * and performant mapping between entity and DTO layers. The generated implementation
 * is registered as a Spring bean (componentModel = "spring").</p>
 * 
 * <p>Key mapping behaviors:</p>
 * <ul>
 *   <li>Template reference must be set by the service layer</li>
 *   <li>Partial updates only modify non-null DTO fields</li>
 *   <li>ID and timestamps are managed by JPA and excluded from request mappings</li>
 * </ul>
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TemplateTaskMapper {
    
    /**
     * Maps TemplateTask entity to TemplateTaskResponseDto.
     * 
     * <p>All task fields are included in the response, including the parent
     * template ID and audit timestamps.</p>
     * 
     * @param templateTask the template task entity
     * @return template task response DTO
     */
    @Mapping(target = "templateId", source = "template.id")
    TemplateTaskResponseDto toResponseDto(TemplateTask templateTask);
    
    /**
     * Maps CreateTemplateTaskRequestDto to TemplateTask entity.
     * 
     * <p>Note: The template reference must be set by the service layer after
     * this mapping, as it requires fetching the Template entity.</p>
     * 
     * <p>ID and timestamps are excluded as they are managed by JPA:</p>
     * <ul>
     *   <li>ID is auto-generated on persist</li>
     *   <li>createdAt and updatedAt are managed by JPA Auditing</li>
     * </ul>
     * 
     * @param dto the create template task request DTO
     * @return new template task entity (without ID, template reference, and timestamps)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TemplateTask toEntity(CreateTemplateTaskRequestDto dto);
    
    /**
     * Updates an existing TemplateTask entity with data from UpdateTemplateTaskRequestDto.
     * 
     * <p>Only non-null fields in the DTO are applied to the entity, allowing
     * partial updates. This is controlled by {@code nullValuePropertyMappingStrategy = IGNORE}.</p>
     * 
     * <p>ID, template reference, and timestamps are excluded as they should never be
     * updated manually:</p>
     * <ul>
     *   <li>ID is immutable</li>
     *   <li>template reference should not change</li>
     *   <li>createdAt is set once on creation</li>
     *   <li>updatedAt is managed by JPA Auditing</li>
     * </ul>
     * 
     * @param dto the update request DTO
     * @param templateTask the existing template task entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateTemplateTaskRequestDto dto, @MappingTarget TemplateTask templateTask);
}

