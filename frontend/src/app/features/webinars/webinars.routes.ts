import { Routes } from '@angular/router';
import { WebinarScheduleComponent } from './components/webinar-schedule.component';
import { WebinarRoomComponent } from './components/webinar-room.component';
import { WebinarHistoryComponent } from './components/webinar-history.component';

export const WEBINARS_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'schedule',
    pathMatch: 'full'
  },
  {
    path: 'schedule',
    component: WebinarScheduleComponent
  },
  {
    path: 'room/:id',
    component: WebinarRoomComponent
  },
  {
    path: 'history',
    component: WebinarHistoryComponent
  }
];
