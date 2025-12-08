package com.narciarz.benew.services;

import com.narciarz.benew.models.OnboardingTask;
import com.narciarz.benew.models.dto.ManagerTaskResponseDto;
import com.narciarz.benew.models.dto.OnboardingTaskResponseDto;
import com.narciarz.benew.models.dto.UpdateOnboardingTaskRequestDto;
import org.mapstruct.*;

/**
 * MapStruct mapper for converting between OnboardingTask entity and OnboardingTask DTOs.
 * 
 * <p>MapStruct generates the implementation at compile time, providing type-safe
 * and performant mapping between entity and DTO layers. The generated implementation
 * is registered as a Spring bean (componentModel = "spring").</p>
 * 
 * <p>Key mapping behaviors:</p>
 * <ul>
 *   <li>Maps nested onboarding process relationship to process ID</li>
 *   <li>Partial updates only modify non-null DTO fields</li>
 *   <li>ID and timestamps are managed by JPA and excluded from request mappings</li>
 * </ul>
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OnboardingTaskMapper {
    
    /**
     * Maps OnboardingTask entity to OnboardingTaskResponseDto.
     * 
     * <p>Extracts onboarding process ID from nested relationship:</p>
     * <ul>
     *   <li>onboardingProcess.id → onboardingProcessId</li>
     * </ul>
     * 
     * @param task the onboarding task entity
     * @return onboarding task response DTO
     */
    @Mapping(target = "onboardingProcessId", source = "onboardingProcess.id")
    OnboardingTaskResponseDto toResponseDto(OnboardingTask task);
    
    /**
     * Updates an existing OnboardingTask entity with data from UpdateOnboardingTaskRequestDto.
     * 
     * <p>Only non-null fields in the DTO are applied to the entity, allowing
     * partial updates. Typically used for marking tasks as completed/incomplete.</p>
     * 
     * <p>ID, timestamps, and relationships are excluded as they should never be updated manually.</p>
     * 
     * @param dto the update request DTO
     * @param task the existing task entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "onboardingProcess", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "taskOrder", ignore = true)
    @Mapping(target = "ownerRole", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateOnboardingTaskRequestDto dto, @MappingTarget OnboardingTask task);
    
    /**
     * Maps OnboardingTask entity to ManagerTaskResponseDto with user information.
     * 
     * <p>Extracts task information and employee details from nested relationships:</p>
     * <ul>
     *   <li>onboardingProcess.id → processId</li>
     *   <li>onboardingProcess.user.id → userId</li>
     *   <li>onboardingProcess.user.firstName → userFirstName</li>
     *   <li>onboardingProcess.user.lastName → userLastName</li>
     *   <li>onboardingProcess.user.positionName → userPosition</li>
     *   <li>onboardingProcess.status → processStatus</li>
     * </ul>
     * 
     * @param task the onboarding task entity
     * @return manager task response DTO with employee information
     */
    @Mapping(target = "processId", source = "onboardingProcess.id")
    @Mapping(target = "userId", source = "onboardingProcess.user.id")
    @Mapping(target = "userFirstName", source = "onboardingProcess.user.firstName")
    @Mapping(target = "userLastName", source = "onboardingProcess.user.lastName")
    @Mapping(target = "userPosition", source = "onboardingProcess.user.positionName")
    @Mapping(target = "processStatus", source = "onboardingProcess.status")
    ManagerTaskResponseDto toManagerTaskResponseDto(OnboardingTask task);
}

