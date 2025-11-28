package com.freelms.bot.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bot configuration for different messaging platforms.
 */
@Entity
@Table(name = "bot_configurations")
@EntityListeners(AuditingEntityListener.class)
public class BotConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BotPlatform platform;

    // Platform-specific credentials
    @Column(length = 500)
    private String apiToken;

    @Column(length = 500)
    private String apiSecret;

    private String appId;
    private String clientId;
    private String clientSecret;

    // Webhook configuration
    private String webhookUrl;
    private String webhookSecret;
    private Boolean webhookVerified = false;

    // Bot identity
    private String botUsername;
    private String botDisplayName;
    private String botAvatarUrl;

    // Organization mapping
    private Long organizationId;

    // Feature flags
    private Boolean notificationsEnabled = true;
    private Boolean commandsEnabled = true;
    private Boolean interactiveEnabled = true;
    private Boolean groupsEnabled = false;

    // Rate limiting
    private Integer rateLimit = 30;  // messages per minute
    private Integer dailyLimit = 1000;

    // Status
    @Enumerated(EnumType.STRING)
    private BotStatus status = BotStatus.ACTIVE;

    private LocalDateTime lastActivityAt;
    private Long messageCount = 0L;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Long createdBy;

    public enum BotPlatform {
        TELEGRAM,
        SLACK,
        WHATSAPP,
        DISCORD,
        MICROSOFT_TEAMS,
        FACEBOOK_MESSENGER,
        VIBER,
        LINE
    }

    public enum BotStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        RATE_LIMITED,
        ERROR
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BotPlatform getPlatform() { return platform; }
    public void setPlatform(BotPlatform platform) { this.platform = platform; }

    public String getApiToken() { return apiToken; }
    public void setApiToken(String apiToken) { this.apiToken = apiToken; }

    public String getApiSecret() { return apiSecret; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }

    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }

    public Boolean getWebhookVerified() { return webhookVerified; }
    public void setWebhookVerified(Boolean webhookVerified) { this.webhookVerified = webhookVerified; }

    public String getBotUsername() { return botUsername; }
    public void setBotUsername(String botUsername) { this.botUsername = botUsername; }

    public String getBotDisplayName() { return botDisplayName; }
    public void setBotDisplayName(String botDisplayName) { this.botDisplayName = botDisplayName; }

    public String getBotAvatarUrl() { return botAvatarUrl; }
    public void setBotAvatarUrl(String botAvatarUrl) { this.botAvatarUrl = botAvatarUrl; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Boolean getNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(Boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public Boolean getCommandsEnabled() { return commandsEnabled; }
    public void setCommandsEnabled(Boolean commandsEnabled) { this.commandsEnabled = commandsEnabled; }

    public Boolean getInteractiveEnabled() { return interactiveEnabled; }
    public void setInteractiveEnabled(Boolean interactiveEnabled) { this.interactiveEnabled = interactiveEnabled; }

    public Boolean getGroupsEnabled() { return groupsEnabled; }
    public void setGroupsEnabled(Boolean groupsEnabled) { this.groupsEnabled = groupsEnabled; }

    public Integer getRateLimit() { return rateLimit; }
    public void setRateLimit(Integer rateLimit) { this.rateLimit = rateLimit; }

    public Integer getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(Integer dailyLimit) { this.dailyLimit = dailyLimit; }

    public BotStatus getStatus() { return status; }
    public void setStatus(BotStatus status) { this.status = status; }

    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    public Long getMessageCount() { return messageCount; }
    public void setMessageCount(Long messageCount) { this.messageCount = messageCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}
