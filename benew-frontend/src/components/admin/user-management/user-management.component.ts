import { Component, signal, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
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
  readonly users = signal<UserDto[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadUsers();
  }

  private loadUsers(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.userService.getUsers(0, 100).subscribe({
      next: (response) => {
        this.users.set(response.content);
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
        this.users.update((users) => [...users, result]);
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
        this.users.update((users) => users.map((u) => (u.id === userId ? result : u)));
      }
    });
  }

  onDeleteUser(userId: string): void {
    console.log('Delete user:', userId);
    if (!confirm('Czy na pewno chcesz usunąć tego użytkownika?')) {
      return;
    }

    this.userService.deleteUser(userId).subscribe({
      next: () => {
        // Remove from local state
        const updated = this.users().filter((u) => u.id !== userId);
        this.users.set(updated);
        console.log('User deleted successfully');
      },
      error: (error) => {
        console.error('Error deleting user:', error);
        this.errorMessage.set('Błąd podczas usuwania użytkownika');
      },
    });
  }

  onResetPassword(userId: string): void {
    console.log('Reset password:', userId);
    // TODO: Implement password reset API call
    alert('Funkcja resetowania hasła będzie dostępna wkrótce');
  }
}

