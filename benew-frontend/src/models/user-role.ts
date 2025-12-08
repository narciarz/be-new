/**
 * User role enumeration for role-based access control.
 * Corresponds to backend UserRole enum.
 */
export enum UserRole {
  /**
   * Administrator with full system access.
   * Can manage users, templates, CSV imports, and all onboarding processes.
   */
  ADMIN = 'ADMIN',

  /**
   * Manager with limited access to their team's onboarding processes.
   * Can view dashboards and update tasks for users they oversee.
   */
  MANAGER = 'MANAGER',

  /**
   * Regular user (employee) with access only to their own onboarding checklist.
   * Can view and mark their assigned tasks as complete.
   */
  USER = 'USER',
}

