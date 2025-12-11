import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';
import { UserDialogComponent } from './user-dialog.component';
import { UserService } from '../../../services/user.service';
import { AuthService } from '../../../services/auth.service';
import { UserDto } from '../../../models/user.dto';
import { UserRole } from '../../../models/user-role';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('UserDialogComponent', () => {
  const mockAdminUser = {
    id: 'admin-1',
    email: 'admin@example.com',
    role: UserRole.ADMIN,
    firstName: 'Admin',
    lastName: 'User',
    token: 'test-token',
  };

  const mockManagerUser = {
    id: 'manager-1',
    email: 'manager@example.com',
    role: UserRole.MANAGER,
    firstName: 'Manager',
    lastName: 'User',
    token: 'test-token',
  };

  const mockUser: UserDto = {
    id: 'user-1',
    email: 'user@example.com',
    firstName: 'Test',
    lastName: 'User',
    positionName: 'developer',
    role: UserRole.USER,
    managerId: 'manager-1',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z',
  };

  const mockManagers: UserDto[] = [
    {
      id: 'manager-1',
      email: 'manager1@example.com',
      firstName: 'Manager',
      lastName: 'One',
      positionName: 'manager',
      role: UserRole.MANAGER,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-02T00:00:00Z',
    },
  ];

  describe('Create Mode - Admin User', () => {
    let component: UserDialogComponent;
    let fixture: ComponentFixture<UserDialogComponent>;
    let userService: jasmine.SpyObj<UserService>;
    let dialogRef: jasmine.SpyObj<MatDialogRef<UserDialogComponent>>;

    beforeEach(async () => {
      const userServiceSpy = jasmine.createSpyObj('UserService', [
        'createUser',
        'updateUser',
        'getUsers',
      ]);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockAdminUser),
      });
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      userServiceSpy.getUsers.and.returnValue(
        of({
          content: mockManagers,
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [UserDialogComponent, ReactiveFormsModule],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: { mode: 'create' } },
          provideAnimations(),
        ],
      }).compileComponents();

      userService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
      dialogRef = TestBed.inject(MatDialogRef) as jasmine.SpyObj<MatDialogRef<UserDialogComponent>>;

      fixture = TestBed.createComponent(UserDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create in create mode', () => {
      expect(component).toBeTruthy();
      expect(component.isEditMode).toBe(false);
    });

    it('should load managers on init for admin', () => {
      expect(userService.getUsers).toHaveBeenCalled();
      expect(component.allUsers().length).toBe(1);
    });

    it('should load positions on init', () => {
      expect(userService.getUsers).toHaveBeenCalled();
    });

    it('should have password as required in create mode', () => {
      const passwordControl = component.userForm.get('password');
      expect(passwordControl?.hasError('required')).toBe(true);
    });

    it('should have all roles available for admin', () => {
      expect(component.roles().length).toBe(3);
      expect(component.roles()).toContain({ value: UserRole.ADMIN, label: 'Administrator' });
      expect(component.roles()).toContain({ value: UserRole.MANAGER, label: 'Menedżer' });
      expect(component.roles()).toContain({ value: UserRole.USER, label: 'Użytkownik' });
    });

    it('should call createUser on save', () => {
      userService.createUser.and.returnValue(of(mockUser));

      component.userForm.patchValue({
        email: 'new@example.com',
        password: 'password123',
        firstName: 'New',
        lastName: 'User',
        positionName: 'developer',
        role: UserRole.USER,
        managerId: 'manager-1',
      });

      component.onSave();

      expect(userService.createUser).toHaveBeenCalledWith({
        email: 'new@example.com',
        password: 'password123',
        firstName: 'New',
        lastName: 'User',
        positionName: 'developer',
        role: UserRole.USER,
        managerId: 'manager-1',
      });
    });

    it('should close dialog with created user', () => {
      userService.createUser.and.returnValue(of(mockUser));

      component.userForm.patchValue({
        email: 'new@example.com',
        password: 'password123',
        firstName: 'New',
        lastName: 'User',
        positionName: 'developer',
        role: UserRole.USER,
      });

      component.onSave();

      expect(dialogRef.close).toHaveBeenCalledWith(mockUser);
    });

    it('should handle error when creating user', () => {
      userService.createUser.and.returnValue(
        throwError(() => ({ error: { message: 'Error creating' } }))
      );

      component.userForm.patchValue({
        email: 'new@example.com',
        password: 'password123',
        firstName: 'New',
        lastName: 'User',
        positionName: 'developer',
        role: UserRole.USER,
      });

      component.onSave();

      expect(component.errorMessage()).toBe('Error creating');
      expect(component.isSaving()).toBe(false);
    });
  });

  describe('Create Mode - Manager User', () => {
    let component: UserDialogComponent;
    let fixture: ComponentFixture<UserDialogComponent>;
    let userService: jasmine.SpyObj<UserService>;
    let dialogRef: jasmine.SpyObj<MatDialogRef<UserDialogComponent>>;

    beforeEach(async () => {
      const userServiceSpy = jasmine.createSpyObj('UserService', [
        'createUser',
        'updateUser',
        'getUsers',
      ]);
      const managerAuthService = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManagerUser),
      });
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      userServiceSpy.getUsers.and.returnValue(
        of({
          content: mockManagers,
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [UserDialogComponent, ReactiveFormsModule],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: managerAuthService },
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: { mode: 'create' } },
          provideAnimations(),
        ],
      }).compileComponents();

      userService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
      dialogRef = TestBed.inject(MatDialogRef) as jasmine.SpyObj<MatDialogRef<UserDialogComponent>>;

      fixture = TestBed.createComponent(UserDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should only have USER role available for manager', () => {
      expect(component.roles().length).toBe(1);
      expect(component.roles()).toEqual([{ value: UserRole.USER, label: 'Użytkownik' }]);
    });

    it('should force USER role when manager creates user', () => {
      userService.createUser.and.returnValue(of(mockUser));

      component.userForm.patchValue({
        email: 'new@example.com',
        password: 'password123',
        firstName: 'New',
        lastName: 'User',
        positionName: 'developer',
        role: UserRole.ADMIN, // Manager tries to set ADMIN role
      });

      component.onSave();

      expect(userService.createUser).toHaveBeenCalledWith(
        jasmine.objectContaining({
          role: UserRole.USER, // Should be forced to USER
          managerId: undefined, // Manager shouldn't specify managerId
        })
      );
    });

    it('should not load managers list for manager user', () => {
      // getUsers should be called only once for positions, not for managers
      // The call count should be 1 (already made in ngOnInit)
      expect(userService.getUsers).toHaveBeenCalledTimes(1);
    });
  });

  describe('Edit Mode', () => {
    let component: UserDialogComponent;
    let fixture: ComponentFixture<UserDialogComponent>;
    let userService: jasmine.SpyObj<UserService>;
    let dialogRef: jasmine.SpyObj<MatDialogRef<UserDialogComponent>>;

    beforeEach(async () => {
      const userServiceSpy = jasmine.createSpyObj('UserService', [
        'createUser',
        'updateUser',
        'getUsers',
      ]);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockAdminUser),
      });
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      userServiceSpy.getUsers.and.returnValue(
        of({
          content: mockManagers,
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [UserDialogComponent, ReactiveFormsModule],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: { mode: 'edit', user: mockUser } },
          provideAnimations(),
        ],
      }).compileComponents();

      userService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
      dialogRef = TestBed.inject(MatDialogRef) as jasmine.SpyObj<MatDialogRef<UserDialogComponent>>;

      fixture = TestBed.createComponent(UserDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create in edit mode', () => {
      expect(component.isEditMode).toBe(true);
    });

    it('should initialize form with user data', () => {
      expect(component.userForm.value.email).toBe(mockUser.email);
      expect(component.userForm.value.firstName).toBe(mockUser.firstName);
      expect(component.userForm.value.lastName).toBe(mockUser.lastName);
      expect(component.userForm.value.positionName).toBe(mockUser.positionName);
      expect(component.userForm.value.role).toBe(mockUser.role);
    });

    it('should not require password in edit mode', () => {
      const passwordControl = component.userForm.get('password');
      expect(passwordControl?.hasError('required')).toBe(false);
    });

    it('should call updateUser on save', () => {
      userService.updateUser.and.returnValue(of(mockUser));

      component.userForm.patchValue({
        firstName: 'Updated',
      });

      component.onSave();

      expect(userService.updateUser).toHaveBeenCalledWith(
        mockUser.id,
        jasmine.objectContaining({
          firstName: 'Updated',
        })
      );
    });

    it('should include password in update if changed', () => {
      userService.updateUser.and.returnValue(of(mockUser));

      component.userForm.patchValue({
        password: 'newpassword',
      });

      component.onSave();

      expect(userService.updateUser).toHaveBeenCalledWith(
        mockUser.id,
        jasmine.objectContaining({
          password: 'newpassword',
        })
      );
    });

    it('should not include password in update if not changed', () => {
      userService.updateUser.and.returnValue(of(mockUser));

      component.userForm.patchValue({
        firstName: 'Updated',
        password: '', // Empty password
      });

      component.onSave();

      const updateData = (userService.updateUser.calls.mostRecent().args[1] as any);
      expect(updateData.password).toBeUndefined();
    });

    it('should close dialog with updated user', () => {
      const updatedUser = { ...mockUser, firstName: 'Updated' };
      userService.updateUser.and.returnValue(of(updatedUser));

      component.userForm.patchValue({
        firstName: 'Updated',
      });

      component.onSave();

      expect(dialogRef.close).toHaveBeenCalledWith(updatedUser);
    });

    it('should handle error when updating user', () => {
      userService.updateUser.and.returnValue(
        throwError(() => ({ error: { message: 'Error updating' } }))
      );

      component.userForm.patchValue({
        firstName: 'Updated',
      });

      component.onSave();

      expect(component.errorMessage()).toBe('Error updating');
      expect(component.isSaving()).toBe(false);
    });
  });

  describe('Form Validation', () => {
    let component: UserDialogComponent;
    let fixture: ComponentFixture<UserDialogComponent>;
    let userService: jasmine.SpyObj<UserService>;

    beforeEach(async () => {
      const userServiceSpy = jasmine.createSpyObj('UserService', [
        'createUser',
        'updateUser',
        'getUsers',
      ]);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockAdminUser),
      });
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      userServiceSpy.getUsers.and.returnValue(
        of({
          content: mockManagers,
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [UserDialogComponent, ReactiveFormsModule],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: { mode: 'create' } },
          provideAnimations(),
        ],
      }).compileComponents();

      userService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;

      fixture = TestBed.createComponent(UserDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should validate email format', () => {
      const emailControl = component.userForm.get('email');
      emailControl?.setValue('invalid-email');
      emailControl?.markAsTouched();
      expect(emailControl?.hasError('email')).toBe(true);
    });

    it('should validate password minimum length in create mode', () => {
      const passwordControl = component.userForm.get('password');
      passwordControl?.setValue('short');
      passwordControl?.markAsTouched();
      expect(passwordControl?.hasError('minlength')).toBe(true);
    });

    it('should require all fields except managerId and password (in edit mode)', () => {
      component.userForm.patchValue({
        email: '',
        firstName: '',
        lastName: '',
        positionName: '',
      });

      expect(component.userForm.get('email')?.hasError('required')).toBe(true);
      expect(component.userForm.get('firstName')?.hasError('required')).toBe(true);
      expect(component.userForm.get('lastName')?.hasError('required')).toBe(true);
      expect(component.userForm.get('positionName')?.hasError('required')).toBe(true);
    });

    it('should not submit invalid form', () => {
      component.userForm.patchValue({ email: '' });
      component.onSave();

      expect(userService.createUser).not.toHaveBeenCalled();
      expect(component.userForm.touched).toBe(true);
    });

    it('should return appropriate error messages', () => {
      const emailControl = component.userForm.get('email');
      emailControl?.setValue('');
      emailControl?.markAsTouched();
      expect(component.getErrorMessage('email')).toBe('To pole jest wymagane');

      emailControl?.setValue('invalid');
      expect(component.getErrorMessage('email')).toBe('Nieprawidłowy adres email');

      const passwordControl = component.userForm.get('password');
      passwordControl?.setValue('short');
      passwordControl?.markAsTouched();
      expect(component.getErrorMessage('password')).toContain('Minimum');
    });

    it('should normalize positionName to lowercase', () => {
      userService.createUser.and.returnValue(of(mockUser));

      component.userForm.patchValue({
        email: 'new@example.com',
        password: 'password123',
        firstName: 'New',
        lastName: 'User',
        positionName: 'Senior Developer', // Mixed case with spaces
        role: UserRole.USER,
      });

      component.onSave();

      expect(userService.createUser).toHaveBeenCalledWith(
        jasmine.objectContaining({
          positionName: 'senior developer', // Should be normalized
        })
      );
    });
  });

  describe('onCancel', () => {
    let component: UserDialogComponent;
    let fixture: ComponentFixture<UserDialogComponent>;
    let dialogRef: jasmine.SpyObj<MatDialogRef<UserDialogComponent>>;

    beforeEach(async () => {
      const userServiceSpy = jasmine.createSpyObj('UserService', [
        'createUser',
        'updateUser',
        'getUsers',
      ]);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockAdminUser),
      });
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      userServiceSpy.getUsers.and.returnValue(
        of({
          content: mockManagers,
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [UserDialogComponent, ReactiveFormsModule],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: { mode: 'create' } },
          provideAnimations(),
        ],
      }).compileComponents();

      dialogRef = TestBed.inject(MatDialogRef) as jasmine.SpyObj<MatDialogRef<UserDialogComponent>>;

      fixture = TestBed.createComponent(UserDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should close dialog without data', () => {
      component.onCancel();
      expect(dialogRef.close).toHaveBeenCalledWith();
    });
  });

  describe('Component Rendering', () => {
    let component: UserDialogComponent;
    let fixture: ComponentFixture<UserDialogComponent>;

    beforeEach(async () => {
      const userServiceSpy = jasmine.createSpyObj('UserService', [
        'createUser',
        'updateUser',
        'getUsers',
      ]);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockAdminUser),
      });
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      userServiceSpy.getUsers.and.returnValue(
        of({
          content: mockManagers,
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [UserDialogComponent, ReactiveFormsModule],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: { mode: 'create' } },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(UserDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should render form', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const form = compiled.querySelector('form');
      expect(form).toBeTruthy();
    });

    it('should render all form fields', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.querySelector('input[formControlName="email"]')).toBeTruthy();
      expect(compiled.querySelector('input[formControlName="password"]')).toBeTruthy();
      expect(compiled.querySelector('input[formControlName="firstName"]')).toBeTruthy();
      expect(compiled.querySelector('input[formControlName="lastName"]')).toBeTruthy();
      expect(compiled.querySelector('input[formControlName="positionName"]')).toBeTruthy();
    });
  });
});
