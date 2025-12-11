import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';
import { OverviewComponent } from './overview.component';
import { UserService } from '../../../services/user.service';
import { OnboardingService } from '../../../services/onboarding.service';
import { AuthService } from '../../../services/auth.service';
import { UserRole } from '../../../models';
import { UserDto } from '../../../models/user.dto';
import { OnboardingProcessDto } from '../../../models/onboarding.dto';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('OverviewComponent', () => {
  const mockManager = {
    id: 'manager-1',
    email: 'manager@example.com',
    role: UserRole.MANAGER,
    firstName: 'Manager',
    lastName: 'User',
    token: 'test-token',
  };

  const mockUsers: UserDto[] = [
    {
      id: 'user-1',
      email: 'user1@example.com',
      firstName: 'John',
      lastName: 'Doe',
      positionName: 'developer',
      role: UserRole.USER,
      managerId: 'manager-1',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-02T00:00:00Z',
    },
    {
      id: 'user-2',
      email: 'user2@example.com',
      firstName: 'Jane',
      lastName: 'Smith',
      positionName: 'designer',
      role: UserRole.USER,
      managerId: 'manager-1',
      createdAt: '2024-01-03T00:00:00Z',
      updatedAt: '2024-01-04T00:00:00Z',
    },
  ];

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
    {
      id: 'process-2',
      userId: 'user-2',
      managerId: 'manager-1',
      sourceTemplateId: 'template-1',
      status: 'ACTIVE',
      totalTasksCount: 10,
      completedTasksCount: 8,
      createdAt: '2024-01-03T00:00:00Z',
      updatedAt: '2024-01-04T00:00:00Z',
    },
  ];

  const mockCompletedProcesses: OnboardingProcessDto[] = [
    {
      id: 'process-3',
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
    let component: OverviewComponent;
    let fixture: ComponentFixture<OverviewComponent>;
    let userService: jasmine.SpyObj<UserService>;
    let onboardingService: jasmine.SpyObj<OnboardingService>;

    beforeEach(async () => {
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUsers']);
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
      ]);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });

      userServiceSpy.getUsers.and.returnValue(
        of({
          content: mockUsers,
          totalElements: 2,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      onboardingServiceSpy.getOnboardingProcesses.and.returnValues(
        of({
          content: mockActiveProcesses,
          totalElements: 2,
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
        imports: [OverviewComponent],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      userService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
      onboardingService = TestBed.inject(OnboardingService) as jasmine.SpyObj<OnboardingService>;
      fixture = TestBed.createComponent(OverviewComponent);
      component = fixture.componentInstance;
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize with loading state', () => {
      expect(component.isLoading()).toBe(true);
    });

    it('should load dashboard data on init', () => {
      fixture.detectChanges();

      expect(userService.getUsers).toHaveBeenCalledWith(0, 100, undefined, {
        managerId: mockManager.id,
      });
      expect(onboardingService.getOnboardingProcesses).toHaveBeenCalledWith(0, 100, {
        managerId: mockManager.id,
        status: 'ACTIVE',
      });
      expect(onboardingService.getOnboardingProcesses).toHaveBeenCalledWith(0, 100, {
        managerId: mockManager.id,
        status: 'ARCHIVED',
      });
    });

    it('should calculate stats correctly', () => {
      fixture.detectChanges();

      const stats = component.stats();
      expect(stats.totalTeamMembers).toBe(2);
      expect(stats.activeOnboarding).toBe(2);
      expect(stats.completedOnboarding).toBe(1);
      expect(stats.averageProgress).toBe(65); // (50 + 80) / 2 = 65
    });

    it('should build team progress list', () => {
      fixture.detectChanges();

      const teamProgress = component.teamProgress();
      expect(teamProgress.length).toBe(2);

      expect(teamProgress[0].name).toBe('John Doe');
      expect(teamProgress[0].position).toBe('developer');
      expect(teamProgress[0].progress).toBe(50);
      expect(teamProgress[0].tasksLeft).toBe(5);

      expect(teamProgress[1].name).toBe('Jane Smith');
      expect(teamProgress[1].position).toBe('designer');
      expect(teamProgress[1].progress).toBe(80);
      expect(teamProgress[1].tasksLeft).toBe(2);
    });

    it('should set loading to false after successful load', () => {
      fixture.detectChanges();

      expect(component.isLoading()).toBe(false);
      expect(component.errorMessage()).toBeNull();
    });

    it('should render mat-card', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const cards = compiled.querySelectorAll('mat-card');
      expect(cards.length).toBeGreaterThan(0);
    });

    it('should render stats cards', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const cards = compiled.querySelectorAll('mat-card');
      // Should have at least the main stats cards
      expect(cards.length).toBeGreaterThan(0);
    });
  });

  describe('Error Handling - Team Members', () => {
    let component: OverviewComponent;
    let fixture: ComponentFixture<OverviewComponent>;

    beforeEach(async () => {
      spyOn(console, 'error'); // Suppress expected error logs

      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUsers']);
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
      ]);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });

      userServiceSpy.getUsers.and.returnValue(
        throwError(() => new Error('Failed to load'))
      );

      await TestBed.configureTestingModule({
        imports: [OverviewComponent],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(OverviewComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should handle error when loading team members', () => {
      expect(component.errorMessage()).toBe('Błąd podczas ładowania zespołu');
      expect(component.isLoading()).toBe(false);
    });
  });

  describe('Error Handling - Onboarding Processes', () => {
    let component: OverviewComponent;
    let fixture: ComponentFixture<OverviewComponent>;

    beforeEach(async () => {
      spyOn(console, 'error'); // Suppress expected error logs

      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUsers']);
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
      ]);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });

      userServiceSpy.getUsers.and.returnValue(
        of({
          content: mockUsers,
          totalElements: 2,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      onboardingServiceSpy.getOnboardingProcesses.and.returnValue(
        throwError(() => new Error('Failed to load'))
      );

      await TestBed.configureTestingModule({
        imports: [OverviewComponent],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(OverviewComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should handle error when loading onboarding processes', () => {
      expect(component.errorMessage()).toBe('Błąd podczas ładowania procesów');
      expect(component.isLoading()).toBe(false);
    });
  });

  describe('Null User Handling', () => {
    let component: OverviewComponent;
    let fixture: ComponentFixture<OverviewComponent>;

    beforeEach(async () => {
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUsers']);
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
      ]);
      const nullAuthService = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(null),
      });

      await TestBed.configureTestingModule({
        imports: [OverviewComponent],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          { provide: AuthService, useValue: nullAuthService },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(OverviewComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should handle null currentUser', () => {
      expect(component.errorMessage()).toBe('Brak danych użytkownika');
      expect(component.isLoading()).toBe(false);
    });
  });

  describe('Empty States', () => {
    let component: OverviewComponent;
    let fixture: ComponentFixture<OverviewComponent>;

    beforeEach(async () => {
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUsers']);
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
      ]);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });

      userServiceSpy.getUsers.and.returnValue(
        of({
          content: [],
          totalElements: 0,
          totalPages: 0,
          size: 100,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [OverviewComponent],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(OverviewComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should handle no team members', () => {
      const stats = component.stats();
      expect(stats.totalTeamMembers).toBe(0);
      expect(stats.activeOnboarding).toBe(0);
      expect(stats.completedOnboarding).toBe(0);
      expect(stats.averageProgress).toBe(0);
      expect(component.teamProgress().length).toBe(0);
    });
  });

  describe('Progress Calculations', () => {
    let component: OverviewComponent;
    let fixture: ComponentFixture<OverviewComponent>;
    let onboardingService: jasmine.SpyObj<OnboardingService>;

    beforeEach(async () => {
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUsers']);
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
      ]);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });

      userServiceSpy.getUsers.and.returnValue(
        of({
          content: mockUsers,
          totalElements: 2,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [OverviewComponent],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      onboardingService = TestBed.inject(OnboardingService) as jasmine.SpyObj<OnboardingService>;
      fixture = TestBed.createComponent(OverviewComponent);
      component = fixture.componentInstance;
    });

    it('should calculate average progress correctly with one process', () => {
      onboardingService.getOnboardingProcesses.and.returnValues(
        of({
          content: [mockActiveProcesses[0]],
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

      fixture.detectChanges();

      const stats = component.stats();
      expect(stats.averageProgress).toBe(50);
    });

    it('should handle division by zero in average progress', () => {
      onboardingService.getOnboardingProcesses.and.returnValues(
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

      fixture.detectChanges();

      const stats = component.stats();
      expect(stats.averageProgress).toBe(0);
    });

    it('should round average progress to nearest integer', () => {
      const processWithOddProgress = {
        ...mockActiveProcesses[0],
        totalTasksCount: 3,
        completedTasksCount: 1, // 33.33%
      };

      onboardingService.getOnboardingProcesses.and.returnValues(
        of({
          content: [processWithOddProgress],
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

      fixture.detectChanges();

      const stats = component.stats();
      expect(stats.averageProgress).toBe(33);
    });
  });

  describe('Team Progress Mapping', () => {
    let component: OverviewComponent;
    let fixture: ComponentFixture<OverviewComponent>;
    let onboardingService: jasmine.SpyObj<OnboardingService>;

    beforeEach(async () => {
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUsers']);
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
      ]);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });

      userServiceSpy.getUsers.and.returnValue(
        of({
          content: mockUsers,
          totalElements: 2,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [OverviewComponent],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      onboardingService = TestBed.inject(OnboardingService) as jasmine.SpyObj<OnboardingService>;
      fixture = TestBed.createComponent(OverviewComponent);
      component = fixture.componentInstance;
    });

    it('should handle user not found in team list', () => {
      const processForUnknownUser = {
        ...mockActiveProcesses[0],
        userId: 'unknown-user',
      };

      onboardingService.getOnboardingProcesses.and.returnValues(
        of({
          content: [processForUnknownUser],
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

      fixture.detectChanges();

      const teamProgress = component.teamProgress();
      expect(teamProgress[0].name).toBe('Unknown');
      expect(teamProgress[0].position).toBe('N/A');
    });
  });

  describe('Empty Active Processes', () => {
    let component: OverviewComponent;
    let fixture: ComponentFixture<OverviewComponent>;

    beforeEach(async () => {
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUsers']);
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
      ]);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });

      userServiceSpy.getUsers.and.returnValue(
        of({
          content: mockUsers,
          totalElements: 2,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      onboardingServiceSpy.getOnboardingProcesses.and.returnValues(
        of({
          content: [],
          totalElements: 0,
          totalPages: 0,
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
        imports: [OverviewComponent],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(OverviewComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should handle no active processes', () => {
      const stats = component.stats();
      expect(stats.activeOnboarding).toBe(0);
      expect(stats.averageProgress).toBe(0);
      expect(component.teamProgress().length).toBe(0);
    });
  });
});
