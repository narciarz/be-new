import { UserRole } from './user-role';

/**
 * DTO for successful login response.
 * Returned by POST /auth/login on successful authentication.
 */
export interface LoginResponseDto {
  /**
   * JWT authentication token for subsequent API calls.
   * Include this in Authorization header as Bearer token.
   */
  token: string;

  /**
   * User ID (UUID format).
   */
  userId: string;

  /**
   * User email.
   */
  email: string;

  /**
   * User role (ADMIN, MANAGER, USER) for client-side access control.
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
}
