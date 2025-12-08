import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

/**
 * HTTP interceptor that adds JWT Bearer token to outgoing requests.
 * Excludes authentication endpoints from token injection.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  // Skip adding token for login endpoint
  if (req.url.includes('/auth/login')) {
    return next(req);
  }

  // Get token from AuthService
  const token = authService.getToken();

  // If token exists, clone request and add Authorization header
  if (token) {
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
    return next(clonedRequest);
  }

  // If no token, proceed with original request
  return next(req);
};
