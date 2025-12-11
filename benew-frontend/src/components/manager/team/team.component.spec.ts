import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';
import { TeamComponent } from './team.component';
import { UserService } from '../../../services/user.service';
import { AuthService } from '../../../services/auth.service';
import { OnboardingService } from '../../../services/onboarding.service';
import { UserRole } from '../../../models';
import { UserDto } from '../../../models/user.dto';
import { OnboardingProcessDto } from '../../../models/onboarding.dto';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('TeamComponent', () => {
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

  const mockActiveProcess: OnboardingProcessDto = {
    id: 'process-1',
    userId: 'user-1',
    managerId: 'manager-1',
    sourceTemplateId: 'template-1',
    status: 'ACTIVE',
    totalTasksCount: 10,
    completedTasksCount: 5,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z',
  };

  const mockCompletedProcess: OnboardingProcessDto = {
    id: 'process-2',
    userId: 'user-2',
    managerId: 'manager-1',
    sourceTemplateId: 'template-1',
    status: 'ARCHIVED',
    totalTasksCount: 10,
    completedTasksCount: 10,
    createdAt: '2024-01-03T00:00:00Z',
    updatedAt: '2024-01-10T00:00:00Z',
  };

  describe('Basic Functionality', () => {
    let component: TeamComponent;
    let fixture: ComponentFixture<TeamComponent>;
    let userService: jasmine.SpyObj<UserService>;
    let onboardingService: jasmine.SpyObj<OnboardingService>;

    beforeEach(async () => {
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUsers']);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
      ]);

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
          content: [mockActiveProcess],
          totalElements: 1,
          totalPages: 1,
          size: 1,
          number: 0,
        }),
        of({
          content: [mockCompletedProcess],
          totalElements: 1,
          totalPages: 1,
          size: 1,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [TeamComponent],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      userService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
      onboardingService = TestBed.inject(OnboardingService) as jasmine.SpyObj<OnboardingService>;
      fixture = TestBed.createComponent(TeamComponent);
      component = fixture.componentInstance;
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize with loading state', () => {
      expect(component.isLoading()).toBe(true);
    });

    it('should load team members on init', () => {
      fixture.detectChanges();

      expect(userService.getUsers).toHaveBeenCalledWith(0, 100, undefined, {
        managerId: mockManager.id,
      });
      expect(component.isLoading()).toBe(false);
    });

    it('should load onboarding processes for each team member', () => {
      fixture.detectChanges();

      expect(onboardingService.getOnboardingProcesses).toHaveBeenCalledTimes(2);
      expect(onboardingService.getOnboardingProcesses).toHaveBeenCalledWith(0, 1, {
        userId: 'user-1',
      });
      expect(onboardingService.getOnboardingProcesses).toHaveBeenCalledWith(0, 1, {
        userId: 'user-2',
      });
    });

    it('should build team member views with onboarding progress', () => {
      fixture.detectChanges();

      const members = component.teamMembers();
      expect(members.length).toBe(2);

      expect(members[0].id).toBe('user-1');
      expect(members[0].firstName).toBe('John');
      expect(members[0].lastName).toBe('Doe');
      expect(members[0].email).toBe('user1@example.com');
      expect(members[0].position).toBe('developer');
      expect(members[0].onboardingProgress).toBe(50);
      expect(members[0].status).toBe('active');
      expect(members[0].startDate).toBe('2024-01-01');

      expect(members[1].id).toBe('user-2');
      expect(members[1].onboardingProgress).toBe(100);
      expect(members[1].status).toBe('completed');
    });

    it('should set status to active for user with active process', () => {
      fixture.detectChanges();

      const member = component.teamMembers().find((m) => m.id === 'user-1');
      expect(member?.status).toBe('active');
    });

    it('should set status to completed for user with completed process', () => {
      fixture.detectChanges();

      const member = component.teamMembers().find((m) => m.id === 'user-2');
      expect(member?.status).toBe('completed');
    });

    it('should calculate progress correctly for active process', () => {
      fixture.detectChanges();

      const member = component.teamMembers().find((m) => m.id === 'user-1');
      expect(member?.onboardingProgress).toBe(50); // 5 out of 10
    });

    it('should show 100% progress for completed process', () => {
      fixture.detectChanges();

      const member = component.teamMembers().find((m) => m.id === 'user-2');
      expect(member?.onboardingProgress).toBe(100);
    });

    it('should format start date correctly', () => {
      fixture.detectChanges();

      const member = component.teamMembers()[0];
      expect(member.startDate).toBe('2024-01-01');
    });

    it('should log member id when viewing details', () => {
      spyOn(console, 'log');
      fixture.detectChanges();

      component.onViewDetails('user-1');
      expect(console.log).toHaveBeenCalledWith('View member details:', 'user-1');
    });

    it('should render mat-card', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const card = compiled.querySelector('mat-card');
      expect(card).toBeTruthy();
    });

    it('should render team member cards', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const cards = compiled.querySelectorAll('mat-card');
      expect(cards.length).toBeGreaterThan(0);
    });
  });

  describe('Status Determination - No Process', () => {
    let component: TeamComponent;
    let fixture: ComponentFixture<TeamComponent>;

    beforeEach(async () => {
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUsers']);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
      ]);

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
        of({
          content: [],
          totalElements: 0,
          totalPages: 0,
          size: 1,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [TeamComponent],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TeamComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should set status to no-process for user without any process', () => {
      const members = component.teamMembers();
      expect(members[0].status).toBe('no-process');
      expect(members[0].onboardingProgress).toBe(0);
    });

    it('should show 0% progress when no process exists', () => {
      const members = component.teamMembers();
      expect(members[0].onboardingProgress).toBe(0);
      expect(members[1].onboardingProgress).toBe(0);
    });
  });

  describe('Status Determination - Completed Priority', () => {
    let component: TeamComponent;
    let fixture: ComponentFixture<TeamComponent>;

    beforeEach(async () => {
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUsers']);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
      ]);

      userServiceSpy.getUsers.and.returnValue(
        of({
          content: [mockUsers[0]],
          totalElements: 1,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      // User has both active and completed processes
      onboardingServiceSpy.getOnboardingProcesses.and.returnValue(
        of({
          content: [mockActiveProcess, mockCompletedProcess],
          totalElements: 2,
          totalPages: 1,
          size: 1,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [TeamComponent],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TeamComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should prioritize completed status over active', () => {
      const member = component.teamMembers()[0];
      expect(member.status).toBe('completed');
    });
  });

  describe('Date Formatting - Missing Date', () => {
    let component: TeamComponent;
    let fixture: ComponentFixture<TeamComponent>;

    beforeEach(async () => {
      const usersWithoutDate = mockUsers.map((u) => ({ ...u, createdAt: undefined }));

      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUsers']);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
      ]);

      userServiceSpy.getUsers.and.returnValue(
        of({
          content: usersWithoutDate,
          totalElements: 2,
          totalPages: 1,
          size: 100,
          number: 0,
        })
      );

      onboardingServiceSpy.getOnboardingProcesses.and.returnValue(
        of({
          content: [],
          totalElements: 0,
          totalPages: 0,
          size: 1,
          number: 0,
        })
      );

      await TestBed.configureTestingModule({
        imports: [TeamComponent],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TeamComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should handle missing createdAt date', () => {
      const member = component.teamMembers()[0];
      expect(member.startDate).toBe('N/A');
    });
  });

  describe('Error Handling - Team Members', () => {
    let component: TeamComponent;
    let fixture: ComponentFixture<TeamComponent>;

    beforeEach(async () => {
      spyOn(console, 'error'); // Suppress expected error logs

      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUsers']);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
      ]);

      userServiceSpy.getUsers.and.returnValue(
        throwError(() => new Error('Failed to load'))
      );

      await TestBed.configureTestingModule({
        imports: [TeamComponent],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TeamComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should handle error when loading team members', () => {
      expect(component.errorMessage()).toBe('Błąd podczas ładowania członków zespołu');
      expect(component.isLoading()).toBe(false);
    });
  });

  describe('Error Handling - Onboarding Processes', () => {
    let component: TeamComponent;
    let fixture: ComponentFixture<TeamComponent>;

    beforeEach(async () => {
      spyOn(console, 'error'); // Suppress expected error logs

      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUsers']);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
      ]);

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
        imports: [TeamComponent],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TeamComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should handle error when loading onboarding processes', () => {
      expect(component.errorMessage()).toBe('Błąd podczas ładowania procesów onboardingu');
      expect(component.isLoading()).toBe(false);
    });
  });

  describe('Null User Handling', () => {
    let component: TeamComponent;
    let fixture: ComponentFixture<TeamComponent>;

    beforeEach(async () => {
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUsers']);
      const nullAuthService = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(null),
      });
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
      ]);

      await TestBed.configureTestingModule({
        imports: [TeamComponent],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: nullAuthService },
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TeamComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should handle null currentUser', () => {
      expect(component.errorMessage()).toBe('Brak danych użytkownika');
      expect(component.isLoading()).toBe(false);
    });
  });

  describe('Empty Team', () => {
    let component: TeamComponent;
    let fixture: ComponentFixture<TeamComponent>;

    beforeEach(async () => {
      const userServiceSpy = jasmine.createSpyObj('UserService', ['getUsers']);
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockManager),
      });
      const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
        'getOnboardingProcesses',
      ]);

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
        imports: [TeamComponent],
        providers: [
          { provide: UserService, useValue: userServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: OnboardingService, useValue: onboardingServiceSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TeamComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should handle no team members', () => {
      expect(component.teamMembers().length).toBe(0);
      expect(component.isLoading()).toBe(false);
    });
  });
});
