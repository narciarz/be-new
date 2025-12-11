import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { TemplatesComponent } from './templates.component';
import { TemplateService } from '../../../services/template.service';
import { TemplateDto, TemplateTaskDto } from '../../../models/template.dto';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('TemplatesComponent', () => {
  let component: TemplatesComponent;
  let fixture: ComponentFixture<TemplatesComponent>;
  let templateService: jasmine.SpyObj<TemplateService>;
  let dialog: jasmine.SpyObj<MatDialog>;

  const mockTemplates: TemplateDto[] = [
    {
      id: 'template-1',
      positionName: 'Developer',
      description: 'Developer onboarding',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-02T00:00:00Z',
    },
    {
      id: 'template-2',
      positionName: 'Manager',
      description: 'Manager onboarding',
      createdAt: '2024-01-03T00:00:00Z',
      updatedAt: '2024-01-04T00:00:00Z',
    },
  ];

  const mockTasks: TemplateTaskDto[] = [
    {
      id: 'task-1',
      templateId: 'template-1',
      title: 'Task 1',
      description: 'Description 1',
      taskOrder: 1,
      ownerRole: 'USER',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-02T00:00:00Z',
    },
    {
      id: 'task-2',
      templateId: 'template-1',
      title: 'Task 2',
      description: 'Description 2',
      taskOrder: 2,
      ownerRole: 'USER',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-02T00:00:00Z',
    },
  ];

  beforeEach(async () => {
    const templateServiceSpy = jasmine.createSpyObj('TemplateService', [
      'getTemplates',
      'getTemplateTasks',
      'createTemplate',
      'updateTemplate',
      'deleteTemplate',
      'deleteTemplateTask',
    ]);
    const dialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

    await TestBed.configureTestingModule({
      imports: [TemplatesComponent],
      providers: [
        { provide: TemplateService, useValue: templateServiceSpy },
        { provide: MatDialog, useValue: dialogSpy },
        provideAnimations(),
      ],
    }).compileComponents();

    templateService = TestBed.inject(TemplateService) as jasmine.SpyObj<TemplateService>;
    dialog = TestBed.inject(MatDialog) as jasmine.SpyObj<MatDialog>;
  });

  beforeEach(() => {
    templateService.getTemplates.and.returnValue(
      of({
        content: mockTemplates,
        totalElements: 2,
        totalPages: 1,
        size: 100,
        number: 0,
      })
    );
    templateService.getTemplateTasks.and.returnValue(of(mockTasks));

    fixture = TestBed.createComponent(TemplatesComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with loading state', () => {
    expect(component.isLoading()).toBe(true);
  });

  it('should load templates on init', () => {
    fixture.detectChanges();

    expect(templateService.getTemplates).toHaveBeenCalledWith(0, 100);
    expect(component.isLoading()).toBe(false);
  });

  it('should load tasks for each template', () => {
    fixture.detectChanges();

    expect(templateService.getTemplateTasks).toHaveBeenCalledTimes(2);
    expect(templateService.getTemplateTasks).toHaveBeenCalledWith('template-1');
    expect(templateService.getTemplateTasks).toHaveBeenCalledWith('template-2');
  });

  it('should set templates with tasks after load', () => {
    fixture.detectChanges();

    const templates = component.templates();
    expect(templates.length).toBe(2);
    expect(templates[0].tasks).toEqual(mockTasks);
    expect(templates[0].tasksCount).toBe(2);
  });

  describe('Error Handling', () => {
    beforeEach(() => {
      spyOn(console, 'error'); // Suppress expected error logs
    });

    it('should handle error when loading templates', () => {
      templateService.getTemplates.and.returnValue(
        throwError(() => new Error('Failed to load'))
      );

      fixture.detectChanges();

      expect(component.errorMessage()).toBe('Błąd podczas ładowania szablonów');
      expect(component.isLoading()).toBe(false);
    });

    it('should handle error when loading template tasks', () => {
      templateService.getTemplateTasks.and.returnValue(
        throwError(() => new Error('Failed to load tasks'))
      );

      fixture.detectChanges();

      expect(component.errorMessage()).toBe('Błąd podczas ładowania zadań szablonów');
      expect(component.isLoading()).toBe(false);
    });

    it('should handle empty templates list', () => {
      templateService.getTemplates.and.returnValue(
        of({
          content: [],
          totalElements: 0,
          totalPages: 0,
          size: 100,
          number: 0,
        })
      );

      fixture.detectChanges();

      expect(component.templates()).toEqual([]);
      expect(component.isLoading()).toBe(false);
    });
  });

  describe('onAddTemplate', () => {
    it('should open dialog for creating template', () => {
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(null));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onAddTemplate();

      expect(dialog.open).toHaveBeenCalled();
    });

    it('should add new template to list when dialog returns result', () => {
      fixture.detectChanges();

      const newTemplate: TemplateDto = {
        id: 'template-3',
        positionName: 'Designer',
        description: 'Designer onboarding',
        createdAt: '2024-01-05T00:00:00Z',
        updatedAt: '2024-01-06T00:00:00Z',
      };

      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(newTemplate));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onAddTemplate();

      expect(component.templates().length).toBe(3);
      expect(component.templates()[2].id).toBe('template-3');
    });

    it('should not add template when dialog is cancelled', () => {
      fixture.detectChanges();
      const initialLength = component.templates().length;

      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(null));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onAddTemplate();

      expect(component.templates().length).toBe(initialLength);
    });
  });

  describe('onEditTemplate', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should open dialog for editing template', () => {
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(null));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onEditTemplate('template-1');

      expect(dialog.open).toHaveBeenCalled();
    });

    it('should update template in list when dialog returns result', () => {
      const updatedTemplate: TemplateDto = {
        ...mockTemplates[0],
        positionName: 'Senior Developer',
      };

      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(updatedTemplate));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onEditTemplate('template-1');

      const template = component.templates().find((t) => t.id === 'template-1');
      expect(template?.positionName).toBe('Senior Developer');
    });

    it('should return early if template not found', () => {
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(null));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onEditTemplate('non-existent');

      expect(dialog.open).not.toHaveBeenCalled();
    });
  });

  describe('onDeleteTemplate', () => {
    beforeEach(() => {
      fixture.detectChanges();
      spyOn(window, 'confirm');
    });

    it('should confirm before deleting', () => {
      (window.confirm as jasmine.Spy).and.returnValue(false);

      component.onDeleteTemplate('template-1');

      expect(window.confirm).toHaveBeenCalled();
      expect(templateService.deleteTemplate).not.toHaveBeenCalled();
    });

    it('should delete template when confirmed', () => {
      (window.confirm as jasmine.Spy).and.returnValue(true);
      templateService.deleteTemplate.and.returnValue(of(void 0));

      component.onDeleteTemplate('template-1');

      expect(templateService.deleteTemplate).toHaveBeenCalledWith('template-1');
    });

    it('should remove template from list after successful deletion', () => {
      (window.confirm as jasmine.Spy).and.returnValue(true);
      templateService.deleteTemplate.and.returnValue(of(void 0));

      component.onDeleteTemplate('template-1');

      const template = component.templates().find((t) => t.id === 'template-1');
      expect(template).toBeUndefined();
      expect(component.templates().length).toBe(1);
    });

    it('should handle error when deleting template', () => {
      spyOn(console, 'error'); // Suppress expected error logs

      (window.confirm as jasmine.Spy).and.returnValue(true);
      templateService.deleteTemplate.and.returnValue(
        throwError(() => new Error('Failed to delete'))
      );

      component.onDeleteTemplate('template-1');

      expect(component.errorMessage()).toBe('Błąd podczas usuwania szablonu');
    });
  });

  describe('onAddTask', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should open dialog for creating task', () => {
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(null));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onAddTask('template-1');

      expect(dialog.open).toHaveBeenCalled();
    });

    it('should add new task to template when dialog returns result', () => {
      const newTask: TemplateTaskDto = {
        id: 'task-3',
        templateId: 'template-1',
        title: 'Task 3',
        description: 'Description 3',
        taskOrder: 3,
        ownerRole: 'USER',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-02T00:00:00Z',
      };

      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(newTask));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onAddTask('template-1');

      const template = component.templates().find((t) => t.id === 'template-1');
      expect(template?.tasks.length).toBe(3);
      expect(template?.tasksCount).toBe(3);
    });
  });

  describe('onEditTask', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should open dialog for editing task', () => {
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(null));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onEditTask('template-1', 'task-1');

      expect(dialog.open).toHaveBeenCalled();
    });

    it('should update task in template when dialog returns result', () => {
      const updatedTask: TemplateTaskDto = {
        ...mockTasks[0],
        title: 'Updated Task 1',
      };

      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(updatedTask));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onEditTask('template-1', 'task-1');

      const template = component.templates().find((t) => t.id === 'template-1');
      const task = template?.tasks.find((t) => t.id === 'task-1');
      expect(task?.title).toBe('Updated Task 1');
    });
  });

  describe('onDeleteTask', () => {
    beforeEach(() => {
      fixture.detectChanges();
      spyOn(window, 'confirm');
    });

    it('should confirm before deleting task', () => {
      (window.confirm as jasmine.Spy).and.returnValue(false);

      component.onDeleteTask('template-1', 'task-1');

      expect(window.confirm).toHaveBeenCalled();
      expect(templateService.deleteTemplateTask).not.toHaveBeenCalled();
    });

    it('should delete task when confirmed', () => {
      (window.confirm as jasmine.Spy).and.returnValue(true);
      templateService.deleteTemplateTask.and.returnValue(of(void 0));

      component.onDeleteTask('template-1', 'task-1');

      expect(templateService.deleteTemplateTask).toHaveBeenCalledWith('template-1', 'task-1');
    });

    it('should remove task from template after successful deletion', () => {
      (window.confirm as jasmine.Spy).and.returnValue(true);
      templateService.deleteTemplateTask.and.returnValue(of(void 0));

      component.onDeleteTask('template-1', 'task-1');

      const template = component.templates().find((t) => t.id === 'template-1');
      const task = template?.tasks.find((t) => t.id === 'task-1');
      expect(task).toBeUndefined();
      expect(template?.tasks.length).toBe(1);
      expect(template?.tasksCount).toBe(1);
    });

    it('should handle error when deleting task', () => {
      spyOn(console, 'error'); // Suppress expected error logs

      (window.confirm as jasmine.Spy).and.returnValue(true);
      templateService.deleteTemplateTask.and.returnValue(
        throwError(() => new Error('Failed to delete'))
      );

      component.onDeleteTask('template-1', 'task-1');

      expect(component.errorMessage()).toBe('Błąd podczas usuwania zadania');
    });
  });

  describe('Component Rendering', () => {
    it('should render mat-card', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const card = compiled.querySelector('mat-card');
      expect(card).toBeTruthy();
    });
  });
});
