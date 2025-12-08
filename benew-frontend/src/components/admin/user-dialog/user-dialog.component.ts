import { Component, inject, signal, OnInit, ChangeDetectionStrategy, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { UserService } from '../../../services/user.service';
import { AuthService } from '../../../services/auth.service';
import { UserDto, CreateUserRequestDto, UpdateUserRequestDto } from '../../../models/user.dto';
import { UserRole } from '../../../models/user-role';
import { startWith, map } from 'rxjs/operators';
import { Observable } from 'rxjs';

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
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatAutocompleteModule,
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
  private readonly authService = inject(AuthService);

  readonly userForm: FormGroup;
  readonly isSaving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly isEditMode: boolean;
  readonly allUsers = signal<UserDto[]>([]);
  readonly availablePositions = signal<string[]>([]);
  filteredPositions$!: Observable<string[]>;

  readonly currentUser = this.authService.currentUser;
  readonly isManager = computed(() => this.currentUser()?.role === UserRole.MANAGER);
  readonly isAdmin = computed(() => this.currentUser()?.role === UserRole.ADMIN);

  readonly allRoles = [
    { value: UserRole.ADMIN, label: 'Administrator' },
    { value: UserRole.MANAGER, label: 'Menedżer' },
    { value: UserRole.USER, label: 'Użytkownik' },
  ];

  // Available roles based on current user's role
  readonly roles = computed(() => {
    if (this.isManager()) {
      // Managers can only create users with USER role
      return [{ value: UserRole.USER, label: 'Użytkownik' }];
    }
    return this.allRoles;
  });

  constructor() {
    this.isEditMode = this.data.mode === 'edit';

    // Determine default role based on current user
    const defaultRole = this.currentUser()?.role === UserRole.MANAGER 
      ? UserRole.USER 
      : (this.data.user?.role || UserRole.USER);

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
      role: [defaultRole, [Validators.required]], // Don't disable - handle in onSave()
      managerId: [this.data.user?.managerId || null], // Don't disable - handle in onSave()
    });
  }

  ngOnInit(): void {
    this.loadManagers();
    this.loadPositions();
    this.setupPositionAutocomplete();
  }

  private loadManagers(): void {
    // Only load managers if the current user is an admin
    // Managers don't need this as they cannot select a manager
    if (!this.isManager()) {
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
  }

  private loadPositions(): void {
    // Load all users to extract unique positions
    this.userService.getUsers(0, 1000).subscribe({
      next: (response) => {
        // Extract unique positions, normalize to lowercase, remove duplicates
        const positions = response.content
          .map((user) => user.positionName.toLowerCase().trim())
          .filter((pos, index, self) => pos && self.indexOf(pos) === index)
          .sort();
        
        this.availablePositions.set(positions);
      },
      error: (error) => {
        console.error('Error loading positions:', error);
      },
    });
  }

  private setupPositionAutocomplete(): void {
    this.filteredPositions$ = this.userForm.get('positionName')!.valueChanges.pipe(
      startWith(''),
      map((value) => this._filterPositions(value || ''))
    );
  }

  private _filterPositions(value: string): string[] {
    const filterValue = value.toLowerCase().trim();
    return this.availablePositions().filter((position) =>
      position.includes(filterValue)
    );
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
        positionName: formValue.positionName.toLowerCase().trim(),
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
      // For MANAGER, force role to USER
      const userRole = this.isManager() ? UserRole.USER : formValue.role;
      
      const createData: CreateUserRequestDto = {
        email: formValue.email,
        password: formValue.password,
        firstName: formValue.firstName,
        lastName: formValue.lastName,
        positionName: formValue.positionName.toLowerCase().trim(),
        role: userRole, // Force USER role for managers
        // Managers don't specify managerId - backend auto-assigns to themselves
        managerId: this.isManager() ? undefined : (formValue.managerId || undefined),
      };

      this.userService.createUser(createData).subscribe({
        next: (user) => {
          this.dialogRef.close(user);
        },
        error: (error) => {
          console.error('Error creating user:', error);
          this.errorMessage.set(error.error?.message || 'Błąd podczas aktualizacji użytkownika');
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

