package com.freelms.notification.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "push_subscriptions", indexes = {
    @Index(name = "idx_push_subscriptions_user", columnList = "user_id"),
    @Index(name = "idx_push_subscriptions_endpoint", columnList = "endpoint")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PushSubscription extends BaseEntity {
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(nullable = false, length = 1000) private String endpoint;
    @Column(name = "p256dh_key", nullable = false, length = 500) private String p256dhKey;
    @Column(name = "auth_key", nullable = false, length = 500) private String authKey;
    @Column(name = "device_info", length = 500) private String deviceInfo;
    @Column(name = "is_active") @Builder.Default private boolean isActive = true;
}
