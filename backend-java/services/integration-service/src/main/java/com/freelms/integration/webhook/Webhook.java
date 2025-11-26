package com.freelms.integration.webhook;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Smartup LMS - Webhook Configuration Entity
 *
 * Defines outgoing webhooks for real-time event notifications to external systems.
 */
@Entity
@Table(name = "webhooks", indexes = {
    @Index(name = "idx_webhook_org", columnList = "organizationId"),
    @Index(name = "idx_webhook_active", columnList = "active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Webhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String webhookId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private Long organizationId;

    @Column(nullable = false)
    private Long createdBy;

    @Column(nullable = false)
    private String secret;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "webhook_events", joinColumns = @JoinColumn(name = "webhook_id"))
    @Column(name = "event_type")
    @Builder.Default
    private Set<String> events = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ContentType contentType = ContentType.JSON;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuthType authType = AuthType.HMAC_SHA256;

    private String authHeader;
    private String authValue;

    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 3;

    @Column(nullable = false)
    @Builder.Default
    private Integer timeoutSeconds = 30;

    @Column(nullable = false)
    @Builder.Default
    private Long successCount = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Long failureCount = 0L;

    private Instant lastTriggeredAt;
    private Instant lastSuccessAt;
    private String lastError;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public enum ContentType {
        JSON,
        FORM_URLENCODED,
        XML
    }

    public enum AuthType {
        NONE,
        HMAC_SHA256,
        HMAC_SHA512,
        BEARER_TOKEN,
        BASIC_AUTH,
        API_KEY
    }

    /**
     * Available webhook events
     */
    public static class Events {
        // User events
        public static final String USER_CREATED = "user.created";
        public static final String USER_UPDATED = "user.updated";
        public static final String USER_DELETED = "user.deleted";
        public static final String USER_ACTIVATED = "user.activated";
        public static final String USER_DEACTIVATED = "user.deactivated";

        // Course events
        public static final String COURSE_CREATED = "course.created";
        public static final String COURSE_UPDATED = "course.updated";
        public static final String COURSE_PUBLISHED = "course.published";
        public static final String COURSE_ARCHIVED = "course.archived";

        // Enrollment events
        public static final String ENROLLMENT_CREATED = "enrollment.created";
        public static final String ENROLLMENT_COMPLETED = "enrollment.completed";
        public static final String ENROLLMENT_EXPIRED = "enrollment.expired";
        public static final String ENROLLMENT_CANCELLED = "enrollment.cancelled";

        // Progress events
        public static final String PROGRESS_UPDATED = "progress.updated";
        public static final String LESSON_COMPLETED = "lesson.completed";
        public static final String QUIZ_COMPLETED = "quiz.completed";
        public static final String QUIZ_PASSED = "quiz.passed";
        public static final String QUIZ_FAILED = "quiz.failed";

        // Certificate events
        public static final String CERTIFICATE_ISSUED = "certificate.issued";
        public static final String CERTIFICATE_EXPIRED = "certificate.expired";

        // Achievement events
        public static final String ACHIEVEMENT_UNLOCKED = "achievement.unlocked";
        public static final String BADGE_EARNED = "badge.earned";

        // Payment events
        public static final String PAYMENT_COMPLETED = "payment.completed";
        public static final String PAYMENT_FAILED = "payment.failed";
        public static final String SUBSCRIPTION_CREATED = "subscription.created";
        public static final String SUBSCRIPTION_CANCELLED = "subscription.cancelled";

        // Compliance events
        public static final String TRAINING_DUE = "training.due";
        public static final String TRAINING_OVERDUE = "training.overdue";
        public static final String COMPLIANCE_COMPLETED = "compliance.completed";
    }

    @PrePersist
    public void prePersist() {
        if (webhookId == null) {
            webhookId = "wh_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        }
        if (secret == null) {
            secret = "whsec_" + UUID.randomUUID().toString().replace("-", "");
        }
    }
}
