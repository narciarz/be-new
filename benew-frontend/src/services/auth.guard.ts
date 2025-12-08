import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Auth guard protecting routes that require authentication.
 * Redirects unauthenticated users to login page.
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // Redirect to login page if not authenticated
  return router.createUrlTree(['/login']);
};

/**
 * Guest guard preventing authenticated users from accessing public pages.
 * Redirects authenticated users to dashboard.
 */
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return true;
  }

  // Redirect to dashboard if already authenticated
  return router.createUrlTree(['/dashboard']);
};

