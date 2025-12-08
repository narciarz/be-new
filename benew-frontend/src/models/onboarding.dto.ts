/**
 * Onboarding Process entity from backend
 */
export interface OnboardingProcessDto {
  id: string; // Backend returns "id" not "processId"
  userId: string;
  managerId: string;
  sourceTemplateId: string;
  status: 'ACTIVE' | 'COMPLETED' | 'ARCHIVED';
  totalTasksCount: number;
  completedTasksCount: number;
  createdAt?: string;
  updatedAt?: string;
  // Additional populated fields for UI
  userName?: string;
  userPosition?: string;
  managerName?: string;
}

/**
 * Request DTO for creating a new onboarding process
 */
export interface CreateOnboardingProcessRequestDto {
  userId: string;
  managerId: string;
  sourceTemplateId: string;
}

/**
 * Request DTO for updating an existing onboarding process
 */
export interface UpdateOnboardingProcessRequestDto {
  status?: 'ACTIVE' | 'COMPLETED' | 'ARCHIVED';
  totalTasksCount?: number;
  completedTasksCount?: number;
}

/**
 * Onboarding Task entity from backend
 */
export interface OnboardingTaskDto {
  id: string; // Backend returns "id" not "taskId"
  processId: string;
  title: string;
  description: string;
  taskOrder: number;
  ownerRole: string;
  isCompleted: boolean;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Request DTO for updating an onboarding task
 */
export interface UpdateOnboardingTaskRequestDto {
  isCompleted: boolean;
}
