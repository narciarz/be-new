import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  UserDto,
  CreateUserRequestDto,
  UpdateUserRequestDto,
  PagedResponse,
} from '../models/user.dto';

/**
 * Service for managing users via REST API
 */
@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = '/api/users';

  /**
   * Get paginated list of users
   */
  getUsers(
    page = 0,
    size = 20,
    sort?: string,
    filter?: Record<string, string>
  ): Observable<PagedResponse<UserDto>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());

    if (sort) {
      params = params.set('sort', sort);
    }

    if (filter) {
      Object.entries(filter).forEach(([key, value]) => {
        params = params.set(key, value);
      });
    }

    return this.http.get<PagedResponse<UserDto>>(this.API_URL, { params });
  }

  /**
   * Get single user by ID
   */
  getUserById(userId: string): Observable<UserDto> {
    return this.http.get<UserDto>(`${this.API_URL}/${userId}`);
  }

  /**
   * Create a new user
   */
  createUser(user: CreateUserRequestDto): Observable<UserDto> {
    return this.http.post<UserDto>(this.API_URL, user);
  }

  /**
   * Update an existing user
   */
  updateUser(userId: string, user: UpdateUserRequestDto): Observable<UserDto> {
    return this.http.put<UserDto>(`${this.API_URL}/${userId}`, user);
  }

  /**
   * Delete a user
   */
  deleteUser(userId: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${userId}`);
  }
}

