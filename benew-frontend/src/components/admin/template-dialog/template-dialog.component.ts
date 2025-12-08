import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { TemplateService } from '../../../services/template.service';
import { TemplateDto, CreateTemplateRequestDto, UpdateTemplateRequestDto } from '../../../models/template.dto';

interface TemplateDialogData {
  template?: TemplateDto;
  mode: 'create' | 'edit';
}

/**
 * Dialog for creating or editing a template
 */
@Component({
  selector: 'app-template-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule,
  ],
  templateUrl: './template-dialog.component.html',
  styleUrl: './template-dialog.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TemplateDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<TemplateDialogComponent>);
  private readonly data = inject<TemplateDialogData>(MAT_DIALOG_DATA);
  private readonly templateService = inject(TemplateService);

  readonly templateForm: FormGroup;
  readonly isSaving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly isEditMode: boolean;

  constructor() {
    this.isEditMode = this.data.mode === 'edit';

    this.templateForm = this.fb.group({
      positionName: [this.data.template?.positionName || '', [Validators.required]],
      description: [this.data.template?.description || ''],
    });
  }

  onSave(): void {
    if (this.templateForm.invalid) {
      this.templateForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.errorMessage.set(null);

    const formValue = this.templateForm.value;

    if (this.isEditMode && this.data.template) {
      // Edit existing template
      const updateData: UpdateTemplateRequestDto = {
        positionName: formValue.positionName,
        description: formValue.description || undefined,
      };

      this.templateService.updateTemplate(this.data.template.id, updateData).subscribe({
        next: (template) => {
          this.dialogRef.close(template);
        },
        error: (error) => {
          console.error('Error updating template:', error);
          this.errorMessage.set(error.error?.message || 'Błąd podczas aktualizacji szablonu');
          this.isSaving.set(false);
        },
      });
    } else {
      // Create new template
      const createData: CreateTemplateRequestDto = {
        positionName: formValue.positionName,
        description: formValue.description || undefined,
      };

      this.templateService.createTemplate(createData).subscribe({
        next: (template) => {
          this.dialogRef.close(template);
        },
        error: (error) => {
          console.error('Error creating template:', error);
          this.errorMessage.set(error.error?.message || 'Błąd podczas tworzenia szablonu');
          this.isSaving.set(false);
        },
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  getErrorMessage(fieldName: string): string {
    const control = this.templateForm.get(fieldName);
    if (!control || !control.errors || !control.touched) {
      return '';
    }

    if (control.errors['required']) {
      return 'To pole jest wymagane';
    }
    return 'Nieprawidłowa wartość';
  }
}
