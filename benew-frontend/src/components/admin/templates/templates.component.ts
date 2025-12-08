import { Component, signal, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TemplateService } from '../../../services/template.service';
import { TemplateDto, TemplateTaskDto } from '../../../models/template.dto';
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
    console.log('Add template clicked');
    // TODO: Open dialog to add template
  }

  onEditTemplate(templateId: string): void {
    console.log('Edit template:', templateId);
    // TODO: Open dialog to edit template
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
    console.log('Add task to template:', templateId);
    // TODO: Open dialog to add task
  }

  onEditTask(templateId: string, taskId: string): void {
    console.log('Edit task:', taskId, 'in template:', templateId);
    // TODO: Open dialog to edit task
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
