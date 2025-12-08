import { Component, signal, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UserService } from '../../../services/user.service';
import { AuthService } from '../../../services/auth.service';
import { OnboardingService } from '../../../services/onboarding.service';
import { UserDto } from '../../../models/user.dto';
import { OnboardingProcessDto } from '../../../models/onboarding.dto';
import { forkJoin } from 'rxjs';

interface TeamMemberView {
  userId: string;
  firstName: string;
  lastName: string;
  email: string;
  position: string;
  startDate: string;
  onboardingProgress: number;
  status: string;
}

/**
 * Team View component for Manager - displays team members.
 */
@Component({
  selector: 'app-team',
  imports: [
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatChipsModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './team.component.html',
  styleUrl: './team.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TeamComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly onboardingService = inject(OnboardingService);

  readonly teamMembers = signal<TeamMemberView[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadTeamMembers();
  }

  private loadTeamMembers(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    const currentUser = this.authService.currentUser();
    if (!currentUser) {
      this.errorMessage.set('Brak danych użytkownika');
      this.isLoading.set(false);
      return;
    }

    // Get users managed by current user
    this.userService.getUsers(0, 100, undefined, { managerId: currentUser.userId }).subscribe({
      next: (response) => {
        const users = response.content;
        if (users.length === 0) {
          this.teamMembers.set([]);
          this.isLoading.set(false);
          return;
        }

        // Load onboarding processes for each user
        const onboardingRequests = users.map((user) =>
          this.onboardingService.getOnboardingProcesses(0, 1, { userId: user.userId })
        );

        forkJoin(onboardingRequests).subscribe({
          next: (processResponses) => {
            const members: TeamMemberView[] = users.map((user, index) => {
              const processes = processResponses[index].content;
              const activeProcess = processes.find((p) => p.status === 'ACTIVE');
              const completedProcess = processes.find((p) => p.status === 'COMPLETED');

              const progress = activeProcess
                ? Math.round(
                    (activeProcess.completedTasksCount / activeProcess.totalTasksCount) * 100
                  )
                : completedProcess
                  ? 100
                  : 0;

              return {
                userId: user.userId,
                firstName: user.firstName,
                lastName: user.lastName,
                email: user.email,
                position: user.positionName,
                startDate: user.createdAt?.split('T')[0] || 'N/A',
                onboardingProgress: progress,
                status: completedProcess ? 'completed' : activeProcess ? 'active' : 'no-process',
              };
            });

            this.teamMembers.set(members);
            this.isLoading.set(false);
          },
          error: (error) => {
            console.error('Error loading onboarding processes:', error);
            this.errorMessage.set('Błąd podczas ładowania procesów onboardingu');
            this.isLoading.set(false);
          },
        });
      },
      error: (error) => {
        console.error('Error loading team members:', error);
        this.errorMessage.set('Błąd podczas ładowania członków zespołu');
        this.isLoading.set(false);
      },
    });
  }

  onViewDetails(memberId: string): void {
    console.log('View member details:', memberId);
    // TODO: Navigate to member details
  }
}
