package com.narciarz.benew.repositories;

import com.narciarz.benew.models.AppUser;
import com.narciarz.benew.models.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for AppUser entity.
 * 
 * <p>Provides CRUD operations and custom query methods for user management.
 * Uses Spring Data JPA for automatic implementation.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<AppUser, UUID> {
    
    /**
     * Find user by email address (case-insensitive).
     * 
     * @param email user email address
     * @return Optional containing user if found
     */
    Optional<AppUser> findByEmailIgnoreCase(String email);
    
    /**
     * Check if user exists by email address (case-insensitive).
     * Useful for validation without fetching the full entity.
     * 
     * @param email user email address
     * @return true if user exists
     */
    boolean existsByEmailIgnoreCase(String email);
    
    /**
     * Find all users by role with pagination.
     * 
     * @param role user role filter
     * @param pageable pagination parameters
     * @return page of users with given role
     */
    Page<AppUser> findByRole(UserRole role, Pageable pageable);
    
    /**
     * Find all users managed by a specific manager.
     * 
     * @param managerId manager's user ID
     * @param pageable pagination parameters
     * @return page of users reporting to the manager
     */
    Page<AppUser> findByManagerId(UUID managerId, Pageable pageable);
    
    /**
     * Find users by position name (case-insensitive, partial match).
     * 
     * @param positionName position name to search
     * @param pageable pagination parameters
     * @return page of users matching the position
     */
    Page<AppUser> findByPositionNameContainingIgnoreCase(String positionName, Pageable pageable);
    
    /**
     * Find users by last name (case-insensitive, partial match).
     * Leverages database index on last_name for performance.
     * 
     * @param lastName last name to search
     * @param pageable pagination parameters
     * @return page of users matching the last name
     */
    Page<AppUser> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);
    
    /**
     * Count users managed by a specific manager.
     * Useful for checking if a manager has employees before deletion.
     * 
     * @param managerId manager's user ID
     * @return count of users reporting to the manager
     */
    long countByManagerId(UUID managerId);
    
    /**
     * Find all users with eager loading of manager relationship.
     * Prevents N+1 query problem when accessing manager data.
     * 
     * @param pageable pagination parameters
     * @return page of users with manager data loaded
     */
    @Query("SELECT u FROM AppUser u LEFT JOIN FETCH u.manager WHERE u IN " +
           "(SELECT u2 FROM AppUser u2)")
    Page<AppUser> findAllWithManager(Pageable pageable);
    
    /**
     * Find users by manager ID and role with pagination.
     * 
     * @param managerId manager's user ID
     * @param role user role filter
     * @param pageable pagination parameters
     * @return page of users matching manager and role
     */
    Page<AppUser> findByManagerIdAndRole(UUID managerId, UserRole role, Pageable pageable);
    
    /**
     * Find users by manager ID and position name (case-insensitive, partial match).
     * 
     * @param managerId manager's user ID
     * @param positionName position name to search
     * @param pageable pagination parameters
     * @return page of users matching manager and position
     */
    Page<AppUser> findByManagerIdAndPositionNameContainingIgnoreCase(
            UUID managerId, String positionName, Pageable pageable);
    
    /**
     * Find users by manager ID and last name (case-insensitive, partial match).
     * 
     * @param managerId manager's user ID
     * @param lastName last name to search
     * @param pageable pagination parameters
     * @return page of users matching manager and last name
     */
    Page<AppUser> findByManagerIdAndLastNameContainingIgnoreCase(
            UUID managerId, String lastName, Pageable pageable);
    
    /**
     * Find users by manager ID, role, and position name (case-insensitive, partial match).
     * 
     * @param managerId manager's user ID
     * @param role user role filter
     * @param positionName position name to search
     * @param pageable pagination parameters
     * @return page of users matching manager, role, and position
     */
    Page<AppUser> findByManagerIdAndRoleAndPositionNameContainingIgnoreCase(
            UUID managerId, UserRole role, String positionName, Pageable pageable);
    
    /**
     * Find users by manager ID, role, and last name (case-insensitive, partial match).
     * 
     * @param managerId manager's user ID
     * @param role user role filter
     * @param lastName last name to search
     * @param pageable pagination parameters
     * @return page of users matching manager, role, and last name
     */
    Page<AppUser> findByManagerIdAndRoleAndLastNameContainingIgnoreCase(
            UUID managerId, UserRole role, String lastName, Pageable pageable);
}
