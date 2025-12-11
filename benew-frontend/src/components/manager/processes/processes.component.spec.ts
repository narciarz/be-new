import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';
import { ProcessesComponent } from './processes.component';
import { OnboardingService } from '../../../services/onboarding.service';
import { UserService } from '../../../services/user.service';
import { AuthService } from '../../../services/auth.service';
import { UserRole } from '../../../models';
import { OnboardingProcessDto } from '../../../models/onboarding.dto';
import { UserDto } from '../../../models/user.dto';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('ProcessesComponent', () => {
  const mockManager = {
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
    firstName: 'John',
    lastName: 'Doe',
    positionName: 'developer',
    role: UserRole.USER,
    managerId: 'manager-1',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z',
  };

  const mockActiveProcesses: OnboardingProcessDto[] = [
    {
      id: 'process-1',
      userId: 'user-1',
      managerId: 'manager-1',
      sourceTemplateId: 'template-1',
      status: 'ACTIVE',
      totalTasksCount: 10,
      completedTasksCount: 5,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-02T00:00:00Z',
    },
  ];

  const mockCompletedProcesses: OnboardingProcessDto[] = [
    {
      id: 'process-2',
      userId: 'user-1',
      managerId: 'manager-1',
      sourceTemplateId: 'template-1',
      status: 'ARCHIVED',
      totalTasksCount: 10,
      completedTasksCount: 10,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-10T00:00:00Z',
    },
  ];

  describe('Basic Functionality', () => {
    let component: ProcessesComponent;
    let fixture: ComponentFixture<ProcessesComponent>;
    let onboardingService: jasmine.SpyObj<OnboardingService>;
    let userService: jasmine.SpyObj<UserService>;
    let router: jasmine.SpyObj<Router>;

    beforeEach(async () => {
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
        'updateOnboardingProcess',
      ]);
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUserById']);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });
      const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      userServiceSpy.getUserById.and.returnValue(of(mockUser));

      onboardingServiceSpy.getOnboardingProcesses.and.returnValues(
        of({
          content: mockActiveProcesses,
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        }),
        of({
          content: mockCompletedProcesses,
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [ProcessesComponent],
        providers: [
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: Router, useValue: routerSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      onboardingService = TestBed.inject(OnboardingService) as jasmine.SpyObj<OnboardingService>;
      userService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
      router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

      fixture = TestBed.createComponent(ProcessesComponent);
      component = fixture.componentInstance;
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize with loading state', () => {
      expect(component.isLoading()).toBe(true);
    });

    it('should load active and completed processes on init', () => {
      fixture.detectChanges();

      expect(onboardingService.getOnboardingProcesses).toHaveBeenCalledWith(0, 100, {
        managerId: mockManager.id,
        status: 'ACTIVE',
      });
      expect(onboardingService.getOnboardingProcesses).toHaveBeenCalledWith(0, 100, {
        managerId: mockManager.id,
        status: 'ARCHIVED',
      });
    });

    it('should load user details for each process', () => {
      fixture.detectChanges();

      expect(userService.getUserById).toHaveBeenCalledWith('user-1');
    });

    it('should set active processes after load', () => {
      fixture.detectChanges();

      expect(component.activeProcesses().length).toBe(1);
      expect(component.activeProcesses()[0].userName).toBe('John Doe');
      expect(component.activeProcesses()[0].progress).toBe(50); // 5 out of 10
    });

    it('should set completed processes after load', () => {
      fixture.detectChanges();

      expect(component.completedProcesses().length).toBe(1);
      expect(component.completedProcesses()[0].userName).toBe('John Doe');
      expect(component.completedProcesses()[0].progress).toBe(100);
    });

    it('should set loading to false after successful load', () => {
      fixture.detectChanges();

      expect(component.isLoading()).toBe(false);
      expect(component.errorMessage()).toBeNull();
    });

    it('should format start date correctly', () => {
      fixture.detectChanges();
      const process = component.activeProcesses()[0];
      expect(process.startDate).toBe('2024-01-01');
    });

    it('should calculate progress correctly', () => {
      fixture.detectChanges();
      const process = component.activeProcesses()[0];
      expect(process.progress).toBe(50);
    });

    it('should include position from user data', () => {
      fixture.detectChanges();
      const process = component.activeProcesses()[0];
      expect(process.position).toBe('developer');
    });

    it('should show completed date for archived processes', () => {
      fixture.detectChanges();
      const process = component.completedProcesses()[0];
      expect(process.completedDate).toBe('2024-01-10');
    });

    it('should calculate duration for completed processes', () => {
      fixture.detectChanges();
      const process = component.completedProcesses()[0];
      expect(process.duration).toBeDefined();
      expect(process.duration).toContain('dni');
    });

    it('should navigate to process detail page', () => {
      fixture.detectChanges();
      component.onViewProcess('process-1');

      expect(router.navigate).toHaveBeenCalledWith([
        '/dashboard/manager/processes',
        'process-1',
      ]);
    });

    it('should render mat-card', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const card = compiled.querySelector('mat-card');
      expect(card).toBeTruthy();
    });

    it('should render mat-tab-group', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const tabGroup = compiled.querySelector('mat-tab-group');
      expect(tabGroup).toBeTruthy();
    });

  });

  describe('Error Handling - Loading Processes', () => {
    let component: ProcessesComponent;
    let fixture: ComponentFixture<ProcessesComponent>;

    beforeEach(async () => {
      spyOn(console, 'error'); // Suppress expected error logs

      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
        'updateOnboardingProcess',
      ]);
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUserById']);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });
      const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      onboardingServiceSpy.getOnboardingProcesses.and.returnValue(
        throwError(() => new Error('Failed to load'))
      );

      await TestBed.configureTestingModule({
        imports: [ProcessesComponent],
        providers: [
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: Router, useValue: routerSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(ProcessesComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should handle error when loading processes', () => {
      expect(component.errorMessage()).toBe('Błąd podczas ładowania procesów');
      expect(component.isLoading()).toBe(false);
    });
  });

  describe('Error Handling - Loading User', () => {
    let component: ProcessesComponent;
    let fixture: ComponentFixture<ProcessesComponent>;

    beforeEach(async () => {
      spyOn(console, 'error'); // Suppress expected error logs

      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
        'updateOnboardingProcess',
      ]);
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUserById']);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });
      const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      onboardingServiceSpy.getOnboardingProcesses.and.returnValues(
        of({
          content: mockActiveProcesses,
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        }),
        of({
          content: mockCompletedProcesses,
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      userServiceSpy.getUserById.and.returnValue(
        throwError(() => new Error('Failed to load user'))
      );

      await TestBed.configureTestingModule({
        imports: [ProcessesComponent],
        providers: [
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: Router, useValue: routerSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(ProcessesComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should handle error when loading user details', () => {
      // Component should still finish loading even if user details fail
      expect(component.isLoading()).toBe(false);
    });
  });

  describe('Null User Handling', () => {
    let component: ProcessesComponent;
    let fixture: ComponentFixture<ProcessesComponent>;

    beforeEach(async () => {
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
        'updateOnboardingProcess',
      ]);
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUserById']);
      const nullAuthService = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(null),
      });
      const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      await TestBed.configureTestingModule({
        imports: [ProcessesComponent],
        providers: [
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: nullAuthService },
          { provide: Router, useValue: routerSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(ProcessesComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should handle null currentUser', () => {
      expect(component.errorMessage()).toBe('Brak danych użytkownika');
      expect(component.isLoading()).toBe(false);
    });
  });

  describe('Archive Process', () => {
    let component: ProcessesComponent;
    let fixture: ComponentFixture<ProcessesComponent>;
    let onboardingService: jasmine.SpyObj<OnboardingService>;

    beforeEach(async () => {
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
        'updateOnboardingProcess',
      ]);
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUserById']);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });
      const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      userServiceSpy.getUserById.and.returnValue(of(mockUser));

      onboardingServiceSpy.getOnboardingProcesses.and.returnValues(
        of({
          content: mockActiveProcesses,
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        }),
        of({
          content: mockCompletedProcesses,
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [ProcessesComponent],
        providers: [
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: Router, useValue: routerSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      onboardingService = TestBed.inject(OnboardingService) as jasmine.SpyObj<OnboardingService>;
      fixture = TestBed.createComponent(ProcessesComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should call updateOnboardingProcess with ARCHIVED status', () => {
      onboardingService.updateOnboardingProcess.and.returnValue(
        of(mockActiveProcesses[0])
      );

      component.onArchiveProcess('process-1');

      expect(onboardingService.updateOnboardingProcess).toHaveBeenCalledWith('process-1', {
        status: 'ARCHIVED',
      });
    });

    it('should remove process from active processes after archiving', () => {
      onboardingService.updateOnboardingProcess.and.returnValue(
        of(mockActiveProcesses[0])
      );

      component.onArchiveProcess('process-1');

      expect(component.activeProcesses().length).toBe(0);
    });

    it('should handle error when archiving process', () => {
      spyOn(console, 'error'); // Suppress expected error logs

      onboardingService.updateOnboardingProcess.and.returnValue(
        throwError(() => new Error('Failed to archive'))
      );

      component.onArchiveProcess('process-1');

      expect(component.errorMessage()).toBe('Błąd podczas archiwizacji procesu');
    });
  });

  describe('Tab Change', () => {
    let component: ProcessesComponent;
    let fixture: ComponentFixture<ProcessesComponent>;
    let onboardingService: jasmine.SpyObj<OnboardingService>;

    beforeEach(async () => {
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
        'updateOnboardingProcess',
      ]);
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUserById']);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });
      const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      userServiceSpy.getUserById.and.returnValue(of(mockUser));

      onboardingServiceSpy.getOnboardingProcesses.and.returnValues(
        of({
          content: mockActiveProcesses,
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        }),
        of({
          content: mockCompletedProcesses,
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [ProcessesComponent],
        providers: [
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: Router, useValue: routerSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      onboardingService = TestBed.inject(OnboardingService) as jasmine.SpyObj<OnboardingService>;
      fixture = TestBed.createComponent(ProcessesComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should reload archived processes when switching to tab 1', () => {
      onboardingService.getOnboardingProcesses.calls.reset();
      onboardingService.getOnboardingProcesses.and.returnValue(
        of({
          content: mockCompletedProcesses,
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      component.onTabChange(1);

      expect(onboardingService.getOnboardingProcesses).toHaveBeenCalledWith(0, 100, {
        managerId: mockManager.id,
        status: 'ARCHIVED',
      });
    });

    it('should not reload when switching to other tabs', () => {
      onboardingService.getOnboardingProcesses.calls.reset();

      component.onTabChange(0);

      expect(onboardingService.getOnboardingProcesses).not.toHaveBeenCalled();
    });
  });

  describe('Empty States', () => {
    let component: ProcessesComponent;
    let fixture: ComponentFixture<ProcessesComponent>;

    beforeEach(async () => {
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
        'updateOnboardingProcess',
      ]);
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUserById']);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });
      const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      onboardingServiceSpy.getOnboardingProcesses.and.returnValues(
        of({
          content: [],
          totalElements: 0,
          totalPages: 0,
          size: 100,
          number: 0,
        }),
        of({
          content: [],
          totalElements: 0,
          totalPages: 0,
          size: 100,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [ProcessesComponent],
        providers: [
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: Router, useValue: routerSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(ProcessesComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should handle empty active processes', () => {
      expect(component.activeProcesses().length).toBe(0);
      expect(component.completedProcesses().length).toBe(0);
    });
  });

  describe('Time Calculations', () => {
    let component: ProcessesComponent;
    let fixture: ComponentFixture<ProcessesComponent>;

    beforeEach(async () => {
      const now = new Date();
      const fiveMinutesAgo = new Date(now.getTime() - 5 * 60000);

      const testProcess = {
        ...mockActiveProcesses[0],
        updatedAt: fiveMinutesAgo.toISOString(),
      };

      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
        'updateOnboardingProcess',
      ]);
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUserById']);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });
      const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      userServiceSpy.getUserById.and.returnValue(of(mockUser));

      onboardingServiceSpy.getOnboardingProcesses.and.returnValues(
        of({
          content: [testProcess],
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        }),
        of({
          content: [],
          totalElements: 0,
          totalPages: 0,
          size: 100,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [ProcessesComponent],
        providers: [
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: Router, useValue: routerSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(ProcessesComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should calculate time ago in minutes', () => {
      const process = component.activeProcesses()[0];
      expect(process.lastActivity).toContain('minut temu');
    });
  });

  describe('Duration Calculation', () => {
    let component: ProcessesComponent;
    let fixture: ComponentFixture<ProcessesComponent>;

    beforeEach(async () => {
      const startDate = '2024-01-01T00:00:00Z';
      const endDate = '2024-01-05T00:00:00Z';

      const testProcess = {
        ...mockCompletedProcesses[0],
        createdAt: startDate,
        updatedAt: endDate,
      };

      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
        'updateOnboardingProcess',
      ]);
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUserById']);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });
      const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      userServiceSpy.getUserById.and.returnValue(of(mockUser));

      onboardingServiceSpy.getOnboardingProcesses.and.returnValues(
        of({
          content: [],
          totalElements: 0,
          totalPages: 0,
          size: 100,
          number: 0,
        }),
        of({
          content: [testProcess],
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [ProcessesComponent],
        providers: [
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: Router, useValue: routerSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(ProcessesComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should calculate duration correctly', () => {
      const process = component.completedProcesses()[0];
      expect(process.duration).toContain('dni');
    });
  });
});
