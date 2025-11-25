import { Injectable } from '@angular/core';
import { Observable, fromEvent, merge, of, Subject } from 'rxjs';
import { map, distinctUntilChanged } from 'rxjs/operators';

export interface PWAInstallPrompt {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
}

@Injectable({
  providedIn: 'root',
})
export class PwaService {
  private deferredPrompt: PWAInstallPrompt | null = null;
  private installPromptSubject = new Subject<PWAInstallPrompt>();
  private swRegistration: ServiceWorkerRegistration | null = null;

  public installPrompt$ = this.installPromptSubject.asObservable();

  constructor() {
    this.init();
  }

  /**
   * Инициализация PWA сервиса
   */
  private init(): void {
    // Слушаем событие beforeinstallprompt
    window.addEventListener('beforeinstallprompt', (e: Event) => {
      e.preventDefault();
      this.deferredPrompt = e as any;
      this.installPromptSubject.next(this.deferredPrompt);
      console.log('PWA: Install prompt available');
    });

    // Отслеживаем установку
    window.addEventListener('appinstalled', () => {
      console.log('PWA: App installed');
      this.deferredPrompt = null;
    });

    // Регистрация service worker
    if ('serviceWorker' in navigator) {
      this.registerServiceWorker();
    }
  }

  /**
   * Регистрация Service Worker
   */
  private async registerServiceWorker(): Promise<void> {
    try {
      const registration = await navigator.serviceWorker.register('/service-worker.js', {
        scope: '/',
      });

      this.swRegistration = registration;
      console.log('PWA: Service Worker registered', registration);

      // Проверка обновлений каждый час
      setInterval(() => {
        registration.update();
      }, 60 * 60 * 1000);

      // Слушаем обновления
      registration.addEventListener('updatefound', () => {
        const newWorker = registration.installing;
        if (newWorker) {
          newWorker.addEventListener('statechange', () => {
            if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
              console.log('PWA: New version available');
              this.promptUserToUpdate();
            }
          });
        }
      });
    } catch (error) {
      console.error('PWA: Service Worker registration failed', error);
    }
  }

  /**
   * Показать промпт для установки PWA
   */
  public async promptInstall(): Promise<boolean> {
    if (!this.deferredPrompt) {
      console.warn('PWA: Install prompt not available');
      return false;
    }

    try {
      await this.deferredPrompt.prompt();
      const choiceResult = await this.deferredPrompt.userChoice;

      if (choiceResult.outcome === 'accepted') {
        console.log('PWA: User accepted install');
        this.deferredPrompt = null;
        return true;
      } else {
        console.log('PWA: User dismissed install');
        return false;
      }
    } catch (error) {
      console.error('PWA: Error showing install prompt', error);
      return false;
    }
  }

  /**
   * Проверить, доступна ли установка
   */
  public isInstallAvailable(): boolean {
    return this.deferredPrompt !== null;
  }

  /**
   * Проверить, установлено ли приложение
   */
  public isInstalled(): boolean {
    // Chrome/Edge
    if (window.matchMedia('(display-mode: standalone)').matches) {
      return true;
    }

    // iOS Safari
    if ((navigator as any).standalone) {
      return true;
    }

    return false;
  }

  /**
   * Запросить разрешение на push уведомления
   */
  public async requestNotificationPermission(): Promise<NotificationPermission> {
    if (!('Notification' in window)) {
      console.warn('PWA: Notifications not supported');
      return 'denied';
    }

    if (Notification.permission === 'granted') {
      return 'granted';
    }

    if (Notification.permission === 'denied') {
      return 'denied';
    }

    const permission = await Notification.requestPermission();
    console.log('PWA: Notification permission:', permission);
    return permission;
  }

  /**
   * Подписаться на push уведомления
   */
  public async subscribeToPush(): Promise<PushSubscription | null> {
    if (!this.swRegistration) {
      console.warn('PWA: Service Worker not registered');
      return null;
    }

    const permission = await this.requestNotificationPermission();
    if (permission !== 'granted') {
      return null;
    }

    try {
      const subscription = await this.swRegistration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: this.urlBase64ToUint8Array(
          // VAPID public key - должен быть сгенерирован на backend
          'YOUR_VAPID_PUBLIC_KEY'
        ),
      });

      console.log('PWA: Push subscription:', subscription);
      return subscription;
    } catch (error) {
      console.error('PWA: Error subscribing to push', error);
      return null;
    }
  }

  /**
   * Отписаться от push уведомлений
   */
  public async unsubscribeFromPush(): Promise<boolean> {
    if (!this.swRegistration) {
      return false;
    }

    try {
      const subscription = await this.swRegistration.pushManager.getSubscription();
      if (subscription) {
        await subscription.unsubscribe();
        console.log('PWA: Unsubscribed from push');
        return true;
      }
      return false;
    } catch (error) {
      console.error('PWA: Error unsubscribing from push', error);
      return false;
    }
  }

  /**
   * Проверить статус подписки на push
   */
  public async getPushSubscription(): Promise<PushSubscription | null> {
    if (!this.swRegistration) {
      return null;
    }

    try {
      return await this.swRegistration.pushManager.getSubscription();
    } catch (error) {
      console.error('PWA: Error getting push subscription', error);
      return null;
    }
  }

  /**
   * Показать локальное уведомление
   */
  public async showNotification(
    title: string,
    options?: NotificationOptions
  ): Promise<void> {
    if (!this.swRegistration) {
      console.warn('PWA: Service Worker not registered');
      return;
    }

    const permission = await this.requestNotificationPermission();
    if (permission !== 'granted') {
      return;
    }

    try {
      await this.swRegistration.showNotification(title, {
        icon: '/assets/icons/icon-192x192.png',
        badge: '/assets/icons/badge-72x72.png',
        ...options,
      });
    } catch (error) {
      console.error('PWA: Error showing notification', error);
    }
  }

  /**
   * Отслеживание онлайн/оффлайн статуса
   */
  public online$(): Observable<boolean> {
    return merge(
      of(navigator.onLine),
      fromEvent(window, 'online').pipe(map(() => true)),
      fromEvent(window, 'offline').pipe(map(() => false))
    ).pipe(distinctUntilChanged());
  }

  /**
   * Проверить поддержку PWA функций
   */
  public checkPWASupport(): {
    serviceWorker: boolean;
    notifications: boolean;
    pushManager: boolean;
    installPrompt: boolean;
  } {
    return {
      serviceWorker: 'serviceWorker' in navigator,
      notifications: 'Notification' in window,
      pushManager: 'PushManager' in window,
      installPrompt: 'BeforeInstallPromptEvent' in window,
    };
  }

  /**
   * Синхронизировать данные в фоне
   */
  public async syncInBackground(tag: string): Promise<void> {
    if (!this.swRegistration || !('sync' in this.swRegistration)) {
      console.warn('PWA: Background Sync not supported');
      return;
    }

    try {
      await (this.swRegistration as any).sync.register(tag);
      console.log('PWA: Background sync registered:', tag);
    } catch (error) {
      console.error('PWA: Error registering background sync', error);
    }
  }

  /**
   * Обновить Service Worker
   */
  public async update(): Promise<void> {
    if (!this.swRegistration) {
      return;
    }

    try {
      await this.swRegistration.update();
      console.log('PWA: Service Worker updated');
    } catch (error) {
      console.error('PWA: Error updating Service Worker', error);
    }
  }

  /**
   * Очистить кэш
   */
  public async clearCache(): Promise<void> {
    if (!('caches' in window)) {
      return;
    }

    try {
      const cacheNames = await caches.keys();
      await Promise.all(cacheNames.map((name) => caches.delete(name)));
      console.log('PWA: Cache cleared');
    } catch (error) {
      console.error('PWA: Error clearing cache', error);
    }
  }

  /**
   * Промпт для обновления приложения
   */
  private promptUserToUpdate(): void {
    // Можно показать snackbar или dialog
    console.log('PWA: New version available. Please reload.');

    // Автоматический reload через 5 секунд
    setTimeout(() => {
      window.location.reload();
    }, 5000);
  }

  /**
   * Конвертировать base64 в Uint8Array для VAPID key
   */
  private urlBase64ToUint8Array(base64String: string): Uint8Array {
    const padding = '='.repeat((4 - (base64String.length % 4)) % 4);
    const base64 = (base64String + padding)
      .replace(/-/g, '+')
      .replace(/_/g, '/');

    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);

    for (let i = 0; i < rawData.length; ++i) {
      outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray;
  }
}
