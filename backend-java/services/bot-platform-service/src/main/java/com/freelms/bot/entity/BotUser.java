package com.freelms.bot.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Links LMS users with their messaging platform accounts.
 */
@Entity
@Table(name = "bot_users", indexes = {
    @Index(name = "idx_bot_user_platform", columnList = "platform, platform_user_id"),
    @Index(name = "idx_bot_user_lms", columnList = "lms_user_id")
})
@EntityListeners(AuditingEntityListener.class)
public class BotUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // LMS user reference
    @Column(name = "lms_user_id")
    private Long lmsUserId;

    // Platform identification
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BotConfiguration.BotPlatform platform;

    @Column(name = "platform_user_id", nullable = false)
    private String platformUserId;

    // User info from platform
    private String username;
    private String displayName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private String locale;
    private String timezone;

    // Chat reference
    private String chatId;
    private String channelId;

    // Preferences
    private Boolean notificationsEnabled = true;
    private Boolean courseUpdates = true;
    private Boolean assignmentReminders = true;
    private Boolean gradeNotifications = true;
    private Boolean eventReminders = true;
    private Boolean messageNotifications = true;

    // Quiet hours
    private String quietHoursStart;  // HH:mm format
    private String quietHoursEnd;
    private String quietDays;  // Comma-separated: SAT,SUN

    // Status
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    private Boolean verified = false;
    private String verificationCode;
    private LocalDateTime verificationExpires;

    // Activity tracking
    private LocalDateTime lastInteractionAt;
    private Long messagesSent = 0L;
    private Long messagesReceived = 0L;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum UserStatus {
        ACTIVE,
        BLOCKED,
        UNSUBSCRIBED,
        PENDING_VERIFICATION
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Long getLmsUserId() { return lmsUserId; }
    public void setLmsUserId(Long lmsUserId) { this.lmsUserId = lmsUserId; }

    public BotConfiguration.BotPlatform getPlatform() { return platform; }
    public void setPlatform(BotConfiguration.BotPlatform platform) { this.platform = platform; }

    public String getPlatformUserId() { return platformUserId; }
    public void setPlatformUserId(String platformUserId) { this.platformUserId = platformUserId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }

    public Boolean getNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(Boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public Boolean getCourseUpdates() { return courseUpdates; }
    public void setCourseUpdates(Boolean courseUpdates) { this.courseUpdates = courseUpdates; }

    public Boolean getAssignmentReminders() { return assignmentReminders; }
    public void setAssignmentReminders(Boolean assignmentReminders) { this.assignmentReminders = assignmentReminders; }

    public Boolean getGradeNotifications() { return gradeNotifications; }
    public void setGradeNotifications(Boolean gradeNotifications) { this.gradeNotifications = gradeNotifications; }

    public Boolean getEventReminders() { return eventReminders; }
    public void setEventReminders(Boolean eventReminders) { this.eventReminders = eventReminders; }

    public Boolean getMessageNotifications() { return messageNotifications; }
    public void setMessageNotifications(Boolean messageNotifications) { this.messageNotifications = messageNotifications; }

    public String getQuietHoursStart() { return quietHoursStart; }
    public void setQuietHoursStart(String quietHoursStart) { this.quietHoursStart = quietHoursStart; }

    public String getQuietHoursEnd() { return quietHoursEnd; }
    public void setQuietHoursEnd(String quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }

    public String getQuietDays() { return quietDays; }
    public void setQuietDays(String quietDays) { this.quietDays = quietDays; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public LocalDateTime getVerificationExpires() { return verificationExpires; }
    public void setVerificationExpires(LocalDateTime verificationExpires) { this.verificationExpires = verificationExpires; }

    public LocalDateTime getLastInteractionAt() { return lastInteractionAt; }
    public void setLastInteractionAt(LocalDateTime lastInteractionAt) { this.lastInteractionAt = lastInteractionAt; }

    public Long getMessagesSent() { return messagesSent; }
    public void setMessagesSent(Long messagesSent) { this.messagesSent = messagesSent; }

    public Long getMessagesReceived() { return messagesReceived; }
    public void setMessagesReceived(Long messagesReceived) { this.messagesReceived = messagesReceived; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
