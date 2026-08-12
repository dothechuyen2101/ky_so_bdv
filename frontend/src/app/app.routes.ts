import { Routes } from '@angular/router';
import { adminGuard, authGuard, guestGuard, operationsGuard } from './core/session.guards';

export const appRoutes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./pages/login/login.page').then((component) => component.LoginPage),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/workspace-layout/workspace-layout.component').then(
        (component) => component.WorkspaceLayoutComponent,
      ),
    children: [
      {
        path: 'overview',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./pages/overview/overview.page').then((component) => component.OverviewPage),
      },
      {
        path: 'documents',
        loadComponent: () =>
          import('./pages/documents/documents.page').then((component) => component.DocumentsPage),
      },
      {
        path: 'operations',
        canActivate: [operationsGuard],
        loadComponent: () =>
          import('./pages/operations/operations.page').then(
            (component) => component.OperationsPage,
          ),
      },
      {
        path: 'audit',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./pages/audit/audit.page').then((component) => component.AuditPage),
      },
      {
        path: 'users',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./pages/users/users.page').then((component) => component.UsersPage),
      },
      { path: '', pathMatch: 'full', redirectTo: 'documents' },
    ],
  },
  { path: '**', redirectTo: '' },
];
