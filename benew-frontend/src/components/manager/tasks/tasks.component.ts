import { Component, signal, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatExpansionModule } from '@angular/material/expansion';
import { OnboardingService } from '../../../services/onboarding.service';
import { ManagerTaskDto } from '../../../models/onboarding.dto';

interface TaskGroup {
  userName: string;
  userPosition: string;
  userId: string;
  tasks: ManagerTaskDto[];
}

/**
 * Manager Tasks component - displays and manages tasks assigned to manager
 */
@Component({
  selector: 'app-manager-tasks',
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatChipsModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatExpansionModule,
  ],
  templateUrl: './tasks.component.html',
  styleUrl: './tasks.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TasksComponent implements OnInit {
  private readonly onboardingService = inject(OnboardingService);

  readonly taskGroups = signal<TaskGroup[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly stats = signal({
    total: 0,
    completed: 0,
    pending: 0,
  });

  ngOnInit(): void {
    this.loadManagerTasks();
  }

  private loadManagerTasks(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.onboardingService.getManagerTasks().subscribe({
      next: (tasks) => {
        this.groupTasksByUser(tasks);
        this.calculateStats(tasks);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading manager tasks:', error);
        this.errorMessage.set('Błąd podczas ładowania zadań');
        this.isLoading.set(false);
      },
    });
  }

  private groupTasksByUser(tasks: ManagerTaskDto[]): void {
    const grouped = new Map<string, TaskGroup>();

    tasks.forEach((task) => {
      const key = task.userId;
      if (!grouped.has(key)) {
        grouped.set(key, {
          userName: `${task.userFirstName} ${task.userLastName}`,
          userPosition: task.userPosition,
          userId: task.userId,
          tasks: [],
        });
      }
      grouped.get(key)!.tasks.push(task);
    });

    // Sort tasks within each group by taskOrder
    grouped.forEach((group) => {
      group.tasks.sort((a, b) => a.taskOrder - b.taskOrder);
    });

    this.taskGroups.set(Array.from(grouped.values()));
  }

  private calculateStats(tasks: ManagerTaskDto[]): void {
    const total = tasks.length;
    const completed = tasks.filter((t) => t.isCompleted).length;
    const pending = total - completed;

    this.stats.set({ total, completed, pending });
  }

  onToggleTask(task: ManagerTaskDto): void {
    const newStatus = !task.isCompleted;

    this.onboardingService
      .updateOnboardingTask(task.processId, task.id, { isCompleted: newStatus })
      .subscribe({
        next: (updatedTask) => {
          // Update local state
          task.isCompleted = updatedTask.isCompleted;
          
          // Recalculate stats
          const allTasks = this.taskGroups()
            .flatMap((group) => group.tasks);
          this.calculateStats(allTasks);
          
          console.log('Task updated successfully');
        },
        error: (error) => {
          console.error('Error updating task:', error);
          this.errorMessage.set('Błąd podczas aktualizacji zadania');
        },
      });
  }

  onRefresh(): void {
    this.loadManagerTasks();
  }

  getPendingTasksCount(group: TaskGroup): number {
    return group.tasks.filter((t) => !t.isCompleted).length;
  }
}
