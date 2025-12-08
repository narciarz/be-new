import { Component, inject, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { UserService } from '../../../services/user.service';
import { UserDto, CreateUserRequestDto, UpdateUserRequestDto } from '../../../models/user.dto';
import { UserRole } from '../../../models/user-role';

interface UserDialogData {
  user?: UserDto;
  mode: 'create' | 'edit';
}

/**
 * Dialog for creating or editing a user
 */
@Component({
  selector: 'app-user-dialog',
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
  templateUrl: './user-dialog.component.html',
  styleUrl: './user-dialog.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<UserDialogComponent>);
  private readonly data = inject<UserDialogData>(MAT_DIALOG_DATA);
  private readonly userService = inject(UserService);

  readonly userForm: FormGroup;
  readonly isSaving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly isEditMode: boolean;
  readonly allUsers = signal<UserDto[]>([]);

  readonly roles = [
    { value: UserRole.ADMIN, label: 'Administrator' },
    { value: UserRole.MANAGER, label: 'Menedżer' },
    { value: UserRole.USER, label: 'Użytkownik' },
  ];

  constructor() {
    this.isEditMode = this.data.mode === 'edit';

    this.userForm = this.fb.group({
      email: [
        this.data.user?.email || '',
        [Validators.required, Validators.email],
      ],
      password: [
        '',
        this.isEditMode ? [] : [Validators.required, Validators.minLength(8)],
      ],
      firstName: [this.data.user?.firstName || '', [Validators.required]],
      lastName: [this.data.user?.lastName || '', [Validators.required]],
      positionName: [this.data.user?.positionName || '', [Validators.required]],
      role: [this.data.user?.role || UserRole.USER, [Validators.required]],
      managerId: [this.data.user?.managerId || null],
    });
  }

  ngOnInit(): void {
    this.loadManagers();
  }

  private loadManagers(): void {
    // Load users with MANAGER role for manager selection
    this.userService.getUsers(0, 100, undefined, { role: UserRole.MANAGER }).subscribe({
      next: (response) => {
        this.allUsers.set(response.content);
      },
      error: (error) => {
        console.error('Error loading managers:', error);
      },
    });
  }

  onSave(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.errorMessage.set(null);

    const formValue = this.userForm.value;

    if (this.isEditMode && this.data.user) {
      // Edit existing user
      const updateData: UpdateUserRequestDto = {
        email: formValue.email,
        firstName: formValue.firstName,
        lastName: formValue.lastName,
        positionName: formValue.positionName,
        role: formValue.role,
        managerId: formValue.managerId || undefined,
      };

      // Only include password if it was changed
      if (formValue.password) {
        updateData.password = formValue.password;
      }

      this.userService.updateUser(this.data.user.id, updateData).subscribe({
        next: (user) => {
          this.dialogRef.close(user);
        },
        error: (error) => {
          console.error('Error updating user:', error);
          this.errorMessage.set(error.error?.message || 'Błąd podczas aktualizacji użytkownika');
          this.isSaving.set(false);
        },
      });
    } else {
      // Create new user
      const createData: CreateUserRequestDto = {
        email: formValue.email,
        password: formValue.password,
        firstName: formValue.firstName,
        lastName: formValue.lastName,
        positionName: formValue.positionName,
        role: formValue.role,
        managerId: formValue.managerId || undefined,
      };

      this.userService.createUser(createData).subscribe({
        next: (user) => {
          this.dialogRef.close(user);
        },
        error: (error) => {
          console.error('Error creating user:', error);
          this.errorMessage.set(error.error?.message || 'Błąd podczas tworzenia użytkownika');
          this.isSaving.set(false);
        },
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  getErrorMessage(fieldName: string): string {
    const control = this.userForm.get(fieldName);
    if (!control || !control.errors || !control.touched) {
      return '';
    }

    if (control.errors['required']) {
      return 'To pole jest wymagane';
    }
    if (control.errors['email']) {
      return 'Nieprawidłowy adres email';
    }
    if (control.errors['minlength']) {
      return `Minimum ${control.errors['minlength'].requiredLength} znaków`;
    }
    return 'Nieprawidłowa wartość';
  }
}
