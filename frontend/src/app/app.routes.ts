import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: 'dashboard',
    loadChildren: () => import('./features/dashboard/dashboard.routes').then(m => m.DASHBOARD_ROUTES),
    canActivate: [authGuard]
  },
  {
    path: 'courses',
    loadChildren: () => import('./features/courses/courses.routes').then(m => m.COURSES_ROUTES)
  },
  {
    path: 'profile',
    loadChildren: () => import('./features/profile/profile.routes').then(m => m.PROFILE_ROUTES),
    canActivate: [authGuard]
  },
  // Phase 3: Monetization & Enterprise Routes
  {
    path: 'subscriptions',
    loadChildren: () => import('./features/subscriptions/subscriptions.routes').then(m => m.SUBSCRIPTIONS_ROUTES)
  },
  {
    path: 'organizations',
    loadChildren: () => import('./features/organizations/organizations.routes').then(m => m.ORGANIZATIONS_ROUTES),
    canActivate: [authGuard]
  },
  {
    path: 'scorm',
    loadChildren: () => import('./features/scorm/scorm.routes').then(m => m.SCORM_ROUTES),
    canActivate: [authGuard]
  },
  {
    path: 'webinars',
    loadChildren: () => import('./features/webinars/webinars.routes').then(m => m.WEBINARS_ROUTES),
    canActivate: [authGuard]
  },
  {
    path: 'payments',
    loadChildren: () => import('./features/payments/payments.routes').then(m => m.PAYMENTS_ROUTES)
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
