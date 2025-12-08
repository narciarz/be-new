import { Component, signal, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FormsModule } from '@angular/forms';
import { OnboardingService } from '../../../services/onboarding.service';
import { AuthService } from '../../../services/auth.service';
import { OnboardingTaskDto } from '../../../models/onboarding.dto';

/**
 * Checklist component for USER role - displays personal onboarding tasks.
 */
@Component({
  selector: 'app-checklist',
  imports: [
    MatCardModule,
    MatIconModule,
    MatCheckboxModule,
    MatProgressBarModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    FormsModule,
  ],
  templateUrl: './checklist.component.html',
  styleUrl: './checklist.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChecklistComponent implements OnInit {
  private readonly onboardingService = inject(OnboardingService);
  private readonly authService = inject(AuthService);

  readonly tasks = signal<OnboardingTaskDto[]>([]);
  readonly progress = signal(0);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  
  private processId: string | null = null;

  // Helper method for template
  getCompletedTasksCount(): number {
    return this.tasks().filter((task) => task.isCompleted).length;
  }

  ngOnInit(): void {
    this.loadUserOnboardingTasks();
  }

  private loadUserOnboardingTasks(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    const currentUser = this.authService.currentUser();
    if (!currentUser) {
      this.errorMessage.set('Brak danych użytkownika');
      this.isLoading.set(false);
      return;
    }

    // Get onboarding processes for current user
    this.onboardingService
      .getOnboardingProcesses(0, 1, { userId: currentUser.id, status: 'ACTIVE' })
      .subscribe({
        next: (response) => {
          if (response.content.length > 0) {
            const process = response.content[0];
            this.processId = process.id;
            this.loadTasks(process.id);
            this.calculateProgress(process.completedTasksCount, process.totalTasksCount);
          } else {
            this.errorMessage.set('Nie znaleziono aktywnego procesu onboardingu');
            this.isLoading.set(false);
          }
        },
        error: (error) => {
          console.error('Error loading onboarding process:', error);
          this.errorMessage.set('Błąd podczas ładowania procesu onboardingu');
          this.isLoading.set(false);
        },
      });
  }

  private loadTasks(processId: string): void {
    this.onboardingService.getOnboardingTasks(processId).subscribe({
      next: (tasks) => {
        this.tasks.set(tasks);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading tasks:', error);
        this.errorMessage.set('Błąd podczas ładowania zadań');
        this.isLoading.set(false);
      },
    });
  }

  private calculateProgress(completed: number, total: number): void {
    if (total === 0) {
      this.progress.set(0);
      return;
    }
    const percentage = Math.round((completed / total) * 100);
    this.progress.set(percentage);
  }

  onToggleTask(taskId: string, completed: boolean): void {
    if (!this.processId) {
      return;
    }

    this.onboardingService
      .updateOnboardingTask(this.processId, taskId, { isCompleted: completed })
      .subscribe({
        next: (updatedTask) => {
          // Update local state
          const updatedTasks = this.tasks().map((task) =>
            task.id === taskId ? updatedTask : task
          );
          this.tasks.set(updatedTasks);

          // Recalculate progress
          const completedCount = updatedTasks.filter((t) => t.isCompleted).length;
          this.calculateProgress(completedCount, updatedTasks.length);
        },
        error: (error) => {
          console.error('Error updating task:', error);
          this.errorMessage.set('Błąd podczas aktualizacji zadania');
        },
      });
  }
}

