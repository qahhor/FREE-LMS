import { Routes } from '@angular/router';
import { ScormLibraryComponent } from './components/scorm-library.component';
import { ScormPlayerComponent } from './components/scorm-player.component';

export const SCORM_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'library',
    pathMatch: 'full'
  },
  {
    path: 'library',
    component: ScormLibraryComponent
  },
  {
    path: 'player/:id',
    component: ScormPlayerComponent
  }
];
