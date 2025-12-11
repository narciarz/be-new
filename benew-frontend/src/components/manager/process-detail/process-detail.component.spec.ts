import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ProcessDetailComponent } from './process-detail.component';
import { OnboardingService } from '../../../services/onboarding.service';
import { UserService } from '../../../services/user.service';
import { OnboardingProcessDto, OnboardingTaskDto } from '../../../models/onboarding.dto';
import { UserDto } from '../../../models/user.dto';
import { UserRole } from '../../../models/user-role';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('ProcessDetailComponent', () => {
  let component: ProcessDetailComponent;
  let fixture: ComponentFixture<ProcessDetailComponent>;
  let onboardingService: jasmine.SpyObj<OnboardingService>;
  let userService: jasmine.SpyObj<UserService>;
  let router: jasmine.SpyObj<Router>;
  let activatedRoute: any;

  const mockProcess: OnboardingProcessDto = {
    id: 'process-1',
    userId: 'user-1',
    managerId: 'manager-1',
    sourceTemplateId: 'template-1',
    status: 'ACTIVE',
    totalTasksCount: 5,
    completedTasksCount: 2,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z',
  };

  const mockTasks: OnboardingTaskDto[] = [
    {
      id: 'task-1',
      processId: 'process-1',
      title: 'Task 1',
      description: 'Description 1',
      isCompleted: true,
      taskOrder: 1,
      ownerRole: 'USER',
    },
    {
      id: 'task-2',
      processId: 'process-1',
      title: 'Task 2',
      description: 'Description 2',
      isCompleted: false,
      taskOrder: 2,
      ownerRole: 'MANAGER',
    },
  ];

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

  const mockManager: UserDto = {
    id: 'manager-1',
    email: 'manager@example.com',
    firstName: 'Manager',
    lastName: 'User',
    positionName: 'manager',
    role: UserRole.MANAGER,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z',
  };

  beforeEach(async () => {
    const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
      'getOnboardingProcessById',
      'getOnboardingTasks',
    ]);
    const userServiceSpy = jasmine.createSpyObj('UserService', ['getUserById']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    activatedRoute = {
      snapshot: {
        paramMap: {
          get: jasmine.createSpy('get').and.returnValue('process-1'),
        },
      },
    };

    await TestBed.configureTestingModule({
      imports: [ProcessDetailComponent],
      providers: [
        { provide: OnboardingService, useValue: onboardingServiceSpy },
        { provide: UserService, useValue: userServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: activatedRoute },
        provideAnimations(),
      ],
    }).compileComponents();

    onboardingService = TestBed.inject(OnboardingService) as jasmine.SpyObj<OnboardingService>;
    userService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    // Setup default responses
    onboardingService.getOnboardingProcessById.and.returnValue(of(mockProcess));
    onboardingService.getOnboardingTasks.and.returnValue(of(mockTasks));
    userService.getUserById.and.returnValues(of(mockUser), of(mockManager));
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProcessDetailComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with loading state', () => {
    expect(component.isLoading()).toBe(true);
  });

  it('should load process details on init', () => {
    fixture.detectChanges();

    expect(onboardingService.getOnboardingProcessById).toHaveBeenCalledWith('process-1');
    expect(onboardingService.getOnboardingTasks).toHaveBeenCalledWith('process-1');
  });

  it('should load user and manager details', () => {
    fixture.detectChanges();

    expect(userService.getUserById).toHaveBeenCalledWith('user-1');
    expect(userService.getUserById).toHaveBeenCalledWith('manager-1');
  });

  it('should set process data after load', () => {
    fixture.detectChanges();

    expect(component.process()).toEqual(mockProcess);
    expect(component.tasks()).toEqual(mockTasks);
    expect(component.userName()).toBe('John Doe');
    expect(component.managerName()).toBe('Manager User');
    expect(component.isLoading()).toBe(false);
  });

  describe('Error Handling', () => {
    it('should handle error when loading process details', () => {
      spyOn(console, 'error'); // Suppress expected error logs

      onboardingService.getOnboardingProcessById.and.returnValue(
        throwError(() => new Error('Failed to load'))
      );

      fixture.detectChanges();

      expect(component.errorMessage()).toBe('Błąd podczas ładowania szczegółów procesu');
      expect(component.isLoading()).toBe(false);
    });

    it('should handle error when loading user details', () => {
      spyOn(console, 'error'); // Suppress expected error logs

      userService.getUserById.and.returnValue(
        throwError(() => new Error('Failed to load user'))
      );

      fixture.detectChanges();

      // Component should still finish loading even if user details fail
      expect(component.isLoading()).toBe(false);
    });

    it('should handle missing process ID', () => {
      activatedRoute.snapshot.paramMap.get.and.returnValue(null);
      fixture = TestBed.createComponent(ProcessDetailComponent);
      component = fixture.componentInstance;

      fixture.detectChanges();

      expect(component.errorMessage()).toBe('Brak ID procesu');
      expect(component.isLoading()).toBe(false);
    });
  });

  describe('getProgress', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should calculate progress correctly', () => {
      expect(component.getProgress()).toBe(40); // 2 out of 5 = 40%
    });

    it('should return 0 when no process', () => {
      component.process.set(null);
      expect(component.getProgress()).toBe(0);
    });

    it('should return 0 when total tasks is 0', () => {
      component.process.set({
        ...mockProcess,
        totalTasksCount: 0,
        completedTasksCount: 0,
      });
      expect(component.getProgress()).toBe(0);
    });

    it('should return 100 when all tasks completed', () => {
      component.process.set({
        ...mockProcess,
        totalTasksCount: 5,
        completedTasksCount: 5,
      });
      expect(component.getProgress()).toBe(100);
    });
  });

  describe('getStatusLabel', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should return Aktywny for ACTIVE status', () => {
      expect(component.getStatusLabel()).toBe('Aktywny');
    });

    it('should return Zarchiwizowany for ARCHIVED status', () => {
      component.process.set({
        ...mockProcess,
        status: 'ARCHIVED',
      });
      expect(component.getStatusLabel()).toBe('Zarchiwizowany');
    });
  });

  describe('getStatusColor', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should return primary for ACTIVE status', () => {
      expect(component.getStatusColor()).toBe('primary');
    });

    it('should return accent for ARCHIVED status', () => {
      component.process.set({
        ...mockProcess,
        status: 'ARCHIVED',
      });
      expect(component.getStatusColor()).toBe('accent');
    });
  });

  describe('Date Formatting', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should format created date correctly', () => {
      expect(component.getCreatedDate()).toBe('2024-01-01');
    });

    it('should format updated date correctly', () => {
      expect(component.getUpdatedDate()).toBe('2024-01-02');
    });

    it('should return N/A when date is missing', () => {
      component.process.set({
        ...mockProcess,
        createdAt: undefined,
        updatedAt: undefined,
      });
      expect(component.getCreatedDate()).toBe('N/A');
      expect(component.getUpdatedDate()).toBe('N/A');
    });
  });

  describe('Task Icons', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should return check_circle for completed task', () => {
      const task = mockTasks[0];
      expect(component.getTaskIcon(task)).toBe('check_circle');
    });

    it('should return radio_button_unchecked for incomplete task', () => {
      const task = mockTasks[1];
      expect(component.getTaskIcon(task)).toBe('radio_button_unchecked');
    });

    it('should return completed color for completed task', () => {
      const task = mockTasks[0];
      expect(component.getTaskIconColor(task)).toBe('completed');
    });

    it('should return pending color for incomplete task', () => {
      const task = mockTasks[1];
      expect(component.getTaskIconColor(task)).toBe('pending');
    });
  });

  describe('Role Labels', () => {
    it('should return correct label for EMPLOYEE role', () => {
      expect(component.getRoleLabel('EMPLOYEE')).toBe('Pracownik');
    });

    it('should return correct label for MANAGER role', () => {
      expect(component.getRoleLabel('MANAGER')).toBe('Menedżer');
    });

    it('should return correct label for HR role', () => {
      expect(component.getRoleLabel('HR')).toBe('HR');
    });

    it('should return correct label for IT role', () => {
      expect(component.getRoleLabel('IT')).toBe('IT');
    });

    it('should return original role for unknown role', () => {
      expect(component.getRoleLabel('UNKNOWN')).toBe('UNKNOWN');
    });
  });

  describe('Role Colors', () => {
    it('should return correct color for EMPLOYEE role', () => {
      expect(component.getRoleColor('EMPLOYEE')).toBe('employee');
    });

    it('should return correct color for MANAGER role', () => {
      expect(component.getRoleColor('MANAGER')).toBe('manager');
    });

    it('should return correct color for HR role', () => {
      expect(component.getRoleColor('HR')).toBe('hr');
    });

    it('should return correct color for IT role', () => {
      expect(component.getRoleColor('IT')).toBe('it');
    });

    it('should return empty string for unknown role', () => {
      expect(component.getRoleColor('UNKNOWN')).toBe('');
    });
  });

  describe('onBack', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should navigate back to processes list', () => {
      component.onBack();
      expect(router.navigate).toHaveBeenCalledWith(['/dashboard/manager/processes']);
    });
  });

  describe('Component Rendering', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should render mat-card', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const card = compiled.querySelector('mat-card');
      expect(card).toBeTruthy();
    });

    it('should render mat-progress-bar', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const progressBar = compiled.querySelector('mat-progress-bar');
      expect(progressBar).toBeTruthy();
    });
  });
});
