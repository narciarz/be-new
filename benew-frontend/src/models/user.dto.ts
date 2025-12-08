import { UserRole } from './user-role';

/**
 * User entity from backend
 */
export interface UserDto {
  id: string; // Backend returns "id" not "userId"
  email: string;
  firstName: string;
  lastName: string;
  positionName: string;
  managerId?: string;
  role: UserRole;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Request DTO for creating a new user
 */
export interface CreateUserRequestDto {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  positionName: string;
  managerId?: string;
  role: UserRole;
}

/**
 * Request DTO for updating an existing user
 */
export interface UpdateUserRequestDto {
  email?: string;
  password?: string;
  firstName?: string;
  lastName?: string;
  positionName?: string;
  managerId?: string;
  role?: UserRole;
}

/**
 * Paginated response wrapper
 */
export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

