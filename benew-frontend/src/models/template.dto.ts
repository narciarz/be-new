/**
 * Template entity from backend
 */
export interface TemplateDto {
  id: string; // Backend returns "id" not "templateId"
  positionName: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Request DTO for creating a new template
 */
export interface CreateTemplateRequestDto {
  positionName: string;
  description?: string;
}

/**
 * Request DTO for updating an existing template
 */
export interface UpdateTemplateRequestDto {
  positionName?: string;
  description?: string;
}

/**
 * Template Task entity from backend
 */
export interface TemplateTaskDto {
  id: string; // Backend returns "id" not "taskId"
  templateId: string;
  title: string;
  description: string;
  taskOrder: number;
  ownerRole: string;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Request DTO for creating a new template task
 */
export interface CreateTemplateTaskRequestDto {
  title: string;
  description: string;
  taskOrder: number;
  ownerRole: string;
}

/**
 * Request DTO for updating an existing template task
 */
export interface UpdateTemplateTaskRequestDto {
  title?: string;
  description?: string;
  taskOrder?: number;
  ownerRole?: string;
}
