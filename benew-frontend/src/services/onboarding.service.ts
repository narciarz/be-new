import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  OnboardingProcessDto,
  CreateOnboardingProcessRequestDto,
  UpdateOnboardingProcessRequestDto,
  OnboardingTaskDto,
  UpdateOnboardingTaskRequestDto,
} from '../models/onboarding.dto';
import { PagedResponse } from '../models/user.dto';

/**
 * Service for managing onboarding processes and tasks via REST API
 */
@Injectable({
  providedIn: 'root',
})
export class OnboardingService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = '/api/onboarding';

  /**
   * Get paginated list of onboarding processes
   */
  getOnboardingProcesses(
    page = 0,
    size = 20,
    filter?: Record<string, string>
  ): Observable<PagedResponse<OnboardingProcessDto>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());

    if (filter) {
      Object.entries(filter).forEach(([key, value]) => {
        params = params.set(key, value);
      });
    }

    return this.http.get<PagedResponse<OnboardingProcessDto>>(this.API_URL, { params });
  }

  /**
   * Get single onboarding process by ID
   */
  getOnboardingProcessById(processId: string): Observable<OnboardingProcessDto> {
    return this.http.get<OnboardingProcessDto>(`${this.API_URL}/${processId}`);
  }

  /**
   * Create a new onboarding process
   */
  createOnboardingProcess(
    process: CreateOnboardingProcessRequestDto
  ): Observable<OnboardingProcessDto> {
    return this.http.post<OnboardingProcessDto>(this.API_URL, process);
  }

  /**
   * Update an onboarding process (e.g., change status to ARCHIVED)
   */
  updateOnboardingProcess(
    processId: string,
    process: UpdateOnboardingProcessRequestDto
  ): Observable<OnboardingProcessDto> {
    return this.http.put<OnboardingProcessDto>(`${this.API_URL}/${processId}`, process);
  }

  /**
   * Delete an onboarding process
   */
  deleteOnboardingProcess(processId: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${processId}`);
  }

  // ==================== Onboarding Tasks ====================

  /**
   * Get all tasks for an onboarding process
   */
  getOnboardingTasks(processId: string): Observable<OnboardingTaskDto[]> {
    return this.http.get<OnboardingTaskDto[]>(`${this.API_URL}/${processId}/tasks`);
  }

  /**
   * Get single onboarding task by ID
   */
  getOnboardingTaskById(processId: string, taskId: string): Observable<OnboardingTaskDto> {
    return this.http.get<OnboardingTaskDto>(`${this.API_URL}/${processId}/tasks/${taskId}`);
  }

  /**
   * Update an onboarding task (e.g., mark as completed)
   */
  updateOnboardingTask(
    processId: string,
    taskId: string,
    task: UpdateOnboardingTaskRequestDto
  ): Observable<OnboardingTaskDto> {
    return this.http.put<OnboardingTaskDto>(`${this.API_URL}/${processId}/tasks/${taskId}`, task);
  }
}

