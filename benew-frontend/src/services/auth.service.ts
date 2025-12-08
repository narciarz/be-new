import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginRequestDto, LoginResponseDto, User } from '../models';

/**
 * Authentication service handling user login, logout, and token management.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = '/auth'; // Uses proxy configuration
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';

  /**
   * Current authenticated user signal.
   */
  readonly currentUser = signal<User | null>(this.getUserFromStorage());

  /**
   * Check if user is authenticated.
   */
  readonly isAuthenticated = signal<boolean>(!!this.currentUser());

  /**
   * Authenticate user with email and password.
   */
  login(credentials: LoginRequestDto): Observable<LoginResponseDto> {
    return this.http.post<LoginResponseDto>(`${this.API_URL}/login`, credentials).pipe(
      tap((response) => {
        this.saveAuthData(response);
      })
    );
  }

  /**
   * Logout current user and clear authentication data.
   */
  logout(): void {
    this.clearAuthData();
  }

  /**
   * Get stored JWT token.
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Save authentication data to localStorage and update signals.
   */
  private saveAuthData(response: LoginResponseDto): void {
    const user: User = {
      userId: response.userId,
      email: response.email,
      role: response.role,
      firstName: response.firstName,
      lastName: response.lastName,
      token: response.token,
    };

    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));

    this.currentUser.set(user);
    this.isAuthenticated.set(true);
  }

  /**
   * Clear authentication data from localStorage and reset signals.
   */
  private clearAuthData(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);

    this.currentUser.set(null);
    this.isAuthenticated.set(false);
  }

  /**
   * Retrieve user data from localStorage.
   */
  private getUserFromStorage(): User | null {
    const userJson = localStorage.getItem(this.USER_KEY);
    if (!userJson) {
      return null;
    }

    try {
      return JSON.parse(userJson) as User;
    } catch {
      return null;
    }
  }
}
