import { Component, signal, inject, OnInit, ChangeDetectionStrategy, computed } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog } from '@angular/material/dialog';
import { UserService } from '../../../services/user.service';
import { UserDto } from '../../../models/user.dto';
import { UserDialogComponent } from '../user-dialog/user-dialog.component';
import { ConfirmationDialogComponent } from '../../shared/confirmation-dialog/confirmation-dialog.component';

/**
 * User Management component for Admin role.
 * Allows CRUD operations on users.
 */
@Component({
  selector: 'app-user-management',
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserManagementComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);

  readonly displayedColumns = ['email', 'firstName', 'lastName', 'role', 'actions'];
  readonly allUsers = signal<UserDto[]>([]);
  readonly searchQuery = signal('');
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  // Filtered users based on search query
  readonly users = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) {
      return this.allUsers();
    }

    return this.allUsers().filter((user) => {
      const searchText = `${user.email} ${user.firstName} ${user.lastName}`.toLowerCase();
      return searchText.includes(query);
    });
  });

  ngOnInit(): void {
    this.loadUsers();
  }

  onSearchChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchQuery.set(input.value);
  }

  private loadUsers(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.userService.getUsers(0, 100).subscribe({
      next: (response) => {
        this.allUsers.set(response.content);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.errorMessage.set('Błąd podczas ładowania użytkowników');
        this.isLoading.set(false);
      },
    });
  }

  onAddUser(): void {
    const dialogRef = this.dialog.open(UserDialogComponent, {
      width: '600px',
      data: { mode: 'create' },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // Add new user to the list
        this.allUsers.update((users) => [...users, result]);
      }
    });
  }

  onEditUser(userId: string): void {
    const user = this.users().find((u) => u.id === userId);
    if (!user) return;

    const dialogRef = this.dialog.open(UserDialogComponent, {
      width: '600px',
      data: { mode: 'edit', user },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // Update user in the list
        this.allUsers.update((users) => users.map((u) => (u.id === userId ? result : u)));
      }
    });
  }

  onDeleteUser(userId: string): void {
    const user = this.users().find((u) => u.id === userId);
    if (!user) return;

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '500px',
      data: {
        title: 'Potwierdź usunięcie użytkownika',
        message: `Czy na pewno chcesz usunąć użytkownika <strong>${user.firstName} ${user.lastName}</strong>?\n\n` +
          `<strong>UWAGA:</strong> Usunięcie użytkownika spowoduje również usunięcie wszystkich jego procesów onboardingowych i zadań. Ta operacja jest nieodwracalna.`,
        confirmText: 'Usuń',
        cancelText: 'Anuluj',
        danger: true,
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (!confirmed) {
        return;
      }

      this.userService.deleteUser(userId).subscribe({
        next: () => {
          // Remove from local state
          this.allUsers.update((users) => users.filter((u) => u.id !== userId));
          console.log('User deleted successfully');
        },
        error: (error) => {
          console.error('Error deleting user:', error);
          const errorMsg = error.error?.message || 'Błąd podczas usuwania użytkownika';
          this.errorMessage.set(errorMsg);
        },
      });
    });
  }

  onResetPassword(userId: string): void {
    console.log('Reset password:', userId);
    // TODO: Implement password reset API call
    alert('Funkcja resetowania hasła będzie dostępna wkrótce');
  }
}

