import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmationDialogData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  danger?: boolean;
}

/**
 * Reusable confirmation dialog component using Angular Material
 */
@Component({
  selector: 'app-confirmation-dialog',
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
  ],
  template: `
    <h2 mat-dialog-title>
      @if (data.danger) {
        <mat-icon class="warning-icon">warning</mat-icon>
      }
      {{ data.title }}
    </h2>

    <mat-dialog-content>
      <p [innerHTML]="data.message"></p>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">
        {{ data.cancelText || 'Anuluj' }}
      </button>
      <button 
        mat-raised-button 
        [color]="data.danger ? 'warn' : 'primary'"
        (click)="onConfirm()">
        {{ data.confirmText || 'Potwierdź' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .warning-icon {
      color: #f44336;
      vertical-align: middle;
      margin-right: 8px;
    }

    mat-dialog-content {
      min-width: 300px;
      max-width: 500px;
    }

    mat-dialog-content p {
      margin: 0;
      line-height: 1.6;
      white-space: pre-line;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfirmationDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<ConfirmationDialogComponent>);
  readonly data = inject<ConfirmationDialogData>(MAT_DIALOG_DATA);

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
