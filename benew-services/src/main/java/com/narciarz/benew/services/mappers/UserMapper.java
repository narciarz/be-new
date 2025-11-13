package com.narciarz.benew.services.mappers;

import com.narciarz.benew.models.AppUser;
import com.narciarz.benew.models.dto.CreateUserRequestDto;
import com.narciarz.benew.models.dto.UpdateUserRequestDto;
import com.narciarz.benew.models.dto.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for converting between AppUser entity and User DTOs.
 * 
 * <p>MapStruct generates the implementation at compile time, providing type-safe
 * and performant mapping between entity and DTO layers. The generated implementation
 * is registered as a Spring bean (componentModel = "spring").</p>
 * 
 * <p>Key mapping behaviors:</p>
 * <ul>
 *   <li>Password hash is never exposed in response DTOs</li>
 *   <li>Manager relationship is mapped to managerId and managerName</li>
 *   <li>Partial updates only modify non-null DTO fields</li>
 *   <li>Password hashing is handled by the service layer</li>
 * </ul>
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    
    /**
     * Maps AppUser entity to UserResponseDto.
     * 
     * <p>Password hash is intentionally excluded from the response for security.
     * Manager information is mapped using {@link #mapManagerId} and {@link #mapManagerName}.</p>
     * 
     * @param user the user entity
     * @return user response DTO
     */
    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "managerName", expression = "java(mapManagerName(user))")
    UserResponseDto toResponseDto(AppUser user);
    
    /**
     * Maps CreateUserRequestDto to AppUser entity.
     * 
     * <p>Note: This method does NOT hash the password. Password hashing must be
     * handled by the service layer using Spring Security's PasswordEncoder.</p>
     * 
     * <p>Manager relationship must be set separately by the service layer after
     * validating the managerId exists.</p>
     * 
     * @param dto the create user request DTO
     * @return new user entity (without ID, timestamps, or manager set)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AppUser toEntity(CreateUserRequestDto dto);
    
    /**
     * Updates an existing AppUser entity with data from UpdateUserRequestDto.
     * 
     * <p>Only non-null fields in the DTO are applied to the entity, allowing
     * partial updates. This is controlled by {@code nullValuePropertyMappingStrategy = IGNORE}
     * at the mapper level.</p>
     * 
     * <p>Password hashing must be handled by the service layer if password is provided.
     * Manager relationship must be updated separately by the service layer after validation.</p>
     * 
     * @param dto the update request DTO
     * @param user the existing user entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateUserRequestDto dto, @MappingTarget AppUser user);
    
    /**
     * Helper method to map manager full name for response DTOs.
     * 
     * <p>Returns formatted "firstName lastName" if manager exists, null otherwise.</p>
     * 
     * @param user the user entity with potential manager relationship
     * @return manager full name or null
     */
    default String mapManagerName(AppUser user) {
        if (user == null || user.getManager() == null) {
            return null;
        }
        AppUser manager = user.getManager();
        return manager.getFirstName() + " " + manager.getLastName();
    }
}
