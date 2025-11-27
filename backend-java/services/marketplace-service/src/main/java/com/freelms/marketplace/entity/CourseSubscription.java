package com.freelms.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Smartup LMS - Course Subscription Entity
 *
 * Tracks purchased/subscribed marketplace courses per organization.
 */
@Entity
@Table(name = "course_subscriptions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "course_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private MarketplaceCourse course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.PENDING;

    // Purchase details
    @Column(name = "purchased_by")
    private Long purchasedBy;

    @Column(name = "purchase_price", precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "purchase_currency")
    private String purchaseCurrency = "UZS";

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "invoice_id")
    private String invoiceId;

    // Subscription period
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type")
    private SubscriptionType subscriptionType = SubscriptionType.PERPETUAL;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "auto_renew")
    private boolean autoRenew;

    // License limits
    @Column(name = "max_enrollments")
    private Integer maxEnrollments;

    @Column(name = "current_enrollments")
    private Integer currentEnrollments = 0;

    // Content access
    @Column(name = "course_copy_id")
    private Long courseCopyId; // ID of the copied course in the organization

    @Column(name = "content_imported")
    private boolean contentImported;

    @Column(name = "imported_at")
    private Instant importedAt;

    // Usage stats
    @Column(name = "total_enrollments")
    private Long totalEnrollments = 0L;

    @Column(name = "total_completions")
    private Long totalCompletions = 0L;

    @Column(name = "average_rating")
    private Double averageRating;

    // Renewal history
    @Column(name = "renewal_count")
    private Integer renewalCount = 0;

    @Column(name = "last_renewed_at")
    private Instant lastRenewedAt;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum SubscriptionStatus {
        PENDING,        // Waiting for payment
        ACTIVE,         // Active subscription
        EXPIRED,        // Subscription ended
        CANCELLED,      // User cancelled
        SUSPENDED,      // Temporarily suspended
        REFUNDED        // Refunded
    }

    public enum SubscriptionType {
        PERPETUAL,      // One-time purchase, forever access
        MONTHLY,        // Monthly subscription
        YEARLY,         // Yearly subscription
        CUSTOM          // Custom duration
    }

    // Helper methods
    public boolean isActive() {
        if (status != SubscriptionStatus.ACTIVE) {
            return false;
        }
        if (expiresAt != null && Instant.now().isAfter(expiresAt)) {
            return false;
        }
        return true;
    }

    public boolean hasAvailableSeats() {
        if (maxEnrollments == null) {
            return true; // Unlimited
        }
        return currentEnrollments < maxEnrollments;
    }

    public void incrementEnrollments() {
        this.currentEnrollments++;
        this.totalEnrollments++;
    }

    public void decrementEnrollments() {
        if (this.currentEnrollments > 0) {
            this.currentEnrollments--;
        }
    }

    public void recordCompletion() {
        this.totalCompletions++;
    }

    public int getRemainingSeats() {
        if (maxEnrollments == null) {
            return Integer.MAX_VALUE;
        }
        return maxEnrollments - currentEnrollments;
    }
}
