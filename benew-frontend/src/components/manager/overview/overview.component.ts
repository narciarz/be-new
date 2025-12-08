import { Component, signal, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UserService } from '../../../services/user.service';
import { OnboardingService } from '../../../services/onboarding.service';
import { AuthService } from '../../../services/auth.service';
import { forkJoin } from 'rxjs';

interface TeamMemberProgress {
  name: string;
  position: string;
  progress: number;
  tasksLeft: number;
}

/**
 * Manager Overview component - dashboard with team statistics.
 */
@Component({
  selector: 'app-overview',
  imports: [
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    MatChipsModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OverviewComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly onboardingService = inject(OnboardingService);
  private readonly authService = inject(AuthService);

  readonly stats = signal({
    totalTeamMembers: 0,
    activeOnboarding: 0,
    completedOnboarding: 0,
    averageProgress: 0,
  });

  readonly teamProgress = signal<TeamMemberProgress[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadDashboardData();
  }

  private loadDashboardData(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    const currentUser = this.authService.currentUser();
    if (!currentUser) {
      this.errorMessage.set('Brak danych użytkownika');
      this.isLoading.set(false);
      return;
    }

    // Load team members and onboarding processes
    this.userService.getUsers(0, 100, undefined, { managerId: currentUser.userId }).subscribe({
      next: (usersResponse) => {
        const users = usersResponse.content;
        const totalTeamMembers = users.length;

        if (users.length === 0) {
          this.stats.set({
            totalTeamMembers: 0,
            activeOnboarding: 0,
            completedOnboarding: 0,
            averageProgress: 0,
          });
          this.teamProgress.set([]);
          this.isLoading.set(false);
          return;
        }

        // Load onboarding processes for all users
        forkJoin({
          active: this.onboardingService.getOnboardingProcesses(0, 100, {
            managerId: currentUser.userId,
            status: 'ACTIVE',
          }),
          completed: this.onboardingService.getOnboardingProcesses(0, 100, {
            managerId: currentUser.userId,
            status: 'COMPLETED',
          }),
        }).subscribe({
          next: (processResponses) => {
            const activeProcesses = processResponses.active.content;
            const completedProcesses = processResponses.completed.content;

            // Calculate stats
            const totalProgress = activeProcesses.reduce((sum, p) => {
              return sum + (p.completedTasksCount / p.totalTasksCount) * 100;
            }, 0);
            const averageProgress =
              activeProcesses.length > 0 ? Math.round(totalProgress / activeProcesses.length) : 0;

            this.stats.set({
              totalTeamMembers,
              activeOnboarding: activeProcesses.length,
              completedOnboarding: completedProcesses.length,
              averageProgress,
            });

            // Build team progress list from active processes
            const progressList: TeamMemberProgress[] = activeProcesses.map((process) => {
              const user = users.find((u) => u.userId === process.userId);
              const progress = Math.round(
                (process.completedTasksCount / process.totalTasksCount) * 100
              );
              const tasksLeft = process.totalTasksCount - process.completedTasksCount;

              return {
                name: user ? `${user.firstName} ${user.lastName}` : 'Unknown',
                position: user?.positionName || 'N/A',
                progress,
                tasksLeft,
              };
            });

            this.teamProgress.set(progressList);
            this.isLoading.set(false);
          },
          error: (error) => {
            console.error('Error loading onboarding processes:', error);
            this.errorMessage.set('Błąd podczas ładowania procesów');
            this.isLoading.set(false);
          },
        });
      },
      error: (error) => {
        console.error('Error loading team members:', error);
        this.errorMessage.set('Błąd podczas ładowania zespołu');
        this.isLoading.set(false);
      },
    });
  }
}
