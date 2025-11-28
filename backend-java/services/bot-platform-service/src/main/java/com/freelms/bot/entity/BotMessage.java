package com.freelms.bot.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Message log for bot communications.
 */
@Entity
@Table(name = "bot_messages", indexes = {
    @Index(name = "idx_bot_message_user", columnList = "bot_user_id"),
    @Index(name = "idx_bot_message_timestamp", columnList = "timestamp")
})
@EntityListeners(AuditingEntityListener.class)
public class BotMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_user_id")
    private BotUser botUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_config_id")
    private BotConfiguration botConfig;

    // Message direction
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageDirection direction;

    // Message type
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.TEXT;

    // Content
    @Column(length = 4000)
    private String text;

    @Column(length = 2000)
    private String mediaUrl;

    private String mediaType;

    // Interactive elements
    @Column(length = 4000)
    private String buttons;  // JSON array of buttons

    @Column(length = 4000)
    private String quickReplies;  // JSON array

    // Platform-specific message ID
    private String platformMessageId;
    private String threadId;
    private String replyToMessageId;

    // Command handling
    private String command;
    private String commandArgs;

    // Callback data
    private String callbackData;
    private String callbackAction;

    // Notification context
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    private Long relatedEntityId;
    private String relatedEntityType;

    // Delivery status
    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;

    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    private String errorMessage;

    @CreatedDate
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public enum MessageDirection {
        INBOUND,
        OUTBOUND
    }

    public enum MessageType {
        TEXT,
        IMAGE,
        VIDEO,
        AUDIO,
        DOCUMENT,
        LOCATION,
        CONTACT,
        STICKER,
        INTERACTIVE,
        TEMPLATE,
        COMMAND,
        CALLBACK
    }

    public enum NotificationType {
        COURSE_UPDATE,
        ASSIGNMENT_DUE,
        ASSIGNMENT_GRADED,
        GRADE_POSTED,
        EVENT_REMINDER,
        ENROLLMENT_CONFIRMED,
        CERTIFICATE_READY,
        MESSAGE_RECEIVED,
        ANNOUNCEMENT,
        SYSTEM
    }

    public enum DeliveryStatus {
        PENDING,
        SENT,
        DELIVERED,
        READ,
        FAILED,
        BLOCKED
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public BotUser getBotUser() { return botUser; }
    public void setBotUser(BotUser botUser) { this.botUser = botUser; }

    public BotConfiguration getBotConfig() { return botConfig; }
    public void setBotConfig(BotConfiguration botConfig) { this.botConfig = botConfig; }

    public MessageDirection getDirection() { return direction; }
    public void setDirection(MessageDirection direction) { this.direction = direction; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getButtons() { return buttons; }
    public void setButtons(String buttons) { this.buttons = buttons; }

    public String getQuickReplies() { return quickReplies; }
    public void setQuickReplies(String quickReplies) { this.quickReplies = quickReplies; }

    public String getPlatformMessageId() { return platformMessageId; }
    public void setPlatformMessageId(String platformMessageId) { this.platformMessageId = platformMessageId; }

    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }

    public String getReplyToMessageId() { return replyToMessageId; }
    public void setReplyToMessageId(String replyToMessageId) { this.replyToMessageId = replyToMessageId; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public String getCommandArgs() { return commandArgs; }
    public void setCommandArgs(String commandArgs) { this.commandArgs = commandArgs; }

    public String getCallbackData() { return callbackData; }
    public void setCallbackData(String callbackData) { this.callbackData = callbackData; }

    public String getCallbackAction() { return callbackAction; }
    public void setCallbackAction(String callbackAction) { this.callbackAction = callbackAction; }

    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }

    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }

    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }

    public DeliveryStatus getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(DeliveryStatus deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
