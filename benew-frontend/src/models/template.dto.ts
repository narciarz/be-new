/**
 * Template entity from backend
 */
export interface TemplateDto {
  templateId: string;
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
  taskId: string;
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
