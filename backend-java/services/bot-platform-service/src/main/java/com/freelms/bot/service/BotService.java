package com.freelms.bot.service;

import com.freelms.bot.entity.BotConfiguration;
import com.freelms.bot.entity.BotMessage;
import com.freelms.bot.entity.BotUser;
import com.freelms.bot.repository.BotConfigurationRepository;
import com.freelms.bot.repository.BotMessageRepository;
import com.freelms.bot.repository.BotUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Core bot management service.
 */
@Service
public class BotService {

    private static final Logger log = LoggerFactory.getLogger(BotService.class);

    private final BotConfigurationRepository configRepository;
    private final BotUserRepository userRepository;
    private final BotMessageRepository messageRepository;

    public BotService(BotConfigurationRepository configRepository,
                      BotUserRepository userRepository,
                      BotMessageRepository messageRepository) {
        this.configRepository = configRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    // ==================== Bot Configuration ====================

    @Transactional
    public BotConfiguration createBotConfiguration(BotConfiguration config) {
        // Generate webhook secret
        if (config.getWebhookSecret() == null) {
            config.setWebhookSecret(UUID.randomUUID().toString());
        }

        return configRepository.save(config);
    }

    @Transactional
    public BotConfiguration updateBotConfiguration(UUID configId, BotConfiguration updates) {
        BotConfiguration config = getBotConfiguration(configId);

        if (updates.getName() != null) config.setName(updates.getName());
        if (updates.getDescription() != null) config.setDescription(updates.getDescription());
        if (updates.getApiToken() != null) config.setApiToken(updates.getApiToken());
        if (updates.getNotificationsEnabled() != null) config.setNotificationsEnabled(updates.getNotificationsEnabled());
        if (updates.getCommandsEnabled() != null) config.setCommandsEnabled(updates.getCommandsEnabled());
        if (updates.getRateLimit() != null) config.setRateLimit(updates.getRateLimit());

        return configRepository.save(config);
    }

    public BotConfiguration getBotConfiguration(UUID configId) {
        return configRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Bot configuration not found: " + configId));
    }

    public List<BotConfiguration> getBotConfigurations(Long organizationId) {
        if (organizationId != null) {
            return configRepository.findByOrganizationId(organizationId);
        }
        return configRepository.findAll();
    }

    public Optional<BotConfiguration> getBotForOrganization(Long orgId, BotConfiguration.BotPlatform platform) {
        return configRepository.findByOrganizationIdAndPlatform(orgId, platform);
    }

    @Transactional
    public void updateBotStatus(UUID configId, BotConfiguration.BotStatus status) {
        BotConfiguration config = getBotConfiguration(configId);
        config.setStatus(status);
        configRepository.save(config);
    }

    // ==================== User Management ====================

    @Transactional
    public BotUser registerUser(BotConfiguration.BotPlatform platform, String platformUserId,
                                 String chatId, String username, String displayName) {
        // Check if user already exists
        Optional<BotUser> existing = userRepository.findByPlatformAndPlatformUserId(platform, platformUserId);
        if (existing.isPresent()) {
            BotUser user = existing.get();
            user.setChatId(chatId);
            user.setUsername(username);
            user.setDisplayName(displayName);
            user.setLastInteractionAt(LocalDateTime.now());
            return userRepository.save(user);
        }

        // Create new user
        BotUser user = new BotUser();
        user.setPlatform(platform);
        user.setPlatformUserId(platformUserId);
        user.setChatId(chatId);
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setStatus(BotUser.UserStatus.PENDING_VERIFICATION);

        return userRepository.save(user);
    }

    @Transactional
    public BotUser linkToLmsUser(UUID botUserId, Long lmsUserId) {
        BotUser user = getBotUser(botUserId);
        user.setLmsUserId(lmsUserId);
        user.setVerified(true);
        user.setStatus(BotUser.UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    @Transactional
    public String generateVerificationCode(UUID botUserId) {
        BotUser user = getBotUser(botUserId);

        String code = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(code);
        user.setVerificationExpires(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        return code;
    }

    @Transactional
    public BotUser verifyUser(String code, Long lmsUserId) {
        BotUser user = userRepository.findByVerificationCode(code)
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification code"));

        user.setLmsUserId(lmsUserId);
        user.setVerified(true);
        user.setStatus(BotUser.UserStatus.ACTIVE);
        user.setVerificationCode(null);
        user.setVerificationExpires(null);

        return userRepository.save(user);
    }

    public BotUser getBotUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Bot user not found: " + userId));
    }

    public Optional<BotUser> findBotUser(BotConfiguration.BotPlatform platform, String platformUserId) {
        return userRepository.findByPlatformAndPlatformUserId(platform, platformUserId);
    }

    public List<BotUser> getLmsUserBots(Long lmsUserId) {
        return userRepository.findByLmsUserId(lmsUserId);
    }

    public List<BotUser> getActiveSubscribers(Long lmsUserId) {
        return userRepository.findActiveSubscribers(lmsUserId);
    }

    @Transactional
    public void updateUserPreferences(UUID userId, Map<String, Boolean> preferences) {
        BotUser user = getBotUser(userId);

        if (preferences.containsKey("notificationsEnabled")) {
            user.setNotificationsEnabled(preferences.get("notificationsEnabled"));
        }
        if (preferences.containsKey("courseUpdates")) {
            user.setCourseUpdates(preferences.get("courseUpdates"));
        }
        if (preferences.containsKey("assignmentReminders")) {
            user.setAssignmentReminders(preferences.get("assignmentReminders"));
        }
        if (preferences.containsKey("gradeNotifications")) {
            user.setGradeNotifications(preferences.get("gradeNotifications"));
        }
        if (preferences.containsKey("eventReminders")) {
            user.setEventReminders(preferences.get("eventReminders"));
        }

        userRepository.save(user);
    }

    // ==================== Message Handling ====================

    @Transactional
    public BotMessage logInboundMessage(BotUser user, BotConfiguration config,
                                         String text, BotMessage.MessageType type) {
        BotMessage message = new BotMessage();
        message.setBotUser(user);
        message.setBotConfig(config);
        message.setDirection(BotMessage.MessageDirection.INBOUND);
        message.setType(type);
        message.setText(text);
        message.setDeliveryStatus(BotMessage.DeliveryStatus.DELIVERED);

        // Update user activity
        user.setLastInteractionAt(LocalDateTime.now());
        user.setMessagesReceived(user.getMessagesReceived() + 1);
        userRepository.save(user);

        // Update bot activity
        config.setLastActivityAt(LocalDateTime.now());
        config.setMessageCount(config.getMessageCount() + 1);
        configRepository.save(config);

        return messageRepository.save(message);
    }

    @Transactional
    public BotMessage logOutboundMessage(BotUser user, BotConfiguration config,
                                          String text, BotMessage.NotificationType notificationType) {
        BotMessage message = new BotMessage();
        message.setBotUser(user);
        message.setBotConfig(config);
        message.setDirection(BotMessage.MessageDirection.OUTBOUND);
        message.setType(BotMessage.MessageType.TEXT);
        message.setText(text);
        message.setNotificationType(notificationType);
        message.setDeliveryStatus(BotMessage.DeliveryStatus.PENDING);

        return messageRepository.save(message);
    }

    @Transactional
    public void updateMessageStatus(UUID messageId, BotMessage.DeliveryStatus status, String errorMessage) {
        BotMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.setDeliveryStatus(status);
        if (status == BotMessage.DeliveryStatus.DELIVERED) {
            message.setDeliveredAt(LocalDateTime.now());
        }
        if (errorMessage != null) {
            message.setErrorMessage(errorMessage);
        }

        messageRepository.save(message);
    }

    // ==================== Statistics ====================

    public Map<String, Object> getBotStats(UUID configId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Map<String, Object> stats = new HashMap<>();

        BotConfiguration config = getBotConfiguration(configId);
        stats.put("configId", configId);
        stats.put("platform", config.getPlatform());
        stats.put("status", config.getStatus());
        stats.put("totalMessages", config.getMessageCount());

        Long recentMessages = messageRepository.countMessages(configId, since);
        stats.put("recentMessages", recentMessages);

        List<Object[]> byType = messageRepository.countByNotificationType(since);
        Map<String, Long> notificationStats = new HashMap<>();
        byType.forEach(row -> {
            if (row[0] != null) {
                notificationStats.put(row[0].toString(), (Long) row[1]);
            }
        });
        stats.put("byNotificationType", notificationStats);

        return stats;
    }
}
