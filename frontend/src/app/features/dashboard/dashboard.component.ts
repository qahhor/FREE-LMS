import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard-container">
      <h1>Welcome to LMS Dashboard</h1>
      <p>Your learning journey starts here!</p>
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: 40px;
      max-width: 1200px;
      margin: 0 auto;
    }
  `]
})
export class DashboardComponent {}
