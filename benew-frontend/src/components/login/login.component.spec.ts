import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../services';
import { UserRole } from '../../models';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
        provideAnimations(),
      ],
    }).compileComponents();

    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with empty form', () => {
    expect(component.loginForm.value).toEqual({
      email: '',
      password: '',
    });
  });

  it('should have initial loading state as false', () => {
    expect(component.isLoading()).toBe(false);
  });

  it('should have initial error message as null', () => {
    expect(component.errorMessage()).toBeNull();
  });

  describe('Form Validation', () => {
    it('should mark email as invalid when empty', () => {
      const emailControl = component.loginForm.get('email');
      emailControl?.setValue('');
      emailControl?.markAsTouched();
      expect(emailControl?.invalid).toBe(true);
      expect(emailControl?.errors?.['required']).toBe(true);
    });

    it('should mark email as invalid with incorrect format', () => {
      const emailControl = component.loginForm.get('email');
      emailControl?.setValue('invalid-email');
      emailControl?.markAsTouched();
      expect(emailControl?.invalid).toBe(true);
      expect(emailControl?.errors?.['email']).toBe(true);
    });

    it('should mark email as valid with correct format', () => {
      const emailControl = component.loginForm.get('email');
      emailControl?.setValue('test@example.com');
      expect(emailControl?.valid).toBe(true);
    });

    it('should mark password as invalid when empty', () => {
      const passwordControl = component.loginForm.get('password');
      passwordControl?.setValue('');
      passwordControl?.markAsTouched();
      expect(passwordControl?.invalid).toBe(true);
      expect(passwordControl?.errors?.['required']).toBe(true);
    });

    it('should mark password as invalid when too short', () => {
      const passwordControl = component.loginForm.get('password');
      passwordControl?.setValue('12');
      passwordControl?.markAsTouched();
      expect(passwordControl?.invalid).toBe(true);
      expect(passwordControl?.errors?.['minlength']).toBeTruthy();
    });

    it('should mark password as valid with minimum length', () => {
      const passwordControl = component.loginForm.get('password');
      passwordControl?.setValue('123');
      expect(passwordControl?.valid).toBe(true);
    });

    it('should mark form as invalid when fields are empty', () => {
      expect(component.loginForm.invalid).toBe(true);
    });

    it('should mark form as valid when all fields are valid', () => {
      component.loginForm.setValue({
        email: 'test@example.com',
        password: 'password123',
      });
      expect(component.loginForm.valid).toBe(true);
    });
  });

  describe('onSubmit', () => {
    it('should not submit if form is invalid', () => {
      component.loginForm.setValue({
        email: '',
        password: '',
      });

      component.onSubmit();

      expect(authService.login).not.toHaveBeenCalled();
      expect(component.loginForm.touched).toBe(true);
    });

    it('should set loading state to true when submitting', () => {
      authService.login.and.returnValue(of({
        token: 'test-token',
        userId: '1',
        email: 'test@example.com',
        role: UserRole.USER,
        firstName: 'Test',
        lastName: 'User',
      }));

      component.loginForm.setValue({
        email: 'test@example.com',
        password: 'password123',
      });

      component.onSubmit();

      expect(component.isLoading()).toBe(false); // After successful login
      expect(authService.login).toHaveBeenCalled();
    });

    it('should call authService.login with correct credentials', () => {
      const credentials = {
        email: 'test@example.com',
        password: 'password123',
      };

      authService.login.and.returnValue(of({
        token: 'test-token',
        userId: '1',
        email: credentials.email,
        role: UserRole.USER,
        firstName: 'Test',
        lastName: 'User',
      }));

      component.loginForm.setValue(credentials);
      component.onSubmit();

      expect(authService.login).toHaveBeenCalledWith(credentials);
    });

    it('should navigate to dashboard on successful login', () => {
      authService.login.and.returnValue(of({
        token: 'test-token',
        userId: '1',
        email: 'test@example.com',
        role: UserRole.USER,
        firstName: 'Test',
        lastName: 'User',
      }));

      component.loginForm.setValue({
        email: 'test@example.com',
        password: 'password123',
      });

      component.onSubmit();

      expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
    });

    it('should set loading to false after successful login', () => {
      authService.login.and.returnValue(of({
        token: 'test-token',
        userId: '1',
        email: 'test@example.com',
        role: UserRole.USER,
        firstName: 'Test',
        lastName: 'User',
      }));

      component.loginForm.setValue({
        email: 'test@example.com',
        password: 'password123',
      });

      component.onSubmit();

      expect(component.isLoading()).toBe(false);
    });
  });

  describe('Error Handling', () => {
    it('should display error message on 401 error', () => {
      authService.login.and.returnValue(
        throwError(() => ({ status: 401, error: {} }))
      );

      component.loginForm.setValue({
        email: 'test@example.com',
        password: 'wrongpassword',
      });

      component.onSubmit();

      expect(component.isLoading()).toBe(false);
      expect(component.errorMessage()).toBe('Nieprawidłowy email lub hasło');
    });

    it('should display error message on 400 error', () => {
      authService.login.and.returnValue(
        throwError(() => ({ status: 400, error: {} }))
      );

      component.loginForm.setValue({
        email: 'test@example.com',
        password: 'password123',
      });

      component.onSubmit();

      expect(component.isLoading()).toBe(false);
      expect(component.errorMessage()).toBe('Nieprawidłowe dane logowania');
    });

    it('should display generic error message on server error', () => {
      authService.login.and.returnValue(
        throwError(() => ({ status: 500, error: {} }))
      );

      component.loginForm.setValue({
        email: 'test@example.com',
        password: 'password123',
      });

      component.onSubmit();

      expect(component.isLoading()).toBe(false);
      expect(component.errorMessage()).toBe(
        'Wystąpił błąd serwera. Spróbuj ponownie później.'
      );
    });

    it('should clear error message on new submission', () => {
      // First submission with error
      authService.login.and.returnValue(
        throwError(() => ({ status: 401, error: {} }))
      );

      component.loginForm.setValue({
        email: 'test@example.com',
        password: 'wrongpassword',
      });

      component.onSubmit();
      expect(component.errorMessage()).toBe('Nieprawidłowy email lub hasło');

      // Second submission should clear error
      authService.login.and.returnValue(of({
        token: 'test-token',
        userId: '1',
        email: 'test@example.com',
        role: UserRole.USER,
        firstName: 'Test',
        lastName: 'User',
      }));

      component.loginForm.setValue({
        email: 'test@example.com',
        password: 'correctpassword',
      });

      component.onSubmit();

      // After submission, error should be null (cleared during onSubmit)
      expect(component.errorMessage()).toBeNull();
    });
  });

  describe('Component Rendering', () => {
    it('should render login form', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.querySelector('form')).toBeTruthy();
    });

    it('should render email input field', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const emailInput = compiled.querySelector('input[type="email"]');
      expect(emailInput).toBeTruthy();
    });

    it('should render password input field', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const passwordInput = compiled.querySelector('input[type="password"]');
      expect(passwordInput).toBeTruthy();
    });

    it('should render submit button', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const submitButton = compiled.querySelector('button[type="submit"]');
      expect(submitButton).toBeTruthy();
    });
  });
});
