package com.freelms.bot.service;

import com.freelms.bot.entity.BotConfiguration;
import com.freelms.bot.entity.BotMessage;
import com.freelms.bot.entity.BotUser;
import com.freelms.bot.repository.BotUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for sending notifications to users via messaging platforms.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final BotService botService;
    private final BotUserRepository userRepository;
    private final TelegramService telegramService;
    private final SlackService slackService;

    public NotificationService(BotService botService,
                                BotUserRepository userRepository,
                                TelegramService telegramService,
                                SlackService slackService) {
        this.botService = botService;
        this.userRepository = userRepository;
        this.telegramService = telegramService;
        this.slackService = slackService;
    }

    /**
     * Send notification to user across all connected platforms.
     */
    @Async
    public void notifyUser(Long lmsUserId, BotMessage.NotificationType type, String message) {
        List<BotUser> subscribers = botService.getActiveSubscribers(lmsUserId);

        for (BotUser user : subscribers) {
            if (shouldSendNotification(user, type)) {
                sendToUser(user, type, message, null);
            }
        }
    }

    /**
     * Send notification to specific platform.
     */
    @Async
    public void notifyUserOnPlatform(Long lmsUserId, BotConfiguration.BotPlatform platform,
                                      BotMessage.NotificationType type, String message) {
        userRepository.findNotifiableUser(lmsUserId, platform)
                .ifPresent(user -> sendToUser(user, type, message, null));
    }

    /**
     * Send notification with interactive buttons.
     */
    @Async
    public void notifyUserWithActions(Long lmsUserId, BotMessage.NotificationType type,
                                       String message, List<Map<String, String>> buttons) {
        List<BotUser> subscribers = botService.getActiveSubscribers(lmsUserId);

        for (BotUser user : subscribers) {
            if (shouldSendNotification(user, type)) {
                sendToUser(user, type, message, buttons);
            }
        }
    }

    /**
     * Broadcast notification to all users of an organization.
     */
    @Async
    public void broadcastToOrganization(Long organizationId, BotConfiguration.BotPlatform platform,
                                         BotMessage.NotificationType type, String message) {
        // In real implementation, query users by organization
        log.info("Broadcasting to org {} on {}: {}", organizationId, platform, message);
    }

    /**
     * Send course-related notification.
     */
    public void notifyCourseUpdate(Long lmsUserId, Long courseId, String courseName, String updateMessage) {
        String message = String.format("📚 *%s*\n\n%s", courseName, updateMessage);
        notifyUser(lmsUserId, BotMessage.NotificationType.COURSE_UPDATE, message);
    }

    /**
     * Send assignment reminder.
     */
    public void notifyAssignmentDue(Long lmsUserId, String assignmentName, String courseName, String dueDate) {
        String message = String.format(
                "⏰ *Assignment Due Soon*\n\n" +
                "*%s*\nCourse: %s\nDue: %s\n\n" +
                "Don't forget to submit!",
                assignmentName, courseName, dueDate
        );
        notifyUser(lmsUserId, BotMessage.NotificationType.ASSIGNMENT_DUE, message);
    }

    /**
     * Send grade notification.
     */
    public void notifyGradePosted(Long lmsUserId, String itemName, String courseName,
                                   String grade, String score) {
        String message = String.format(
                "📊 *Grade Posted*\n\n" +
                "*%s*\nCourse: %s\n\n" +
                "Grade: %s\nScore: %s",
                itemName, courseName, grade, score
        );
        notifyUser(lmsUserId, BotMessage.NotificationType.GRADE_POSTED, message);
    }

    /**
     * Send event reminder.
     */
    public void notifyEventReminder(Long lmsUserId, String eventName, String dateTime, String location) {
        String message = String.format(
                "📅 *Event Reminder*\n\n" +
                "*%s*\n" +
                "When: %s\n" +
                "Where: %s",
                eventName, dateTime, location != null ? location : "Online"
        );
        notifyUser(lmsUserId, BotMessage.NotificationType.EVENT_REMINDER, message);
    }

    /**
     * Send certificate notification.
     */
    public void notifyCertificateReady(Long lmsUserId, String courseName, String certificateUrl) {
        String message = String.format(
                "🎓 *Congratulations!*\n\n" +
                "Your certificate for *%s* is ready!\n\n" +
                "Download it from your profile or click below.",
                courseName
        );

        List<Map<String, String>> buttons = List.of(
                Map.of("text", "View Certificate", "url", certificateUrl)
        );

        notifyUserWithActions(lmsUserId, BotMessage.NotificationType.CERTIFICATE_READY, message, buttons);
    }

    private void sendToUser(BotUser user, BotMessage.NotificationType type,
                            String message, List<Map<String, String>> buttons) {
        try {
            switch (user.getPlatform()) {
                case TELEGRAM -> telegramService.sendMessage(user.getChatId(), message, buttons);
                case SLACK -> slackService.sendMessage(user.getChatId(), user.getChannelId(), message, buttons);
                default -> log.warn("Unsupported platform: {}", user.getPlatform());
            }

            // Log successful send
            user.setMessagesSent(user.getMessagesSent() + 1);

        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}", user.getId(), e.getMessage());
        }
    }

    private boolean shouldSendNotification(BotUser user, BotMessage.NotificationType type) {
        if (!user.getNotificationsEnabled()) return false;

        return switch (type) {
            case COURSE_UPDATE -> user.getCourseUpdates();
            case ASSIGNMENT_DUE, ASSIGNMENT_GRADED -> user.getAssignmentReminders();
            case GRADE_POSTED -> user.getGradeNotifications();
            case EVENT_REMINDER -> user.getEventReminders();
            case MESSAGE_RECEIVED -> user.getMessageNotifications();
            default -> true;
        };
    }
}
