import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { TaskDialogComponent } from './task-dialog.component';
import { TemplateService } from '../../../services/template.service';
import { TemplateTaskDto } from '../../../models/template.dto';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('TaskDialogComponent', () => {
  const mockTask: TemplateTaskDto = {
    id: 'task-1',
    templateId: 'template-1',
    title: 'Task 1',
    description: 'Description 1',
    taskOrder: 1,
    ownerRole: 'USER',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z',
  };

  describe('Create Mode', () => {
    let component: TaskDialogComponent;
    let fixture: ComponentFixture<TaskDialogComponent>;
    let templateService: jasmine.SpyObj<TemplateService>;
    let dialogRef: jasmine.SpyObj<MatDialogRef<TaskDialogComponent>>;

    const createModeData = {
      mode: 'create' as const,
      templateId: 'template-1',
      existingTasksCount: 2,
    };

    beforeEach(async () => {
      const templateServiceSpy = jasmine.createSpyObj('TemplateService', [
        'createTemplateTask',
        'updateTemplateTask',
      ]);
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      await TestBed.configureTestingModule({
        imports: [TaskDialogComponent, ReactiveFormsModule],
        providers: [
          { provide: TemplateService, useValue: templateServiceSpy },
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: createModeData },
          provideAnimations(),
        ],
      }).compileComponents();

      templateService = TestBed.inject(TemplateService) as jasmine.SpyObj<TemplateService>;
      dialogRef = TestBed.inject(MatDialogRef) as jasmine.SpyObj<MatDialogRef<TaskDialogComponent>>;

      fixture = TestBed.createComponent(TaskDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create in create mode', () => {
      expect(component).toBeTruthy();
      expect(component.isEditMode).toBe(false);
    });

    it('should initialize form with default values', () => {
      expect(component.taskForm.value.title).toBe('');
      expect(component.taskForm.value.description).toBe('');
      expect(component.taskForm.value.taskOrder).toBe(3); // existingTasksCount + 1
      expect(component.taskForm.value.ownerRole).toBe('USER');
    });

    it('should call createTemplateTask on save', () => {
      templateService.createTemplateTask.and.returnValue(of(mockTask));

      component.taskForm.patchValue({
        title: 'New Task',
        description: 'New Description',
        taskOrder: 1,
        ownerRole: 'USER',
      });

      component.onSave();

      expect(templateService.createTemplateTask).toHaveBeenCalledWith('template-1', {
        title: 'New Task',
        description: 'New Description',
        taskOrder: 1,
        ownerRole: 'USER',
      });
    });

    it('should close dialog with created task', () => {
      templateService.createTemplateTask.and.returnValue(of(mockTask));

      component.taskForm.patchValue({
        title: 'New Task',
        description: 'New Description',
        taskOrder: 1,
        ownerRole: 'USER',
      });

      component.onSave();

      expect(dialogRef.close).toHaveBeenCalledWith(mockTask);
    });

    it('should handle error when creating task', () => {
      templateService.createTemplateTask.and.returnValue(
        throwError(() => ({ error: { message: 'Error creating' } }))
      );

      component.taskForm.patchValue({
        title: 'New Task',
        description: 'New Description',
        taskOrder: 1,
        ownerRole: 'USER',
      });

      component.onSave();

      expect(component.errorMessage()).toBe('Error creating');
      expect(component.isSaving()).toBe(false);
    });
  });

  describe('Edit Mode', () => {
    let component: TaskDialogComponent;
    let fixture: ComponentFixture<TaskDialogComponent>;
    let templateService: jasmine.SpyObj<TemplateService>;
    let dialogRef: jasmine.SpyObj<MatDialogRef<TaskDialogComponent>>;

    const editModeData = {
      mode: 'edit' as const,
      templateId: 'template-1',
      task: mockTask,
      existingTasksCount: 2,
    };

    beforeEach(async () => {
      const templateServiceSpy = jasmine.createSpyObj('TemplateService', [
        'createTemplateTask',
        'updateTemplateTask',
      ]);
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      await TestBed.configureTestingModule({
        imports: [TaskDialogComponent, ReactiveFormsModule],
        providers: [
          { provide: TemplateService, useValue: templateServiceSpy },
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: editModeData },
          provideAnimations(),
        ],
      }).compileComponents();

      templateService = TestBed.inject(TemplateService) as jasmine.SpyObj<TemplateService>;
      dialogRef = TestBed.inject(MatDialogRef) as jasmine.SpyObj<MatDialogRef<TaskDialogComponent>>;

      fixture = TestBed.createComponent(TaskDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create in edit mode', () => {
      expect(component.isEditMode).toBe(true);
    });

    it('should initialize form with task data', () => {
      expect(component.taskForm.value).toEqual({
        title: mockTask.title,
        description: mockTask.description,
        taskOrder: mockTask.taskOrder,
        ownerRole: mockTask.ownerRole,
      });
    });

    it('should call updateTemplateTask on save', () => {
      templateService.updateTemplateTask.and.returnValue(of(mockTask));

      component.taskForm.patchValue({
        title: 'Updated Task',
        description: 'Updated Description',
        taskOrder: 2,
        ownerRole: 'MANAGER',
      });

      component.onSave();

      expect(templateService.updateTemplateTask).toHaveBeenCalledWith(
        'template-1',
        'task-1',
        {
          title: 'Updated Task',
          description: 'Updated Description',
          taskOrder: 2,
          ownerRole: 'MANAGER',
        }
      );
    });

    it('should close dialog with updated task', () => {
      const updatedTask = { ...mockTask, title: 'Updated Task' };
      templateService.updateTemplateTask.and.returnValue(of(updatedTask));

      component.taskForm.patchValue({
        title: 'Updated Task',
      });

      component.onSave();

      expect(dialogRef.close).toHaveBeenCalledWith(updatedTask);
    });

    it('should handle error when updating task', () => {
      templateService.updateTemplateTask.and.returnValue(
        throwError(() => ({ error: { message: 'Error updating' } }))
      );

      component.taskForm.patchValue({
        title: 'Updated Task',
      });

      component.onSave();

      expect(component.errorMessage()).toBe('Error updating');
      expect(component.isSaving()).toBe(false);
    });
  });

  describe('Form Validation', () => {
    let component: TaskDialogComponent;
    let fixture: ComponentFixture<TaskDialogComponent>;
    let templateService: jasmine.SpyObj<TemplateService>;

    beforeEach(async () => {
      const templateServiceSpy = jasmine.createSpyObj('TemplateService', [
        'createTemplateTask',
        'updateTemplateTask',
      ]);
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      await TestBed.configureTestingModule({
        imports: [TaskDialogComponent, ReactiveFormsModule],
        providers: [
          { provide: TemplateService, useValue: templateServiceSpy },
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: { mode: 'create', templateId: 'template-1', existingTasksCount: 0 } },
          provideAnimations(),
        ],
      }).compileComponents();

      templateService = TestBed.inject(TemplateService) as jasmine.SpyObj<TemplateService>;
      fixture = TestBed.createComponent(TaskDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be invalid when title is empty', () => {
      component.taskForm.patchValue({ title: '' });
      expect(component.taskForm.get('title')?.invalid).toBe(true);
    });

    it('should be invalid when description is empty', () => {
      component.taskForm.patchValue({ description: '' });
      expect(component.taskForm.get('description')?.invalid).toBe(true);
    });

    it('should be invalid when taskOrder is less than 1', () => {
      component.taskForm.patchValue({ taskOrder: 0 });
      expect(component.taskForm.get('taskOrder')?.invalid).toBe(true);
    });

    it('should be valid with all required fields', () => {
      component.taskForm.patchValue({
        title: 'Task',
        description: 'Description',
        taskOrder: 1,
        ownerRole: 'USER',
      });
      expect(component.taskForm.valid).toBe(true);
    });

    it('should not submit invalid form', () => {
      component.taskForm.patchValue({ title: '' });
      component.onSave();

      expect(templateService.createTemplateTask).not.toHaveBeenCalled();
      expect(component.taskForm.touched).toBe(true);
    });

    it('should return error message for required field', () => {
      const control = component.taskForm.get('title');
      control?.setValue('');
      control?.markAsTouched();

      const errorMsg = component.getErrorMessage('title');
      expect(errorMsg).toBe('To pole jest wymagane');
    });

    it('should return error message for min value', () => {
      const control = component.taskForm.get('taskOrder');
      control?.setValue(0);
      control?.markAsTouched();

      const errorMsg = component.getErrorMessage('taskOrder');
      expect(errorMsg).toContain('Wartość musi być większa lub równa');
    });

    it('should return empty string when field is valid', () => {
      component.taskForm.get('title')?.setValue('Task');
      const errorMsg = component.getErrorMessage('title');
      expect(errorMsg).toBe('');
    });
  });

  describe('ownerRoles', () => {
    let component: TaskDialogComponent;
    let fixture: ComponentFixture<TaskDialogComponent>;

    beforeEach(async () => {
      const templateServiceSpy = jasmine.createSpyObj('TemplateService', [
        'createTemplateTask',
        'updateTemplateTask',
      ]);
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      await TestBed.configureTestingModule({
        imports: [TaskDialogComponent, ReactiveFormsModule],
        providers: [
          { provide: TemplateService, useValue: templateServiceSpy },
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: { mode: 'create', templateId: 'template-1', existingTasksCount: 0 } },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TaskDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should have USER and MANAGER roles available', () => {
      expect(component.ownerRoles).toEqual([
        { value: 'USER', label: 'Użytkownik (nowy pracownik)' },
        { value: 'MANAGER', label: 'Menedżer' },
      ]);
    });
  });

  describe('onCancel', () => {
    let component: TaskDialogComponent;
    let fixture: ComponentFixture<TaskDialogComponent>;
    let dialogRef: jasmine.SpyObj<MatDialogRef<TaskDialogComponent>>;

    beforeEach(async () => {
      const templateServiceSpy = jasmine.createSpyObj('TemplateService', [
        'createTemplateTask',
        'updateTemplateTask',
      ]);
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      await TestBed.configureTestingModule({
        imports: [TaskDialogComponent, ReactiveFormsModule],
        providers: [
          { provide: TemplateService, useValue: templateServiceSpy },
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: { mode: 'create', templateId: 'template-1', existingTasksCount: 0 } },
          provideAnimations(),
        ],
      }).compileComponents();

      dialogRef = TestBed.inject(MatDialogRef) as jasmine.SpyObj<MatDialogRef<TaskDialogComponent>>;
      fixture = TestBed.createComponent(TaskDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should close dialog without data', () => {
      component.onCancel();
      expect(dialogRef.close).toHaveBeenCalledWith();
    });
  });

  describe('Component Rendering', () => {
    let component: TaskDialogComponent;
    let fixture: ComponentFixture<TaskDialogComponent>;

    beforeEach(async () => {
      const templateServiceSpy = jasmine.createSpyObj('TemplateService', [
        'createTemplateTask',
        'updateTemplateTask',
      ]);
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      await TestBed.configureTestingModule({
        imports: [TaskDialogComponent, ReactiveFormsModule],
        providers: [
          { provide: TemplateService, useValue: templateServiceSpy },
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: { mode: 'create', templateId: 'template-1', existingTasksCount: 0 } },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TaskDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should render form', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const form = compiled.querySelector('form');
      expect(form).toBeTruthy();
    });

    it('should render title input', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const input = compiled.querySelector('input[formControlName="title"]');
      expect(input).toBeTruthy();
    });

    it('should render description textarea', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const textarea = compiled.querySelector('textarea[formControlName="description"]');
      expect(textarea).toBeTruthy();
    });

    it('should render taskOrder input', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const input = compiled.querySelector('input[formControlName="taskOrder"]');
      expect(input).toBeTruthy();
    });

    it('should render ownerRole select', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const select = compiled.querySelector('mat-select[formControlName="ownerRole"]');
      expect(select).toBeTruthy();
    });
  });
});
