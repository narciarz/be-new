import { Component, signal, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog } from '@angular/material/dialog';
import { TemplateService } from '../../../services/template.service';
import { TemplateDto, TemplateTaskDto } from '../../../models/template.dto';
import { TemplateDialogComponent } from '../template-dialog/template-dialog.component';
import { TaskDialogComponent } from '../task-dialog/task-dialog.component';
import { forkJoin } from 'rxjs';

interface TemplateWithTasks extends TemplateDto {
  tasks: TemplateTaskDto[];
  tasksCount: number;
}

/**
 * Templates component for Admin role.
 * Manages onboarding checklist templates.
 */
@Component({
  selector: 'app-templates',
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatChipsModule,
    MatExpansionModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './templates.component.html',
  styleUrl: './templates.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TemplatesComponent implements OnInit {
  private readonly templateService = inject(TemplateService);
  private readonly dialog = inject(MatDialog);

  readonly templates = signal<TemplateWithTasks[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadTemplates();
  }

  private loadTemplates(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.templateService.getTemplates(0, 100).subscribe({
      next: (response) => {
        const templates = response.content;

        if (templates.length === 0) {
          this.templates.set([]);
          this.isLoading.set(false);
          return;
        }

        // Load tasks for each template
        const taskRequests = templates.map((t) =>
          this.templateService.getTemplateTasks(t.templateId)
        );

        forkJoin(taskRequests).subscribe({
          next: (tasksArrays) => {
            const templatesWithTasks: TemplateWithTasks[] = templates.map((template, index) => {
              const tasks = tasksArrays[index];
              return {
                ...template,
                tasks,
                tasksCount: tasks.length,
              };
            });

            this.templates.set(templatesWithTasks);
            this.isLoading.set(false);
          },
          error: (error) => {
            console.error('Error loading template tasks:', error);
            this.errorMessage.set('Błąd podczas ładowania zadań szablonów');
            this.isLoading.set(false);
          },
        });
      },
      error: (error) => {
        console.error('Error loading templates:', error);
        this.errorMessage.set('Błąd podczas ładowania szablonów');
        this.isLoading.set(false);
      },
    });
  }

  onAddTemplate(): void {
    const dialogRef = this.dialog.open(TemplateDialogComponent, {
      width: '600px',
      data: { mode: 'create' },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // Add new template to the list (with empty tasks array)
        const newTemplate: TemplateWithTasks = {
          ...result,
          tasks: [],
          tasksCount: 0,
        };
        this.templates.update((templates) => [...templates, newTemplate]);
      }
    });
  }

  onEditTemplate(templateId: string): void {
    const template = this.templates().find((t) => t.templateId === templateId);
    if (!template) return;

    const dialogRef = this.dialog.open(TemplateDialogComponent, {
      width: '600px',
      data: { mode: 'edit', template },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // Update template in the list (preserve tasks)
        this.templates.update((templates) =>
          templates.map((t) =>
            t.templateId === templateId ? { ...result, tasks: t.tasks, tasksCount: t.tasksCount } : t
          )
        );
      }
    });
  }

  onDeleteTemplate(templateId: string): void {
    console.log('Delete template:', templateId);
    if (!confirm('Czy na pewno chcesz usunąć ten szablon?')) {
      return;
    }

    this.templateService.deleteTemplate(templateId).subscribe({
      next: () => {
        const updated = this.templates().filter((t) => t.templateId !== templateId);
        this.templates.set(updated);
        console.log('Template deleted successfully');
      },
      error: (error) => {
        console.error('Error deleting template:', error);
        this.errorMessage.set('Błąd podczas usuwania szablonu');
      },
    });
  }

  onAddTask(templateId: string): void {
    const template = this.templates().find((t) => t.templateId === templateId);
    if (!template) return;

    const dialogRef = this.dialog.open(TaskDialogComponent, {
      width: '600px',
      data: {
        mode: 'create',
        templateId,
        existingTasksCount: template.tasks.length,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // Add new task to the template
        this.templates.update((templates) =>
          templates.map((t) => {
            if (t.templateId === templateId) {
              return {
                ...t,
                tasks: [...t.tasks, result],
                tasksCount: t.tasks.length + 1,
              };
            }
            return t;
          })
        );
      }
    });
  }

  onEditTask(templateId: string, taskId: string): void {
    const template = this.templates().find((t) => t.templateId === templateId);
    if (!template) return;

    const task = template.tasks.find((t) => t.taskId === taskId);
    if (!task) return;

    const dialogRef = this.dialog.open(TaskDialogComponent, {
      width: '600px',
      data: {
        mode: 'edit',
        templateId,
        task,
        existingTasksCount: template.tasks.length,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // Update task in the template
        this.templates.update((templates) =>
          templates.map((t) => {
            if (t.templateId === templateId) {
              return {
                ...t,
                tasks: t.tasks.map((task) => (task.taskId === taskId ? result : task)),
              };
            }
            return t;
          })
        );
      }
    });
  }

  onDeleteTask(templateId: string, taskId: string): void {
    console.log('Delete task:', taskId, 'from template:', templateId);
    if (!confirm('Czy na pewno chcesz usunąć to zadanie?')) {
      return;
    }

    this.templateService.deleteTemplateTask(templateId, taskId).subscribe({
      next: () => {
        // Update local state
        const updated = this.templates().map((template) => {
          if (template.templateId === templateId) {
            const updatedTasks = template.tasks.filter((t) => t.taskId !== taskId);
            return {
              ...template,
              tasks: updatedTasks,
              tasksCount: updatedTasks.length,
            };
          }
          return template;
        });
        this.templates.set(updated);
        console.log('Task deleted successfully');
      },
      error: (error) => {
        console.error('Error deleting task:', error);
        this.errorMessage.set('Błąd podczas usuwania zadania');
      },
    });
  }
}
