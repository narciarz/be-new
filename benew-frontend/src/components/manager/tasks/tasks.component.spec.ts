import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { TasksComponent } from './tasks.component';
import { OnboardingService } from '../../../services/onboarding.service';
import { ManagerTaskDto } from '../../../models/onboarding.dto';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('TasksComponent', () => {
  let component: TasksComponent;
  let fixture: ComponentFixture<TasksComponent>;
  let onboardingService: jasmine.SpyObj<OnboardingService>;

  const mockManagerTasks: ManagerTaskDto[] = [
    {
      id: 'task-1',
      processId: 'process-1',
      title: 'Task 1',
      description: 'Description 1',
      isCompleted: true,
      taskOrder: 1,
      ownerRole: 'MANAGER',
      userId: 'user-1',
      userFirstName: 'John',
      userLastName: 'Doe',
      userPosition: 'developer',
      processStatus: 'ACTIVE',
    },
    {
      id: 'task-2',
      processId: 'process-1',
      title: 'Task 2',
      description: 'Description 2',
      isCompleted: false,
      taskOrder: 2,
      ownerRole: 'MANAGER',
      userId: 'user-1',
      userFirstName: 'John',
      userLastName: 'Doe',
      userPosition: 'developer',
      processStatus: 'ACTIVE',
    },
    {
      id: 'task-3',
      processId: 'process-2',
      title: 'Task 3',
      description: 'Description 3',
      isCompleted: false,
      taskOrder: 1,
      ownerRole: 'MANAGER',
      userId: 'user-2',
      userFirstName: 'Jane',
      userLastName: 'Smith',
      userPosition: 'designer',
      processStatus: 'ACTIVE',
    },
  ];

  beforeEach(async () => {
    const onboardingServiceSpy = jasmine.createSpyObj('OnboardingService', [
      'getManagerTasks',
      'updateOnboardingTask',
    ]);

    await TestBed.configureTestingModule({
      imports: [TasksComponent],
      providers: [
        { provide: OnboardingService, useValue: onboardingServiceSpy },
        provideAnimations(),
      ],
    }).compileComponents();

    onboardingService = TestBed.inject(OnboardingService) as jasmine.SpyObj<OnboardingService>;
  });

  beforeEach(() => {
    onboardingService.getManagerTasks.and.returnValue(of(mockManagerTasks));

    fixture = TestBed.createComponent(TasksComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with loading state', () => {
    expect(component.isLoading()).toBe(true);
  });

  it('should load manager tasks on init', () => {
    fixture.detectChanges();

    expect(onboardingService.getManagerTasks).toHaveBeenCalled();
    expect(component.isLoading()).toBe(false);
  });

  it('should group tasks by user', () => {
    fixture.detectChanges();

    const groups = component.taskGroups();
    expect(groups.length).toBe(2);

    expect(groups[0].userName).toBe('John Doe');
    expect(groups[0].userPosition).toBe('developer');
    expect(groups[0].tasks.length).toBe(2);

    expect(groups[1].userName).toBe('Jane Smith');
    expect(groups[1].userPosition).toBe('designer');
    expect(groups[1].tasks.length).toBe(1);
  });

  it('should sort tasks within groups by taskOrder', () => {
    fixture.detectChanges();

    const groups = component.taskGroups();
    const johnTasks = groups[0].tasks;

    expect(johnTasks[0].taskOrder).toBe(1);
    expect(johnTasks[1].taskOrder).toBe(2);
  });

  it('should calculate stats correctly', () => {
    fixture.detectChanges();

    const stats = component.stats();
    expect(stats.total).toBe(3);
    expect(stats.completed).toBe(1);
    expect(stats.pending).toBe(2);
  });

  describe('Error Handling', () => {
    it('should handle error when loading tasks', () => {
      spyOn(console, 'error'); // Suppress expected error logs

      onboardingService.getManagerTasks.and.returnValue(
        throwError(() => new Error('Failed to load'))
      );

      fixture.detectChanges();

      expect(component.errorMessage()).toBe('Błąd podczas ładowania zadań');
      expect(component.isLoading()).toBe(false);
    });
  });

  describe('onToggleTask', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should call updateOnboardingTask with correct parameters', () => {
      const task = mockManagerTasks[1];
      const updatedTask = { ...task, isCompleted: true };
      onboardingService.updateOnboardingTask.and.returnValue(of(updatedTask as any));

      component.onToggleTask(task);

      expect(onboardingService.updateOnboardingTask).toHaveBeenCalledWith(
        'process-1',
        'task-2',
        { isCompleted: true }
      );
    });

    it('should update local task state after successful toggle', () => {
      const task = mockManagerTasks[1];
      const updatedTask = { ...task, isCompleted: true };
      onboardingService.updateOnboardingTask.and.returnValue(of(updatedTask as any));

      component.onToggleTask(task);

      expect(task.isCompleted).toBe(true);
    });

    it('should recalculate stats after toggle', () => {
      const task = mockManagerTasks[1];
      const updatedTask = { ...task, isCompleted: true };
      onboardingService.updateOnboardingTask.and.returnValue(of(updatedTask as any));

      component.onToggleTask(task);

      const stats = component.stats();
      expect(stats.completed).toBe(2);
      expect(stats.pending).toBe(1);
    });

    it('should handle error when updating task', () => {
      spyOn(console, 'error'); // Suppress expected error logs

      const task = mockManagerTasks[1];
      onboardingService.updateOnboardingTask.and.returnValue(
        throwError(() => new Error('Failed to update'))
      );

      component.onToggleTask(task);

      expect(component.errorMessage()).toBe('Błąd podczas aktualizacji zadania');
    });

    it('should toggle from completed to incomplete', () => {
      const task = mockManagerTasks[0]; // Already completed
      const updatedTask = { ...task, isCompleted: false };
      onboardingService.updateOnboardingTask.and.returnValue(of(updatedTask as any));

      component.onToggleTask(task);

      expect(onboardingService.updateOnboardingTask).toHaveBeenCalledWith(
        'process-1',
        'task-1',
        { isCompleted: false }
      );
    });
  });

  describe('onRefresh', () => {
    beforeEach(() => {
      fixture.detectChanges();
      onboardingService.getManagerTasks.calls.reset();
    });

    it('should reload manager tasks', () => {
      component.onRefresh();

      expect(onboardingService.getManagerTasks).toHaveBeenCalled();
    });
  });

  describe('getPendingTasksCount', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should return correct pending count for group', () => {
      const groups = component.taskGroups();
      const johnGroup = groups[0];

      const pendingCount = component.getPendingTasksCount(johnGroup);
      expect(pendingCount).toBe(1); // Only task-2 is pending
    });

    it('should return 0 for group with all tasks completed', () => {
      // Update all tasks in first group to completed
      mockManagerTasks[0].isCompleted = true;
      mockManagerTasks[1].isCompleted = true;

      onboardingService.getManagerTasks.and.returnValue(of(mockManagerTasks));
      fixture = TestBed.createComponent(TasksComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      const groups = component.taskGroups();
      const johnGroup = groups[0];

      const pendingCount = component.getPendingTasksCount(johnGroup);
      expect(pendingCount).toBe(0);
    });
  });

  describe('Empty States', () => {
    it('should handle no tasks', () => {
      onboardingService.getManagerTasks.and.returnValue(of([]));

      fixture = TestBed.createComponent(TasksComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      expect(component.taskGroups().length).toBe(0);
      const stats = component.stats();
      expect(stats.total).toBe(0);
      expect(stats.completed).toBe(0);
      expect(stats.pending).toBe(0);
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

    it('should render mat-expansion-panel for groups', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const expansionPanels = compiled.querySelectorAll('mat-expansion-panel');
      expect(expansionPanels.length).toBeGreaterThan(0);
    });
  });
});
