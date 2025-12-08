import { UserRole } from './user-role';

/**
 * User entity representing authenticated user.
 */
export interface User {
  /**
   * User ID (UUID format).
   */
  userId: string;

  /**
   * User email.
   */
  email: string;

  /**
   * User role for access control.
   */
  role: UserRole;

  /**
   * User first name.
   */
  firstName: string;

  /**
   * User last name.
   */
  lastName: string;

  /**
   * JWT token for API authentication.
   */
  token: string;
}
