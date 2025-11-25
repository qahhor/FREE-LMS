import {
  Controller,
  Get,
  Post,
  Put,
  Delete,
  Body,
  Param,
  Query,
  UseGuards,
  ParseIntPipe,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth } from '@nestjs/swagger';
import { NotificationsService } from './notifications.service';
import { PushNotificationService } from './push-notification.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { User } from '../users/entities/user.entity';
import { RateLimit } from '../../common/guards/rate-limit.guard';

@ApiTags('notifications')
@Controller('notifications')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class NotificationsController {
  constructor(
    private notificationsService: NotificationsService,
    private pushNotificationService: PushNotificationService,
  ) {}

  @Get()
  @RateLimit(100, 60)
  @ApiOperation({ summary: 'Get user notifications' })
  @ApiResponse({ status: 200, description: 'Notifications list' })
  async getUserNotifications(
    @CurrentUser() user: User,
    @Query('page', ParseIntPipe) page: number = 1,
    @Query('limit', ParseIntPipe) limit: number = 20,
  ) {
    return this.notificationsService.getUserNotifications(user.id, page, limit);
  }

  @Get('unread-count')
  @RateLimit(200, 60)
  @ApiOperation({ summary: 'Get unread notifications count' })
  @ApiResponse({ status: 200, description: 'Unread count' })
  async getUnreadCount(@CurrentUser() user: User) {
    const count = await this.notificationsService.getUnreadCount(user.id);
    return { count };
  }

  @Put(':id/read')
  @RateLimit(50, 60)
  @ApiOperation({ summary: 'Mark notification as read' })
  @ApiResponse({ status: 200, description: 'Notification marked as read' })
  async markAsRead(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: User,
  ) {
    return this.notificationsService.markAsRead(id, user.id);
  }

  @Put('read-all')
  @HttpCode(HttpStatus.NO_CONTENT)
  @RateLimit(10, 60)
  @ApiOperation({ summary: 'Mark all notifications as read' })
  @ApiResponse({ status: 204, description: 'All notifications marked as read' })
  async markAllAsRead(@CurrentUser() user: User) {
    await this.notificationsService.markAllAsRead(user.id);
  }

  @Delete(':id')
  @HttpCode(HttpStatus.NO_CONTENT)
  @RateLimit(50, 60)
  @ApiOperation({ summary: 'Delete notification' })
  @ApiResponse({ status: 204, description: 'Notification deleted' })
  async deleteNotification(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: User,
  ) {
    await this.notificationsService.delete(id, user.id);
  }

  @Delete('read/all')
  @HttpCode(HttpStatus.OK)
  @RateLimit(10, 60)
  @ApiOperation({ summary: 'Delete all read notifications' })
  @ApiResponse({ status: 200, description: 'Read notifications deleted' })
  async deleteAllRead(@CurrentUser() user: User) {
    const count = await this.notificationsService.deleteAllRead(user.id);
    return { deleted: count };
  }

  // Push Subscription Endpoints

  @Post('push/subscribe')
  @RateLimit(10, 60)
  @ApiOperation({ summary: 'Subscribe to push notifications' })
  @ApiResponse({ status: 201, description: 'Subscribed successfully' })
  async subscribeToPush(
    @CurrentUser() user: User,
    @Body() subscription: {
      endpoint: string;
      keys: { p256dh: string; auth: string };
    },
  ) {
    const result = await this.pushNotificationService.subscribe(
      user.id,
      subscription,
      user.email, // or get from request headers
    );

    return {
      success: true,
      subscriptionId: result.id,
    };
  }

  @Post('push/unsubscribe')
  @RateLimit(10, 60)
  @ApiOperation({ summary: 'Unsubscribe from push notifications' })
  @ApiResponse({ status: 200, description: 'Unsubscribed successfully' })
  async unsubscribeFromPush(
    @CurrentUser() user: User,
    @Body() body: { endpoint: string },
  ) {
    const success = await this.pushNotificationService.unsubscribe(
      user.id,
      body.endpoint,
    );

    return { success };
  }

  @Get('push/subscriptions')
  @RateLimit(50, 60)
  @ApiOperation({ summary: 'Get user push subscriptions' })
  @ApiResponse({ status: 200, description: 'Push subscriptions list' })
  async getPushSubscriptions(@CurrentUser() user: User) {
    const subscriptions = await this.pushNotificationService.getUserSubscriptions(user.id);
    return {
      subscriptions: subscriptions.map(s => ({
        id: s.id,
        endpoint: s.endpoint,
        userAgent: s.userAgent,
        createdAt: s.createdAt,
        active: s.active,
      })),
    };
  }

  @Post('push/test')
  @RateLimit(5, 60)
  @ApiOperation({ summary: 'Send test push notification' })
  @ApiResponse({ status: 200, description: 'Test notification sent' })
  async sendTestPush(@CurrentUser() user: User) {
    const result = await this.pushNotificationService.sendToUser(user.id, {
      title: 'Тестовое уведомление',
      body: 'Это тестовое push уведомление от FREE LMS!',
      url: '/',
    });

    return {
      sent: result.sent,
      failed: result.failed,
      message: `Отправлено ${result.sent} уведомлений, ошибок: ${result.failed}`,
    };
  }
}
