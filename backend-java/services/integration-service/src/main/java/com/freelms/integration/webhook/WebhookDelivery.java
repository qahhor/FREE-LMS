package com.freelms.integration.webhook;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Smartup LMS - Webhook Delivery Log
 *
 * Records each webhook delivery attempt for debugging and retry logic.
 */
@Entity
@Table(name = "webhook_deliveries", indexes = {
    @Index(name = "idx_delivery_webhook", columnList = "webhookId"),
    @Index(name = "idx_delivery_event", columnList = "eventId"),
    @Index(name = "idx_delivery_status", columnList = "status"),
    @Index(name = "idx_delivery_created", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String deliveryId;

    @Column(nullable = false)
    private Long webhookId;

    @Column(nullable = false)
    private String eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    private Integer httpStatusCode;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

    @Column(columnDefinition = "TEXT")
    private String responseHeaders;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    @Builder.Default
    private Integer attemptNumber = 1;

    private Long durationMs;

    @CreationTimestamp
    private Instant createdAt;

    private Instant nextRetryAt;

    public enum DeliveryStatus {
        PENDING,
        SUCCESS,
        FAILED,
        RETRYING
    }

    @PrePersist
    public void prePersist() {
        if (deliveryId == null) {
            deliveryId = "del_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        }
    }

    public boolean isSuccess() {
        return status == DeliveryStatus.SUCCESS;
    }

    public boolean canRetry(int maxRetries) {
        return status == DeliveryStatus.FAILED && attemptNumber < maxRetries;
    }
}
