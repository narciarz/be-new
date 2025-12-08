# Security Fix: Manager Role Filtering

## Critical Security Issue

**Severity:** HIGH  
**Date Fixed:** 2025-12-08  
**Issue:** Managers could view and edit ADMIN and MANAGER users who had them as a manager

## Problem Description

The initial implementation filtered users by `managerId` only, which meant:
- If an ADMIN user had a manager assigned, that manager could see and edit them
- If a MANAGER user had a manager assigned, their manager could see and edit them
- This violated the principle that managers should only manage USER role employees

### Example Scenario (Vulnerable)
```
Admin (Alice) reports to Manager (Bob)
Manager (Charlie) reports to Manager (Bob)
User (David) reports to Manager (Bob)

Bob could see and edit: Alice (ADMIN), Charlie (MANAGER), David (USER) ❌
Bob should only see: David (USER) ✅
```

## Root Cause

In `UserService`, all methods filtered only by `managerId`:

```java
// VULNERABLE CODE
if (currentUserRole == UserRole.MANAGER) {
    return userRepository.findByManagerId(currentUserId, pageable);
}
```

This returned ALL users with matching `manager_id`, regardless of their role.

## Solution

Added role-based filtering to ensure managers can only access USER role users:

### 1. Modified `getAllUsers()`

```java
// SECURE CODE
if (currentUserRole == UserRole.MANAGER) {
    log.debug("Manager {} fetching their USER role team members", currentUserId);
    return userRepository.findByManagerIdAndRole(currentUserId, UserRole.USER, pageable)
            .map(userMapper::toResponseDto);
}
```

### 2. Modified `getUsersByRole()`

```java
if (currentUserRole == UserRole.MANAGER) {
    // Managers can only query USER role
    if (role != UserRole.USER) {
        log.warn("Manager {} attempted to query role: {}", currentUserId, role);
        return Page.empty(pageable);
    }
    return userRepository.findByManagerIdAndRole(currentUserId, UserRole.USER, pageable)
            .map(userMapper::toResponseDto);
}
```

### 3. Modified `getUsersByManager()`

```java
if (currentUserRole == UserRole.MANAGER) {
    if (!managerId.equals(currentUserId)) {
        return Page.empty(pageable);
    }
    // Return only USER role members
    return userRepository.findByManagerIdAndRole(managerId, UserRole.USER, pageable)
            .map(userMapper::toResponseDto);
}
```

### 4. Modified `getUserById()`

```java
if (currentUserRole == UserRole.MANAGER) {
    // Check if user is USER role AND assigned to current manager
    if (user.getRole() != UserRole.USER || 
        user.getManager() == null || 
        !user.getManager().getId().equals(currentUserId)) {
        log.warn("Manager {} attempted to access user {} (role: {}) who is not a USER in their team", 
                currentUserId, userId, user.getRole());
        throw new UserNotFoundException(userId);
    }
}
```

### 5. Modified `updateUser()`

```java
if (currentUserRole == UserRole.MANAGER) {
    // Check if user is USER role and assigned to current manager
    if (user.getRole() != UserRole.USER || 
        user.getManager() == null || 
        !user.getManager().getId().equals(currentUserId)) {
        log.warn("Manager {} attempted to update user {} (role: {}) who is not a USER in their team", 
                currentUserId, userId, user.getRole());
        throw new UserNotFoundException(userId);
    }
    // ... rest of validation
}
```

### 6. Modified `deleteUser()`

```java
if (currentUserRole == UserRole.MANAGER) {
    // Check if user is USER role and assigned to current manager
    if (user.getRole() != UserRole.USER || 
        user.getManager() == null || 
        !user.getManager().getId().equals(currentUserId)) {
        log.warn("Manager {} attempted to delete user {} (role: {}) who is not a USER in their team", 
                currentUserId, userId, user.getRole());
        throw new UserNotFoundException(userId);
    }
}
```

## Security Validation Checklist

### ✅ Managers CAN:
- View list of USER role users assigned to them
- Get details of USER role users assigned to them
- Create new USER role users (auto-assigned to themselves)
- Update USER role users assigned to them (except role and manager fields)
- Delete USER role users assigned to them

### ❌ Managers CANNOT:
- View ADMIN users (even if manager_id points to them)
- View MANAGER users (even if manager_id points to them)
- Edit ADMIN or MANAGER users
- Delete ADMIN or MANAGER users
- Query for ADMIN or MANAGER roles (returns empty page)
- Change user roles
- Reassign users to other managers

### ✅ Admins CAN:
- Everything without restrictions
- View/edit/delete users of any role
- Assign any user to any manager
- Change user roles

## Testing Scenarios

### Test 1: Manager tries to list users
```
Given: Manager Bob with manager_id = UUID-BOB
And: Admin Alice with manager_id = UUID-BOB
And: Manager Charlie with manager_id = UUID-BOB
And: User David with manager_id = UUID-BOB

When: Bob calls GET /api/users
Then: Response contains only David (USER role)
And: Alice and Charlie are NOT in the response
```

### Test 2: Manager tries to access ADMIN user
```
Given: Manager Bob (UUID-BOB)
And: Admin Alice (UUID-ALICE) with manager_id = UUID-BOB

When: Bob calls GET /api/users/UUID-ALICE
Then: Response status is 404 Not Found
```

### Test 3: Manager tries to edit ADMIN user
```
Given: Manager Bob (UUID-BOB)
And: Admin Alice (UUID-ALICE) with manager_id = UUID-BOB

When: Bob calls PUT /api/users/UUID-ALICE with update data
Then: Response status is 404 Not Found
And: Alice's data remains unchanged
```

### Test 4: Manager queries for ADMIN role
```
Given: Manager Bob

When: Bob calls GET /api/users?role=ADMIN
Then: Response is empty page (no results)
```

### Test 5: Manager queries for USER role
```
Given: Manager Bob with USER David assigned
And: Admin Alice with manager_id = UUID-BOB (role=ADMIN)

When: Bob calls GET /api/users?role=USER
Then: Response contains only David
And: Alice is NOT in the response
```

## Response Codes

| Scenario | HTTP Status | Reason |
|----------|-------------|--------|
| Manager tries to access non-USER | 404 Not Found | Security by obscurity - don't reveal existence |
| Manager tries to edit non-USER | 404 Not Found | Security by obscurity |
| Manager tries to delete non-USER | 404 Not Found | Security by obscurity |
| Manager queries for ADMIN/MANAGER role | 200 OK (empty) | Valid request, no matches |
| Manager tries to change user role | 400 Bad Request | Explicitly forbidden |

## Logging

All security violations are logged at WARN level:

```java
log.warn("Manager {} attempted to access user {} (role: {}) who is not a USER in their team", 
        currentUserId, userId, user.getRole());
```

This allows for security auditing and detection of:
- Malicious attempts to access unauthorized data
- Frontend bugs sending wrong requests
- Token manipulation attempts

## Database Queries

The fix uses efficient database queries:

```sql
-- Before (vulnerable)
SELECT * FROM app_user WHERE manager_id = ?

-- After (secure)
SELECT * FROM app_user WHERE manager_id = ? AND role = 'USER'
```

This ensures filtering happens at the database level, not in application memory.

## Files Modified

1. `UserService.java` - All user retrieval and modification methods
2. Comments and documentation updated to reflect role restrictions

## Dependencies

Uses existing repository method:
- `UserRepository.findByManagerIdAndRole(UUID managerId, UserRole role, Pageable pageable)`

No new database queries or indices required.

## Backward Compatibility

This is a **BREAKING CHANGE** for managers who previously could see/edit ADMIN or MANAGER users.

**Migration Impact:**
- Managers who had ADMIN/MANAGER users assigned will no longer see them
- No data migration needed
- Recommended: Review and reassign any ADMIN/MANAGER users who incorrectly had a manager assigned

## Recommendations

1. **Database Audit**: Run query to find ADMIN/MANAGER users with manager_id set:
   ```sql
   SELECT id, email, role, manager_id 
   FROM benew.app_user 
   WHERE role IN ('ADMIN', 'MANAGER') 
   AND manager_id IS NOT NULL;
   ```

2. **Clean Up**: Consider setting `manager_id = NULL` for all ADMIN users

3. **Monitoring**: Monitor logs for WARN messages about unauthorized access attempts

4. **Testing**: Run comprehensive integration tests with different role scenarios

## Lessons Learned

1. Always filter by **both** relationship AND role when implementing RBAC
2. Test with realistic data scenarios (users of different roles with same manager)
3. Use security by obscurity (404) for unauthorized resource access
4. Log all security violations for audit trails
5. Implement defense in depth (URL + method + business logic)

## Security Checklist for Future Changes

When modifying user access control:
- [ ] Filter by user role in addition to relationships
- [ ] Test with users of all roles (ADMIN, MANAGER, USER)
- [ ] Verify logging of unauthorized access attempts
- [ ] Check that 404 is returned (not 403) for non-existent/unauthorized resources
- [ ] Ensure database queries include role filters
- [ ] Update documentation and tests
- [ ] Review audit logs after deployment
