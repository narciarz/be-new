package com.narciarz.benew.services;

import com.narciarz.benew.models.Template;
import com.narciarz.benew.models.dto.CreateTemplateRequestDto;
import com.narciarz.benew.models.dto.UpdateTemplateRequestDto;
import com.narciarz.benew.models.dto.TemplateResponseDto;
import org.mapstruct.*;

/**
 * MapStruct mapper for converting between Template entity and Template DTOs.
 * 
 * <p>MapStruct generates the implementation at compile time, providing type-safe
 * and performant mapping between entity and DTO layers. The generated implementation
 * is registered as a Spring bean (componentModel = "spring").</p>
 * 
 * <p>Key mapping behaviors:</p>
 * <ul>
 *   <li>Position name normalization is handled by the service layer</li>
 *   <li>Partial updates only modify non-null DTO fields</li>
 *   <li>ID and timestamps are managed by JPA and excluded from request mappings</li>
 * </ul>
 */
@Mapper(
    componentModel = "spring", 
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TemplateMapper {
    
    /**
     * Maps Template entity to TemplateResponseDto.
     * 
     * <p>All template fields are included in the response, including the normalized
     * position name and audit timestamps.</p>
     * 
     * @param template the template entity
     * @return template response DTO
     */
    TemplateResponseDto toResponseDto(Template template);
    
    /**
     * Maps CreateTemplateRequestDto to Template entity.
     * 
     * <p>Note: Position name normalization (trim + lowercase) must be handled by
     * the service layer after this mapping.</p>
     * 
     * <p>ID and timestamps are excluded as they are managed by JPA:</p>
     * <ul>
     *   <li>ID is auto-generated on persist</li>
     *   <li>createdAt and updatedAt are managed by JPA Auditing</li>
     * </ul>
     * 
     * @param dto the create template request DTO
     * @return new template entity (without ID and timestamps)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Template toEntity(CreateTemplateRequestDto dto);
    
    /**
     * Updates an existing Template entity with data from UpdateTemplateRequestDto.
     * 
     * <p>Only non-null fields in the DTO are applied to the entity, allowing
     * partial updates. This is controlled by {@code nullValuePropertyMappingStrategy = IGNORE}
     * at the mapper level.</p>
     * 
     * <p>Position name normalization must be handled by the service layer if
     * position name is being updated.</p>
     * 
     * <p>ID and timestamps are excluded as they should never be updated manually:</p>
     * <ul>
     *   <li>ID is immutable</li>
     *   <li>createdAt is set once on creation</li>
     *   <li>updatedAt is managed by JPA Auditing</li>
     * </ul>
     * 
     * @param dto the update request DTO
     * @param template the existing template entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateTemplateRequestDto dto, @MappingTarget Template template);
}

