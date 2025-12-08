import { Routes } from '@angular/router';
import { authGuard, guestGuard } from '../services';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full',
  },
  {
    path: 'login',
    loadComponent: () =>
      import('../components/login/login.component').then((m) => m.LoginComponent),
    canActivate: [guestGuard],
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('../components/dashboard/dashboard.component').then((m) => m.DashboardComponent),
    canActivate: [authGuard],
    children: [
      {
        path: '',
        redirectTo: 'home',
        pathMatch: 'full',
      },
      {
        path: 'home',
        loadComponent: () =>
          import('../components/home/home.component').then((m) => m.HomeComponent),
      },
      // Admin routes
      {
        path: 'admin/users',
        loadComponent: () =>
          import('../components/admin/user-management/user-management.component').then(
            (m) => m.UserManagementComponent
          ),
      },
      {
        path: 'admin/templates',
        loadComponent: () =>
          import('../components/admin/templates/templates.component').then(
            (m) => m.TemplatesComponent
          ),
      },
      {
        path: 'admin/import',
        loadComponent: () =>
          import('../components/admin/template-import/template-import.component').then(
            (m) => m.TemplateImportComponent
          ),
      },
      // Manager routes
      {
        path: 'manager/overview',
        loadComponent: () =>
          import('../components/manager/overview/overview.component').then(
            (m) => m.OverviewComponent
          ),
      },
      {
        path: 'manager/team',
        loadComponent: () =>
          import('../components/manager/team/team.component').then((m) => m.TeamComponent),
      },
      {
        path: 'manager/processes',
        loadComponent: () =>
          import('../components/manager/processes/processes.component').then(
            (m) => m.ProcessesComponent
          ),
      },
      {
        path: 'manager/tasks',
        loadComponent: () =>
          import('../components/manager/tasks/tasks.component').then(
            (m) => m.TasksComponent
          ),
      },
      // User routes
      {
        path: 'user/checklist',
        loadComponent: () =>
          import('../components/user/checklist/checklist.component').then(
            (m) => m.ChecklistComponent
          ),
      },
      {
        path: 'user/info',
        loadComponent: () =>
          import('../components/user/info/info.component').then((m) => m.InfoComponent),
      },
    ],
  },
  {
    path: '**',
    redirectTo: '/dashboard',
  },
];
