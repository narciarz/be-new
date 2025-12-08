/**
 * DTO for user login request.
 * Used by POST /auth/login to authenticate user credentials.
 */
export interface LoginRequestDto {
  /**
   * User email address (used as username).
   */
  email: string;

  /**
   * User password (plain text, will be verified against BCrypt hash on backend).
   */
  password: string;
}

