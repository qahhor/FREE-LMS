import { Routes } from '@angular/router';
import { OrganizationDashboardComponent } from './components/organization-dashboard.component';
import { TeamMembersComponent } from './components/team-members.component';
import { BrandingSettingsComponent } from './components/branding-settings.component';
import { SsoConfigComponent } from './components/sso-config.component';
import { ApiKeysComponent } from './components/api-keys.component';

export const ORGANIZATIONS_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    component: OrganizationDashboardComponent
  },
  {
    path: 'team',
    component: TeamMembersComponent
  },
  {
    path: 'branding',
    component: BrandingSettingsComponent
  },
  {
    path: 'sso',
    component: SsoConfigComponent
  },
  {
    path: 'api-keys',
    component: ApiKeysComponent
  }
];
