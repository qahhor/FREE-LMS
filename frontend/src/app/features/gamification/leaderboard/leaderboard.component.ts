import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '@environments/environment';

interface LeaderboardEntry {
  userId: number;
  user: {
    firstName: string;
    lastName: string;
    avatarUrl?: string;
  };
  totalPoints: number;
  level: number;
  badgeCount: number;
  rank: number;
}

interface Badge {
  id: number;
  name: string;
  description: string;
  icon: string;
  category: string;
  rarity: string;
  pointsReward: number;
  unlockCount: number;
}

interface UserProgress {
  totalPoints: number;
  level: number;
  pointsToNextLevel: number;
  rank: number;
  badges: Array<{
    id: number;
    badge: Badge;
    unlockedAt: Date;
    isShowcased: boolean;
  }>;
}

/**
 * Gamification leaderboard and badges component
 */
@Component({
  selector: 'app-leaderboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="gamification-container">
      <!-- Header -->
      <div class="header">
        <h1>🏆 Leaderboard & Achievements</h1>
        <div class="tabs">
          <button
            class="tab"
            [class.active]="activeTab === 'leaderboard'"
            (click)="activeTab = 'leaderboard'"
          >
            Leaderboard
          </button>
          <button
            class="tab"
            [class.active]="activeTab === 'badges'"
            (click)="activeTab = 'badges'"
          >
            Badges
          </button>
          <button
            class="tab"
            [class.active]="activeTab === 'my-progress'"
            (click)="activeTab = 'my-progress'"
          >
            My Progress
          </button>
        </div>
      </div>

      <!-- Leaderboard Tab -->
      <div *ngIf="activeTab === 'leaderboard'" class="tab-content">
        <div *ngIf="loading" class="loading">Loading...</div>

        <div *ngIf="!loading" class="leaderboard-list">
          <div
            *ngFor="let entry of leaderboard"
            class="leaderboard-entry"
            [class.top-three]="entry.rank <= 3"
            [class.current-user]="isCurrentUser(entry.userId)"
          >
            <div class="rank" [class.gold]="entry.rank === 1" [class.silver]="entry.rank === 2" [class.bronze]="entry.rank === 3">
              <span class="rank-number">{{ entry.rank }}</span>
              <span *ngIf="entry.rank === 1" class="trophy">🥇</span>
              <span *ngIf="entry.rank === 2" class="trophy">🥈</span>
              <span *ngIf="entry.rank === 3" class="trophy">🥉</span>
            </div>

            <div class="user-info">
              <div class="avatar">
                <img *ngIf="entry.user.avatarUrl" [src]="entry.user.avatarUrl" [alt]="entry.user.firstName" />
                <div *ngIf="!entry.user.avatarUrl" class="avatar-placeholder">
                  {{ entry.user.firstName[0] }}{{ entry.user.lastName[0] }}
                </div>
              </div>
              <div class="name-level">
                <div class="name">{{ entry.user.firstName }} {{ entry.user.lastName }}</div>
                <div class="level">Level {{ entry.level }}</div>
              </div>
            </div>

            <div class="stats">
              <div class="stat-item">
                <span class="stat-icon">⭐</span>
                <span class="stat-value">{{ entry.totalPoints }}</span>
                <span class="stat-label">Points</span>
              </div>
              <div class="stat-item">
                <span class="stat-icon">🎖️</span>
                <span class="stat-value">{{ entry.badgeCount }}</span>
                <span class="stat-label">Badges</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Badges Tab -->
      <div *ngIf="activeTab === 'badges'" class="tab-content">
        <div *ngIf="loading" class="loading">Loading...</div>

        <div *ngIf="!loading" class="badges-grid">
          <div
            *ngFor="let badge of allBadges"
            class="badge-card"
            [class]="'rarity-' + badge.rarity"
          >
            <div class="badge-icon">{{ badge.icon }}</div>
            <h3>{{ badge.name }}</h3>
            <p class="badge-description">{{ badge.description }}</p>
            <div class="badge-meta">
              <span class="badge-rarity">{{ badge.rarity }}</span>
              <span class="badge-points">+{{ badge.pointsReward }} pts</span>
            </div>
            <div class="badge-unlock-count">
              Unlocked by {{ badge.unlockCount }} users
            </div>
          </div>
        </div>
      </div>

      <!-- My Progress Tab -->
      <div *ngIf="activeTab === 'my-progress'" class="tab-content">
        <div *ngIf="loading" class="loading">Loading...</div>

        <div *ngIf="!loading && userProgress" class="progress-content">
          <!-- Level Progress -->
          <div class="level-card">
            <div class="level-header">
              <h2>Level {{ userProgress.level }}</h2>
              <p>Rank #{{ userProgress.rank }}</p>
            </div>
            <div class="progress-bar-container">
              <div class="progress-bar">
                <div
                  class="progress-fill"
                  [style.width.%]="getLevelProgress()"
                ></div>
              </div>
              <p class="progress-text">
                {{ userProgress.pointsToNextLevel }} points to next level
              </p>
            </div>
            <div class="total-points">
              <span class="points-icon">⭐</span>
              <span class="points-value">{{ userProgress.totalPoints }}</span>
              <span class="points-label">Total Points</span>
            </div>
          </div>

          <!-- My Badges -->
          <div class="my-badges-section">
            <h2>My Badges ({{ userProgress.badges.length }})</h2>
            <div *ngIf="userProgress.badges.length === 0" class="empty-state">
              No badges yet. Keep learning to unlock achievements!
            </div>
            <div *ngIf="userProgress.badges.length > 0" class="badges-grid">
              <div
                *ngFor="let userBadge of userProgress.badges"
                class="badge-card unlocked"
                [class]="'rarity-' + userBadge.badge.rarity"
              >
                <div class="badge-icon">{{ userBadge.badge.icon }}</div>
                <h3>{{ userBadge.badge.name }}</h3>
                <p class="badge-description">{{ userBadge.badge.description }}</p>
                <div class="unlocked-date">
                  Unlocked {{ formatDate(userBadge.unlockedAt) }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .gamification-container {
      max-width: 1400px;
      margin: 0 auto;
      padding: 40px 20px;
    }

    .header {
      text-align: center;
      margin-bottom: 40px;
    }

    .header h1 {
      font-size: 42px;
      margin-bottom: 30px;
    }

    .tabs {
      display: flex;
      gap: 10px;
      justify-content: center;
    }

    .tab {
      padding: 12px 24px;
      border: none;
      background: #f5f5f5;
      border-radius: 8px;
      cursor: pointer;
      font-size: 16px;
      font-weight: 600;
      transition: all 0.2s;
    }

    .tab:hover {
      background: #e0e0e0;
    }

    .tab.active {
      background: #667eea;
      color: white;
    }

    .loading {
      text-align: center;
      padding: 60px;
      font-size: 18px;
    }

    .leaderboard-list {
      display: flex;
      flex-direction: column;
      gap: 15px;
    }

    .leaderboard-entry {
      display: grid;
      grid-template-columns: 80px 1fr auto;
      gap: 20px;
      align-items: center;
      background: white;
      padding: 20px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      transition: transform 0.2s;
    }

    .leaderboard-entry:hover {
      transform: translateY(-2px);
    }

    .leaderboard-entry.top-three {
      background: linear-gradient(135deg, #fff 0%, #f5f7fa 100%);
      border: 2px solid #ffd700;
    }

    .leaderboard-entry.current-user {
      border: 2px solid #667eea;
    }

    .rank {
      text-align: center;
      font-size: 24px;
      font-weight: bold;
    }

    .rank.gold { color: #ffd700; }
    .rank.silver { color: #c0c0c0; }
    .rank.bronze { color: #cd7f32; }

    .trophy {
      display: block;
      font-size: 32px;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 15px;
    }

    .avatar {
      width: 50px;
      height: 50px;
      border-radius: 50%;
      overflow: hidden;
    }

    .avatar img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .avatar-placeholder {
      width: 100%;
      height: 100%;
      background: #667eea;
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      font-size: 18px;
    }

    .name {
      font-size: 18px;
      font-weight: 600;
    }

    .level {
      font-size: 14px;
      color: #666;
    }

    .stats {
      display: flex;
      gap: 30px;
    }

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 5px;
    }

    .stat-icon {
      font-size: 24px;
    }

    .stat-value {
      font-size: 20px;
      font-weight: bold;
    }

    .stat-label {
      font-size: 12px;
      color: #666;
    }

    .badges-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: 20px;
    }

    .badge-card {
      background: white;
      padding: 30px;
      border-radius: 12px;
      text-align: center;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      transition: transform 0.2s;
      border: 3px solid transparent;
    }

    .badge-card:hover {
      transform: translateY(-5px);
    }

    .badge-card.rarity-common { border-color: #9e9e9e; }
    .badge-card.rarity-uncommon { border-color: #4caf50; }
    .badge-card.rarity-rare { border-color: #2196f3; }
    .badge-card.rarity-epic { border-color: #9c27b0; }
    .badge-card.rarity-legendary { border-color: #ff9800; }

    .badge-card.unlocked {
      opacity: 1;
    }

    .badge-icon {
      font-size: 64px;
      margin-bottom: 15px;
    }

    .badge-card h3 {
      font-size: 20px;
      margin-bottom: 10px;
    }

    .badge-description {
      font-size: 14px;
      color: #666;
      margin-bottom: 15px;
    }

    .badge-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 15px;
      border-top: 1px solid #e0e0e0;
    }

    .badge-rarity {
      font-size: 12px;
      font-weight: bold;
      text-transform: uppercase;
    }

    .badge-points {
      font-size: 14px;
      font-weight: 600;
      color: #667eea;
    }

    .badge-unlock-count {
      margin-top: 10px;
      font-size: 12px;
      color: #999;
    }

    .unlocked-date {
      margin-top: 10px;
      font-size: 12px;
      color: #4caf50;
      font-weight: 600;
    }

    .level-card {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 40px;
      border-radius: 16px;
      margin-bottom: 40px;
    }

    .level-header {
      text-align: center;
      margin-bottom: 30px;
    }

    .level-header h2 {
      font-size: 48px;
      margin-bottom: 10px;
    }

    .progress-bar-container {
      margin-bottom: 30px;
    }

    .progress-bar {
      height: 20px;
      background: rgba(255, 255, 255, 0.3);
      border-radius: 10px;
      overflow: hidden;
      margin-bottom: 10px;
    }

    .progress-fill {
      height: 100%;
      background: white;
      border-radius: 10px;
      transition: width 0.5s ease;
    }

    .progress-text {
      text-align: center;
      font-size: 16px;
      opacity: 0.9;
    }

    .total-points {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;
    }

    .points-icon {
      font-size: 48px;
    }

    .points-value {
      font-size: 64px;
      font-weight: bold;
    }

    .points-label {
      font-size: 18px;
      opacity: 0.9;
    }

    .my-badges-section h2 {
      font-size: 32px;
      margin-bottom: 30px;
    }

    .empty-state {
      text-align: center;
      padding: 60px 20px;
      color: #666;
      font-size: 18px;
    }
  `],
})
export class LeaderboardComponent implements OnInit {
  activeTab: 'leaderboard' | 'badges' | 'my-progress' = 'leaderboard';
  leaderboard: LeaderboardEntry[] = [];
  allBadges: Badge[] = [];
  userProgress?: UserProgress;
  loading = true;
  currentUserId?: number;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadData();
  }

  private async loadData(): Promise<void> {
    this.loading = true;
    try {
      // Load all data in parallel
      const [leaderboard, badges, progress] = await Promise.all([
        this.http.get<LeaderboardEntry[]>(`${environment.apiUrl}/gamification/leaderboard`).toPromise(),
        this.http.get<Badge[]>(`${environment.apiUrl}/gamification/badges`).toPromise(),
        this.http.get<UserProgress>(`${environment.apiUrl}/gamification/my-progress`).toPromise().catch(() => null),
      ]);

      this.leaderboard = leaderboard || [];
      this.allBadges = badges || [];
      this.userProgress = progress || undefined;
    } catch (error) {
      console.error('Error loading gamification data:', error);
    } finally {
      this.loading = false;
    }
  }

  isCurrentUser(userId: number): boolean {
    return this.currentUserId === userId;
  }

  getLevelProgress(): number {
    if (!this.userProgress) return 0;
    const pointsInLevel = this.userProgress.totalPoints % 1000;
    return (pointsInLevel / 1000) * 100;
  }

  formatDate(date: Date | string): string {
    const d = new Date(date);
    return d.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  }
}
