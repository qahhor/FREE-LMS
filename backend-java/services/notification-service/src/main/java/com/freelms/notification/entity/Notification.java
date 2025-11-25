package com.freelms.notification.entity;

import com.freelms.common.entity.BaseEntity;
import com.freelms.common.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_user", columnList = "user_id"),
    @Index(name = "idx_notifications_read", columnList = "is_read"),
    @Index(name = "idx_notifications_type", columnList = "type")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Notification extends BaseEntity {
    @Column(name = "user_id", nullable = false) private Long userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private NotificationType type;
    @Column(nullable = false, length = 255) private String title;
    @Column(columnDefinition = "TEXT") private String message;
    @Column(name = "is_read") @Builder.Default private boolean isRead = false;
    @Column(name = "read_at") private LocalDateTime readAt;
    @Column(name = "action_url", length = 500) private String actionUrl;
    @Column(name = "entity_id") private Long entityId;
    @Column(name = "entity_type", length = 50) private String entityType;

    public void markAsRead() { this.isRead = true; this.readAt = LocalDateTime.now(); }
}
