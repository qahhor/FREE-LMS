import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Subject, takeUntil } from 'rxjs';
import { PwaService } from '../../../core/services/pwa.service';

@Component({
  selector: 'app-pwa-install-banner',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
  ],
  template: `
    <div class="pwa-banner" *ngIf="showBanner && !isInstalled">
      <div class="pwa-banner-content">
        <mat-icon class="pwa-icon">install_mobile</mat-icon>
        <div class="pwa-text">
          <h3>Установите Smartup LMS</h3>
          <p>Быстрый доступ и работа офлайн</p>
        </div>
      </div>
      <div class="pwa-actions">
        <button mat-button (click)="dismiss()">Позже</button>
        <button mat-raised-button color="primary" (click)="install()">
          Установить
        </button>
      </div>
    </div>

    <div class="online-indicator" *ngIf="!isOnline">
      <mat-icon>wifi_off</mat-icon>
      <span>Нет подключения</span>
    </div>
  `,
  styles: [`
    .pwa-banner {
      position: fixed;
      bottom: 20px;
      left: 50%;
      transform: translateX(-50%);
      background: white;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
      border-radius: 12px;
      padding: 16px 20px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      max-width: 500px;
      width: calc(100% - 40px);
      z-index: 1000;
      animation: slideUp 0.3s ease-out;
    }

    @keyframes slideUp {
      from {
        transform: translateX(-50%) translateY(100px);
        opacity: 0;
      }
      to {
        transform: translateX(-50%) translateY(0);
        opacity: 1;
      }
    }

    .pwa-banner-content {
      display: flex;
      align-items: center;
      gap: 16px;
      flex: 1;
    }

    .pwa-icon {
      font-size: 40px;
      width: 40px;
      height: 40px;
      color: #3f51b5;
    }

    .pwa-text h3 {
      margin: 0 0 4px 0;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }

    .pwa-text p {
      margin: 0;
      font-size: 14px;
      color: #666;
    }

    .pwa-actions {
      display: flex;
      gap: 8px;
      align-items: center;
    }

    .online-indicator {
      position: fixed;
      top: 60px;
      right: 20px;
      background: #f44336;
      color: white;
      padding: 8px 16px;
      border-radius: 20px;
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      box-shadow: 0 2px 10px rgba(244, 67, 54, 0.3);
      z-index: 1000;
      animation: pulse 2s infinite;
    }

    @keyframes pulse {
      0%, 100% {
        opacity: 1;
      }
      50% {
        opacity: 0.7;
      }
    }

    .online-indicator mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    @media (max-width: 600px) {
      .pwa-banner {
        flex-direction: column;
        align-items: stretch;
      }

      .pwa-actions {
        margin-top: 12px;
        justify-content: flex-end;
      }
    }
  `],
})
export class PwaInstallBannerComponent implements OnInit, OnDestroy {
  showBanner = false;
  isInstalled = false;
  isOnline = true;

  private destroy$ = new Subject<void>();
  private dismissedKey = 'pwa-install-dismissed';
  private dismissedUntil = 0;

  constructor(
    private pwaService: PwaService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    // Проверяем, установлено ли уже приложение
    this.isInstalled = this.pwaService.isInstalled();

    // Отслеживаем онлайн статус
    this.pwaService.online$()
      .pipe(takeUntil(this.destroy$))
      .subscribe(online => {
        this.isOnline = online;

        if (!online) {
          this.snackBar.open('Вы офлайн. Некоторые функции могут быть недоступны.', 'OK', {
            duration: 3000,
          });
        }
      });

    // Слушаем промпт для установки
    if (!this.isInstalled) {
      this.pwaService.installPrompt$
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => {
          this.checkShouldShowBanner();
        });

      // Проверяем сразу
      if (this.pwaService.isInstallAvailable()) {
        this.checkShouldShowBanner();
      }
    }

    // Логируем PWA support
    const support = this.pwaService.checkPWASupport();
    console.log('PWA Support:', support);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private checkShouldShowBanner(): void {
    // Проверяем, не был ли баннер отклонен недавно
    const dismissed = localStorage.getItem(this.dismissedKey);
    if (dismissed) {
      this.dismissedUntil = parseInt(dismissed, 10);
      if (Date.now() < this.dismissedUntil) {
        return;
      }
    }

    // Показываем баннер через 5 секунд
    setTimeout(() => {
      this.showBanner = true;
    }, 5000);
  }

  async install(): Promise<void> {
    const success = await this.pwaService.promptInstall();

    if (success) {
      this.showBanner = false;
      this.snackBar.open('Приложение установлено!', 'OK', {
        duration: 3000,
      });
    } else {
      this.snackBar.open('Установка отменена', 'OK', {
        duration: 2000,
      });
    }
  }

  dismiss(): void {
    this.showBanner = false;

    // Не показывать баннер 7 дней
    const dismissUntil = Date.now() + (7 * 24 * 60 * 60 * 1000);
    localStorage.setItem(this.dismissedKey, dismissUntil.toString());
  }
}
