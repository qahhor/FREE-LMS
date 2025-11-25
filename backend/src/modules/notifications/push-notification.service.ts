import { Injectable, Logger } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import * as webpush from 'web-push';
import { PushSubscription } from './entities/push-subscription.entity';
import { Notification } from './entities/notification.entity';

@Injectable()
export class PushNotificationService {
  private readonly logger = new Logger(PushNotificationService.name);

  constructor(
    @InjectRepository(PushSubscription)
    private pushSubscriptionRepo: Repository<PushSubscription>,
  ) {
    this.initializeWebPush();
  }

  /**
   * Initialize Web Push with VAPID keys
   */
  private initializeWebPush(): void {
    const vapidPublicKey = process.env.VAPID_PUBLIC_KEY;
    const vapidPrivateKey = process.env.VAPID_PRIVATE_KEY;
    const vapidSubject = process.env.VAPID_SUBJECT || 'mailto:support@freelms.com';

    if (!vapidPublicKey || !vapidPrivateKey) {
      this.logger.warn(
        'VAPID keys not configured. Push notifications will not work. ' +
        'Generate keys with: npx web-push generate-vapid-keys'
      );
      return;
    }

    webpush.setVapidDetails(
      vapidSubject,
      vapidPublicKey,
      vapidPrivateKey
    );

    this.logger.log('Web Push initialized with VAPID keys');
  }

  /**
   * Subscribe user to push notifications
   */
  async subscribe(
    userId: number,
    subscription: {
      endpoint: string;
      keys: { p256dh: string; auth: string };
    },
    userAgent?: string,
  ): Promise<PushSubscription> {
    // Check if subscription already exists
    const existing = await this.pushSubscriptionRepo.findOne({
      where: { endpoint: subscription.endpoint },
    });

    if (existing) {
      // Update existing subscription
      existing.userId = userId;
      existing.keys = JSON.stringify(subscription.keys);
      existing.userAgent = userAgent;
      existing.active = true;
      return this.pushSubscriptionRepo.save(existing);
    }

    // Create new subscription
    const newSubscription = this.pushSubscriptionRepo.create({
      userId,
      endpoint: subscription.endpoint,
      keys: JSON.stringify(subscription.keys),
      userAgent,
      active: true,
    });

    return this.pushSubscriptionRepo.save(newSubscription);
  }

  /**
   * Unsubscribe user from push notifications
   */
  async unsubscribe(userId: number, endpoint: string): Promise<boolean> {
    const subscription = await this.pushSubscriptionRepo.findOne({
      where: { userId, endpoint },
    });

    if (!subscription) {
      return false;
    }

    await this.pushSubscriptionRepo.remove(subscription);
    return true;
  }

  /**
   * Get all subscriptions for a user
   */
  async getUserSubscriptions(userId: number): Promise<PushSubscription[]> {
    return this.pushSubscriptionRepo.find({
      where: { userId, active: true },
    });
  }

  /**
   * Send push notification to user
   */
  async sendToUser(
    userId: number,
    notification: {
      title: string;
      body: string;
      icon?: string;
      badge?: string;
      data?: any;
      url?: string;
    },
  ): Promise<{ sent: number; failed: number }> {
    const subscriptions = await this.getUserSubscriptions(userId);

    if (subscriptions.length === 0) {
      this.logger.warn(`No push subscriptions found for user ${userId}`);
      return { sent: 0, failed: 0 };
    }

    const payload = JSON.stringify({
      title: notification.title,
      body: notification.body,
      icon: notification.icon || '/assets/icons/icon-192x192.png',
      badge: notification.badge || '/assets/icons/badge-72x72.png',
      data: notification.data,
      url: notification.url || '/',
    });

    let sent = 0;
    let failed = 0;

    for (const subscription of subscriptions) {
      try {
        const keys = JSON.parse(subscription.keys);
        const pushSubscription = {
          endpoint: subscription.endpoint,
          keys: {
            p256dh: keys.p256dh,
            auth: keys.auth,
          },
        };

        await webpush.sendNotification(pushSubscription, payload);
        sent++;
        this.logger.log(`Push notification sent to subscription ${subscription.id}`);
      } catch (error) {
        failed++;
        this.logger.error(
          `Failed to send push notification to subscription ${subscription.id}`,
          error.stack
        );

        // If subscription is expired or invalid, mark as inactive
        if (error.statusCode === 410 || error.statusCode === 404) {
          subscription.active = false;
          await this.pushSubscriptionRepo.save(subscription);
        }
      }
    }

    return { sent, failed };
  }

  /**
   * Send push notification to multiple users
   */
  async sendToUsers(
    userIds: number[],
    notification: {
      title: string;
      body: string;
      icon?: string;
      badge?: string;
      data?: any;
      url?: string;
    },
  ): Promise<{ sent: number; failed: number }> {
    let totalSent = 0;
    let totalFailed = 0;

    for (const userId of userIds) {
      const result = await this.sendToUser(userId, notification);
      totalSent += result.sent;
      totalFailed += result.failed;
    }

    return { sent: totalSent, failed: totalFailed };
  }

  /**
   * Send push notification for a notification entity
   */
  async sendForNotification(notification: Notification): Promise<void> {
    await this.sendToUser(notification.userId, {
      title: notification.title,
      body: notification.message,
      url: notification.actionUrl,
      data: notification.metadata,
    });
  }

  /**
   * Cleanup inactive subscriptions
   */
  async cleanupInactiveSubscriptions(): Promise<number> {
    const result = await this.pushSubscriptionRepo
      .createQueryBuilder()
      .delete()
      .where('active = :active', { active: false })
      .execute();

    this.logger.log(`Cleaned up ${result.affected} inactive push subscriptions`);
    return result.affected || 0;
  }
}
