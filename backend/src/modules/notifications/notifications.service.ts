import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Notification, NotificationType } from './entities/notification.entity';
import { PushNotificationService } from './push-notification.service';

@Injectable()
export class NotificationsService {
  constructor(
    @InjectRepository(Notification)
    private notificationRepo: Repository<Notification>,
    private pushNotificationService: PushNotificationService,
  ) {}

  /**
   * Create notification and optionally send push
   */
  async create(data: {
    userId: number;
    type: NotificationType;
    title: string;
    message: string;
    actionUrl?: string;
    metadata?: Record<string, any>;
    sendPush?: boolean;
  }): Promise<Notification> {
    const notification = this.notificationRepo.create({
      userId: data.userId,
      type: data.type,
      title: data.title,
      message: data.message,
      actionUrl: data.actionUrl,
      metadata: data.metadata,
    });

    const saved = await this.notificationRepo.save(notification);

    // Send push notification if requested
    if (data.sendPush !== false) {
      try {
        await this.pushNotificationService.sendForNotification(saved);
        saved.sent = true;
        saved.sentAt = new Date();
        await this.notificationRepo.save(saved);
      } catch (error) {
        // Log but don't fail if push notification fails
        console.error('Failed to send push notification:', error);
      }
    }

    return saved;
  }

  /**
   * Get user notifications with pagination
   */
  async getUserNotifications(
    userId: number,
    page: number = 1,
    limit: number = 20,
  ): Promise<{ data: Notification[]; total: number; unreadCount: number }> {
    const [data, total] = await this.notificationRepo.findAndCount({
      where: { userId },
      order: { createdAt: 'DESC' },
      skip: (page - 1) * limit,
      take: limit,
    });

    const unreadCount = await this.notificationRepo.count({
      where: { userId, read: false },
    });

    return { data, total, unreadCount };
  }

  /**
   * Get unread notifications count
   */
  async getUnreadCount(userId: number): Promise<number> {
    return this.notificationRepo.count({
      where: { userId, read: false },
    });
  }

  /**
   * Mark notification as read
   */
  async markAsRead(notificationId: number, userId: number): Promise<Notification> {
    const notification = await this.notificationRepo.findOne({
      where: { id: notificationId, userId },
    });

    if (!notification) {
      throw new Error('Notification not found');
    }

    notification.read = true;
    notification.readAt = new Date();
    return this.notificationRepo.save(notification);
  }

  /**
   * Mark all notifications as read
   */
  async markAllAsRead(userId: number): Promise<void> {
    await this.notificationRepo
      .createQueryBuilder()
      .update()
      .set({ read: true, readAt: new Date() })
      .where('userId = :userId AND read = false', { userId })
      .execute();
  }

  /**
   * Delete notification
   */
  async delete(notificationId: number, userId: number): Promise<void> {
    await this.notificationRepo.delete({ id: notificationId, userId });
  }

  /**
   * Delete all read notifications
   */
  async deleteAllRead(userId: number): Promise<number> {
    const result = await this.notificationRepo
      .createQueryBuilder()
      .delete()
      .where('userId = :userId AND read = true', { userId })
      .execute();

    return result.affected || 0;
  }

  // Helper methods for creating specific notification types

  async notifyCourseEnrollment(userId: number, courseName: string, courseId: number): Promise<void> {
    await this.create({
      userId,
      type: NotificationType.COURSE_ENROLLMENT,
      title: 'Новый курс!',
      message: `Вы записались на курс "${courseName}"`,
      actionUrl: `/courses/${courseId}`,
      metadata: { courseId },
      sendPush: true,
    });
  }

  async notifyLessonCompleted(userId: number, lessonName: string, courseId: number): Promise<void> {
    await this.create({
      userId,
      type: NotificationType.LESSON_COMPLETED,
      title: 'Урок завершен!',
      message: `Вы завершили урок "${lessonName}"`,
      actionUrl: `/courses/${courseId}`,
      metadata: { courseId },
      sendPush: false, // Don't send push for every lesson
    });
  }

  async notifyCertificateEarned(userId: number, courseName: string, certificateId: number): Promise<void> {
    await this.create({
      userId,
      type: NotificationType.CERTIFICATE_EARNED,
      title: '🎉 Сертификат получен!',
      message: `Поздравляем! Вы получили сертификат за курс "${courseName}"`,
      actionUrl: `/certificates/${certificateId}`,
      metadata: { certificateId },
      sendPush: true,
    });
  }

  async notifyAchievementUnlocked(userId: number, achievementName: string): Promise<void> {
    await this.create({
      userId,
      type: NotificationType.ACHIEVEMENT_UNLOCKED,
      title: '🏆 Новое достижение!',
      message: `Вы разблокировали достижение "${achievementName}"`,
      actionUrl: '/profile/achievements',
      metadata: { achievementName },
      sendPush: true,
    });
  }

  async notifyWebinarReminder(userId: number, webinarTitle: string, webinarId: number, startsAt: Date): Promise<void> {
    const timeUntil = Math.floor((startsAt.getTime() - Date.now()) / 60000);
    await this.create({
      userId,
      type: NotificationType.WEBINAR_REMINDER,
      title: 'Напоминание о вебинаре',
      message: `Вебинар "${webinarTitle}" начнется через ${timeUntil} минут`,
      actionUrl: `/webinars/${webinarId}`,
      metadata: { webinarId, startsAt },
      sendPush: true,
    });
  }

  async notifySubscriptionExpiring(userId: number, daysLeft: number): Promise<void> {
    await this.create({
      userId,
      type: NotificationType.SUBSCRIPTION_EXPIRING,
      title: 'Подписка заканчивается',
      message: `Ваша подписка истекает через ${daysLeft} дней. Продлите подписку, чтобы сохранить доступ.`,
      actionUrl: '/subscriptions',
      metadata: { daysLeft },
      sendPush: true,
    });
  }

  async notifyCourseUpdate(userId: number, courseName: string, courseId: number, updateInfo: string): Promise<void> {
    await this.create({
      userId,
      type: NotificationType.COURSE_UPDATE,
      title: 'Обновление курса',
      message: `Курс "${courseName}" обновлен: ${updateInfo}`,
      actionUrl: `/courses/${courseId}`,
      metadata: { courseId, updateInfo },
      sendPush: false,
    });
  }
}
