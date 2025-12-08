import { Component, signal, inject, ChangeDetectionStrategy } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TemplateService } from '../../../services/template.service';

/**
 * Template Import component for Admin role.
 * Allows importing templates from CSV files.
 */
@Component({
  selector: 'app-template-import',
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './template-import.component.html',
  styleUrl: './template-import.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TemplateImportComponent {
  private readonly templateService = inject(TemplateService);

  readonly isUploading = signal(false);
  readonly uploadResult = signal<{
    success: boolean;
    message: string;
    tasksImported?: number;
  } | null>(null);

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) {
      return;
    }

    const file = input.files[0];

    // Validate file type
    if (!file.name.endsWith('.csv')) {
      this.uploadResult.set({
        success: false,
        message: 'Nieprawidłowy format pliku. Wybierz plik CSV.',
      });
      return;
    }

    console.log('Uploading file:', file.name);
    this.isUploading.set(true);
    this.uploadResult.set(null);

    this.templateService.importTemplateFromCsv(file).subscribe({
      next: (response) => {
        console.log('Import response:', response);
        this.isUploading.set(false);
        this.uploadResult.set({
          success: true,
          message: 'Szablon został pomyślnie zaimportowany',
          tasksImported: response.tasksImported || response.tasksCount || 0,
        });
        // Clear file input
        input.value = '';
      },
      error: (error) => {
        console.error('Error importing template:', error);
        this.isUploading.set(false);
        this.uploadResult.set({
          success: false,
          message: error.error?.message || 'Błąd podczas importu szablonu',
        });
        // Clear file input
        input.value = '';
      },
    });
  }

  onDownloadTemplate(): void {
    console.log('Download template clicked');
    
    // Generate sample CSV template with correct format:
    // Header: position_name,task_order,task_title,task_description,owner_role
    // Data rows: one task per line with position name repeated
    const csvContent = `position_name,task_order,task_title,task_description,owner_role
IT DevOps,1,Workspace - laptop,Laptop configuration,USER
IT DevOps,2,Team introduction,Meet with the team and department members,MANAGER
IT DevOps,3,Review codebase,Familiarize with repositories and projects,USER
IT DevOps,4,Security training,Complete security awareness training,USER
IT DevOps,5,Tools setup,Install and configure development tools,USER`;

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', 'onboarding_template_example.csv');
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    console.log('Template downloaded successfully');
  }

  onReset(): void {
    this.uploadResult.set(null);
  }
}

