package com.narciarz.benew.services;

import com.narciarz.benew.models.OnboardingProcess;
import com.narciarz.benew.models.dto.OnboardingProcessResponseDto;
import com.narciarz.benew.models.dto.UpdateOnboardingProcessRequestDto;
import org.mapstruct.*;

/**
 * MapStruct mapper for converting between OnboardingProcess entity and Onboarding DTOs.
 * 
 * <p>MapStruct generates the implementation at compile time, providing type-safe
 * and performant mapping between entity and DTO layers. The generated implementation
 * is registered as a Spring bean (componentModel = "spring").</p>
 * 
 * <p>Key mapping behaviors:</p>
 * <ul>
 *   <li>Maps nested relationships (user, manager, template) to IDs and display names</li>
 *   <li>Calculates progress percentage from task counters</li>
 *   <li>Partial updates only modify non-null DTO fields</li>
 *   <li>ID and timestamps are managed by JPA and excluded from request mappings</li>
 * </ul>
 */
@Mapper(
    componentModel = "spring", 
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OnboardingMapper {
    
    /**
     * Maps OnboardingProcess entity to OnboardingProcessResponseDto.
     * 
     * <p>Extracts nested relationships to flat DTO structure:</p>
     * <ul>
     *   <li>user.id → userId</li>
     *   <li>user.firstName + user.lastName → userName</li>
     *   <li>manager.id → managerId</li>
     *   <li>manager.firstName + manager.lastName → managerName</li>
     *   <li>sourceTemplate.id → sourceTemplateId</li>
     *   <li>sourceTemplate.positionName → sourceTemplateName</li>
     * </ul>
     * 
     * <p>Progress percentage is calculated: (completedTasksCount / totalTasksCount) * 100</p>
     * 
     * @param process the onboarding process entity
     * @return onboarding process response DTO
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(getUserFullName(process))")
    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "managerName", expression = "java(getManagerFullName(process))")
    @Mapping(target = "sourceTemplateId", source = "sourceTemplate.id")
    @Mapping(target = "sourceTemplateName", source = "sourceTemplate.positionName")
    @Mapping(target = "progressPercentage", expression = "java(calculateProgressPercentage(process))")
    OnboardingProcessResponseDto toResponseDto(OnboardingProcess process);
    
    /**
     * Updates an existing OnboardingProcess entity with data from UpdateOnboardingProcessRequestDto.
     * 
     * <p>Only non-null fields in the DTO are applied to the entity, allowing
     * partial updates. This is controlled by {@code nullValuePropertyMappingStrategy = IGNORE}
     * at the mapper level. Typically used for:</p>
     * <ul>
     *   <li>Archiving process (status = ARCHIVED)</li>
     *   <li>Manually adjusting task counters if needed</li>
     * </ul>
     * 
     * <p>ID, timestamps, and relationships are excluded as they should never be updated manually.</p>
     * 
     * @param dto the update request DTO
     * @param process the existing process entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "sourceTemplate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateOnboardingProcessRequestDto dto, @MappingTarget OnboardingProcess process);
    
    /**
     * Helper method to construct user's full name.
     * 
     * @param process the onboarding process
     * @return user's full name (firstName + lastName)
     */
    default String getUserFullName(OnboardingProcess process) {
        if (process.getUser() == null) {
            return null;
        }
        return process.getUser().getFirstName() + " " + process.getUser().getLastName();
    }
    
    /**
     * Helper method to construct manager's full name.
     * 
     * @param process the onboarding process
     * @return manager's full name (firstName + lastName)
     */
    default String getManagerFullName(OnboardingProcess process) {
        if (process.getManager() == null) {
            return null;
        }
        return process.getManager().getFirstName() + " " + process.getManager().getLastName();
    }
    
    /**
     * Helper method to calculate progress percentage.
     * 
     * @param process the onboarding process
     * @return progress percentage (0-100), or 0 if no tasks
     */
    default Double calculateProgressPercentage(OnboardingProcess process) {
        if (process.getTotalTasksCount() == null || process.getTotalTasksCount() == 0) {
            return 0.0;
        }
        int completed = process.getCompletedTasksCount() != null ? process.getCompletedTasksCount() : 0;
        return (completed * 100.0) / process.getTotalTasksCount();
    }
}

