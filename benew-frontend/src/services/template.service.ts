import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  TemplateDto,
  CreateTemplateRequestDto,
  UpdateTemplateRequestDto,
  TemplateTaskDto,
  CreateTemplateTaskRequestDto,
  UpdateTemplateTaskRequestDto,
} from '../models/template.dto';
import { PagedResponse } from '../models/user.dto';

/**
 * Service for managing templates and template tasks via REST API
 */
@Injectable({
  providedIn: 'root',
})
export class TemplateService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = '/api/templates';

  /**
   * Get paginated list of templates
   */
  getTemplates(
    page = 0,
    size = 20,
    sort?: string
  ): Observable<PagedResponse<TemplateDto>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());

    if (sort) {
      params = params.set('sort', sort);
    }

    return this.http.get<PagedResponse<TemplateDto>>(this.API_URL, { params });
  }

  /**
   * Get single template by ID
   */
  getTemplateById(templateId: string): Observable<TemplateDto> {
    return this.http.get<TemplateDto>(`${this.API_URL}/${templateId}`);
  }

  /**
   * Create a new template
   */
  createTemplate(template: CreateTemplateRequestDto): Observable<TemplateDto> {
    return this.http.post<TemplateDto>(this.API_URL, template);
  }

  /**
   * Update an existing template
   */
  updateTemplate(templateId: string, template: UpdateTemplateRequestDto): Observable<TemplateDto> {
    return this.http.put<TemplateDto>(`${this.API_URL}/${templateId}`, template);
  }

  /**
   * Delete a template
   */
  deleteTemplate(templateId: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${templateId}`);
  }

  // ==================== Template Tasks ====================

  /**
   * Get all tasks for a template
   */
  getTemplateTasks(templateId: string): Observable<TemplateTaskDto[]> {
    return this.http.get<TemplateTaskDto[]>(`${this.API_URL}/${templateId}/tasks`);
  }

  /**
   * Create a new task for a template
   */
  createTemplateTask(
    templateId: string,
    task: CreateTemplateTaskRequestDto
  ): Observable<TemplateTaskDto> {
    return this.http.post<TemplateTaskDto>(`${this.API_URL}/${templateId}/tasks`, task);
  }

  /**
   * Update a template task
   */
  updateTemplateTask(
    templateId: string,
    taskId: string,
    task: UpdateTemplateTaskRequestDto
  ): Observable<TemplateTaskDto> {
    return this.http.put<TemplateTaskDto>(`${this.API_URL}/${templateId}/tasks/${taskId}`, task);
  }

  /**
   * Delete a template task
   */
  deleteTemplateTask(templateId: string, taskId: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${templateId}/tasks/${taskId}`);
  }

  /**
   * Import template from CSV
   */
  importTemplateFromCsv(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.API_URL}/import`, formData);
  }
}
