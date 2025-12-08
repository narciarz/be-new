import { Component, signal, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { OnboardingService } from '../../../services/onboarding.service';
import { UserService } from '../../../services/user.service';
import { OnboardingProcessDto, OnboardingTaskDto } from '../../../models/onboarding.dto';
import { forkJoin } from 'rxjs';

/**
 * Process Detail component for Manager - displays detailed view of an onboarding process
 * with read-only task list showing completion status.
 */
@Component({
  selector: 'app-process-detail',
  imports: [
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatChipsModule,
    MatListModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatDividerModule,
  ],
  templateUrl: './process-detail.component.html',
  styleUrl: './process-detail.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProcessDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly onboardingService = inject(OnboardingService);
  private readonly userService = inject(UserService);

  readonly process = signal<OnboardingProcessDto | null>(null);
  readonly tasks = signal<OnboardingTaskDto[]>([]);
  readonly userName = signal<string>('');
  readonly managerName = signal<string>('');
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    const processId = this.route.snapshot.paramMap.get('id');
    if (processId) {
      this.loadProcessDetails(processId);
    } else {
      this.errorMessage.set('Brak ID procesu');
      this.isLoading.set(false);
    }
  }

  private loadProcessDetails(processId: string): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    // Load process details and tasks in parallel
    forkJoin({
      process: this.onboardingService.getOnboardingProcessById(processId),
      tasks: this.onboardingService.getOnboardingTasks(processId),
    }).subscribe({
      next: (results) => {
        this.process.set(results.process);
        this.tasks.set(results.tasks);

        // Load user and manager names
        forkJoin({
          user: this.userService.getUserById(results.process.userId),
          manager: this.userService.getUserById(results.process.managerId),
        }).subscribe({
          next: (users) => {
            this.userName.set(`${users.user.firstName} ${users.user.lastName}`);
            this.managerName.set(`${users.manager.firstName} ${users.manager.lastName}`);
            this.isLoading.set(false);
          },
          error: (error) => {
            console.error('Error loading user details:', error);
            this.isLoading.set(false);
          },
        });
      },
      error: (error) => {
        console.error('Error loading process details:', error);
        this.errorMessage.set('Błąd podczas ładowania szczegółów procesu');
        this.isLoading.set(false);
      },
    });
  }

  getProgress(): number {
    const proc = this.process();
    if (!proc || proc.totalTasksCount === 0) return 0;
    return Math.round((proc.completedTasksCount / proc.totalTasksCount) * 100);
  }

  getStatusLabel(): string {
    const status = this.process()?.status;
    return status === 'ACTIVE' ? 'Aktywny' : 'Zarchiwizowany';
  }

  getStatusColor(): string {
    const status = this.process()?.status;
    return status === 'ACTIVE' ? 'primary' : 'accent';
  }

  getCreatedDate(): string {
    return this.process()?.createdAt?.split('T')[0] || 'N/A';
  }

  getUpdatedDate(): string {
    return this.process()?.updatedAt?.split('T')[0] || 'N/A';
  }

  onBack(): void {
    this.router.navigate(['/dashboard/manager/processes']);
  }

  getTaskIcon(task: OnboardingTaskDto): string {
    return task.isCompleted ? 'check_circle' : 'radio_button_unchecked';
  }

  getTaskIconColor(task: OnboardingTaskDto): string {
    return task.isCompleted ? 'completed' : 'pending';
  }

  getRoleLabel(role: string): string {
    switch (role) {
      case 'EMPLOYEE':
        return 'Pracownik';
      case 'MANAGER':
        return 'Menedżer';
      case 'HR':
        return 'HR';
      case 'IT':
        return 'IT';
      default:
        return role;
    }
  }

  getRoleColor(role: string): string {
    switch (role) {
      case 'EMPLOYEE':
        return 'employee';
      case 'MANAGER':
        return 'manager';
      case 'HR':
        return 'hr';
      case 'IT':
        return 'it';
      default:
        return '';
    }
  }
}
