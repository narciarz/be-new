import { Component, signal, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { OnboardingService } from '../../../services/onboarding.service';
import { UserService } from '../../../services/user.service';
import { AuthService } from '../../../services/auth.service';
import { OnboardingProcessDto } from '../../../models/onboarding.dto';
import { forkJoin } from 'rxjs';

interface ProcessView {
  processId: string;
  userName: string;
  position: string;
  startDate: string;
  progress: number;
  totalTasks: number;
  completedTasks: number;
  lastActivity?: string;
  completedDate?: string;
  duration?: string;
}

/**
 * Processes component for Manager - displays onboarding processes.
 */
@Component({
  selector: 'app-processes',
  imports: [
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatChipsModule,
    MatTabsModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './processes.component.html',
  styleUrl: './processes.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProcessesComponent implements OnInit {
  private readonly onboardingService = inject(OnboardingService);
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);

  readonly activeProcesses = signal<ProcessView[]>([]);
  readonly completedProcesses = signal<ProcessView[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadProcesses();
  }

  private loadProcesses(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    const currentUser = this.authService.currentUser();
    if (!currentUser) {
      this.errorMessage.set('Brak danych użytkownika');
      this.isLoading.set(false);
      return;
    }

    // Load both active and completed processes
    forkJoin({
      active: this.onboardingService.getOnboardingProcesses(0, 100, {
        managerId: currentUser.id,
        status: 'ACTIVE',
      }),
      completed: this.onboardingService.getOnboardingProcesses(0, 100, {
        managerId: currentUser.id,
        status: 'ARCHIVED',
      }),
    }).subscribe({
      next: (results) => {
        this.processActiveProcesses(results.active.content);
        this.processCompletedProcesses(results.completed.content);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading processes:', error);
        this.errorMessage.set('Błąd podczas ładowania procesów');
        this.isLoading.set(false);
      },
    });
  }

  private processActiveProcesses(processes: OnboardingProcessDto[]): void {
    if (processes.length === 0) {
      this.activeProcesses.set([]);
      return;
    }

    const userRequests = processes.map((p) => this.userService.getUserById(p.userId));

    forkJoin(userRequests).subscribe({
      next: (users) => {
        const views: ProcessView[] = processes.map((process, index) => {
          const user = users[index];
          const progress = Math.round(
            (process.completedTasksCount / process.totalTasksCount) * 100
          );

          return {
            processId: process.id,
            userName: `${user.firstName} ${user.lastName}`,
            position: user.positionName,
            startDate: process.createdAt?.split('T')[0] || 'N/A',
            progress,
            totalTasks: process.totalTasksCount,
            completedTasks: process.completedTasksCount,
            lastActivity: this.calculateTimeAgo(process.updatedAt),
          };
        });

        this.activeProcesses.set(views);
      },
      error: (error) => {
        console.error('Error loading user details:', error);
      },
    });
  }

  private processCompletedProcesses(processes: OnboardingProcessDto[]): void {
    if (processes.length === 0) {
      this.completedProcesses.set([]);
      return;
    }

    const userRequests = processes.map((p) => this.userService.getUserById(p.userId));

    forkJoin(userRequests).subscribe({
      next: (users) => {
        const views: ProcessView[] = processes.map((process, index) => {
          const user = users[index];
          const duration = this.calculateDuration(process.createdAt, process.updatedAt);

          return {
            processId: process.id,
            userName: `${user.firstName} ${user.lastName}`,
            position: user.positionName,
            startDate: process.createdAt?.split('T')[0] || 'N/A',
            progress: 100,
            totalTasks: process.totalTasksCount,
            completedTasks: process.completedTasksCount,
            completedDate: process.updatedAt?.split('T')[0] || 'N/A',
            duration,
          };
        });

        this.completedProcesses.set(views);
      },
      error: (error) => {
        console.error('Error loading user details:', error);
      },
    });
  }

  private calculateTimeAgo(dateString?: string): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 60) return `${diffMins} minut temu`;
    if (diffHours < 24) return `${diffHours} godzin temu`;
    return `${diffDays} dni temu`;
  }

  private calculateDuration(startDate?: string, endDate?: string): string {
    if (!startDate || !endDate) return 'N/A';
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diffMs = end.getTime() - start.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) return 'mniej niż 1 dzień';
    if (diffDays === 1) return '1 dzień';
    if (diffDays < 5) return `${diffDays} dni`;
    return `${diffDays} dni`;
  }

  onViewProcess(processId: string): void {
    console.log('View process:', processId);
    // TODO: Navigate to process details
  }

  onArchiveProcess(processId: string): void {
    this.onboardingService
      .updateOnboardingProcess(processId, { status: 'ARCHIVED' })
      .subscribe({
        next: () => {
          // Remove from active processes
          const updated = this.activeProcesses().filter((p) => p.processId !== processId);
          this.activeProcesses.set(updated);
          console.log('Process archived successfully');
        },
        error: (error) => {
          console.error('Error archiving process:', error);
          this.errorMessage.set('Błąd podczas archiwizacji procesu');
        },
      });
  }

  onTabChange(index: number): void {
    // Reload data when switching to "Zarchiwizowane" tab (index 1)
    if (index === 1) {
      this.loadArchivedProcesses();
    }
  }

  private loadArchivedProcesses(): void {
    const currentUser = this.authService.currentUser();
    if (!currentUser) {
      return;
    }

    this.onboardingService
      .getOnboardingProcesses(0, 100, {
        managerId: currentUser.id,
        status: 'ARCHIVED',
      })
      .subscribe({
        next: (result) => {
          this.processCompletedProcesses(result.content);
        },
        error: (error) => {
          console.error('Error loading archived processes:', error);
        },
      });
  }
}

