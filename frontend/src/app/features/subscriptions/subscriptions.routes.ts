import { Routes } from '@angular/router';
import { PricingPageComponent } from './components/pricing-page.component';
import { SubscriptionDashboardComponent } from './components/subscription-dashboard.component';

export const SUBSCRIPTIONS_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'pricing',
    pathMatch: 'full'
  },
  {
    path: 'pricing',
    component: PricingPageComponent
  },
  {
    path: 'dashboard',
    component: SubscriptionDashboardComponent
  }
];
