import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { TemplateService } from '../../../services/template.service';
import { TemplateTaskDto, CreateTemplateTaskRequestDto, UpdateTemplateTaskRequestDto } from '../../../models/template.dto';

interface TaskDialogData {
  templateId: string;
  task?: TemplateTaskDto;
  mode: 'create' | 'edit';
  existingTasksCount: number;
}

/**
 * Dialog for creating or editing a template task
 */
@Component({
  selector: 'app-task-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatIconModule,
  ],
  templateUrl: './task-dialog.component.html',
  styleUrl: './task-dialog.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TaskDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<TaskDialogComponent>);
  private readonly data = inject<TaskDialogData>(MAT_DIALOG_DATA);
  private readonly templateService = inject(TemplateService);

  readonly taskForm: FormGroup;
  readonly isSaving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly isEditMode: boolean;

  readonly ownerRoles = [
    { value: 'USER', label: 'Użytkownik (nowy pracownik)' },
    { value: 'MANAGER', label: 'Menedżer' },
  ];

  constructor() {
    this.isEditMode = this.data.mode === 'edit';

    this.taskForm = this.fb.group({
      title: [this.data.task?.title || '', [Validators.required]],
      description: [this.data.task?.description || '', [Validators.required]],
      taskOrder: [
        this.data.task?.taskOrder || this.data.existingTasksCount + 1,
        [Validators.required, Validators.min(1)],
      ],
      ownerRole: [this.data.task?.ownerRole || 'USER', [Validators.required]],
    });
  }

  onSave(): void {
    if (this.taskForm.invalid) {
      this.taskForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.errorMessage.set(null);

    const formValue = this.taskForm.value;

    if (this.isEditMode && this.data.task) {
      // Edit existing task
      const updateData: UpdateTemplateTaskRequestDto = {
        title: formValue.title,
        description: formValue.description,
        taskOrder: formValue.taskOrder,
        ownerRole: formValue.ownerRole,
      };

      this.templateService
        .updateTemplateTask(this.data.templateId, this.data.task.id, updateData)
        .subscribe({
          next: (task) => {
            this.dialogRef.close(task);
          },
          error: (error) => {
            console.error('Error updating task:', error);
            this.errorMessage.set(error.error?.message || 'Błąd podczas aktualizacji zadania');
            this.isSaving.set(false);
          },
        });
    } else {
      // Create new task
      const createData: CreateTemplateTaskRequestDto = {
        title: formValue.title,
        description: formValue.description,
        taskOrder: formValue.taskOrder,
        ownerRole: formValue.ownerRole,
      };

      this.templateService.createTemplateTask(this.data.templateId, createData).subscribe({
        next: (task) => {
          this.dialogRef.close(task);
        },
        error: (error) => {
          console.error('Error creating task:', error);
          this.errorMessage.set(error.error?.message || 'Błąd podczas tworzenia zadania');
          this.isSaving.set(false);
        },
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  getErrorMessage(fieldName: string): string {
    const control = this.taskForm.get(fieldName);
    if (!control || !control.errors || !control.touched) {
      return '';
    }

    if (control.errors['required']) {
      return 'To pole jest wymagane';
    }
    if (control.errors['min']) {
      return `Wartość musi być większa lub równa ${control.errors['min'].min}`;
    }
    return 'Nieprawidłowa wartość';
  }
}
