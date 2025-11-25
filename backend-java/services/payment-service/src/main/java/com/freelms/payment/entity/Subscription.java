package com.freelms.payment.entity;

import com.freelms.common.entity.BaseEntity;
import com.freelms.common.enums.SubscriptionStatus;
import com.freelms.common.enums.SubscriptionTier;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions", indexes = {
    @Index(name = "idx_subscriptions_user", columnList = "user_id"),
    @Index(name = "idx_subscriptions_status", columnList = "status")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Subscription extends BaseEntity {
    @Column(name = "user_id", nullable = false) private Long userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private SubscriptionTier tier = SubscriptionTier.FREE;
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private SubscriptionStatus status = SubscriptionStatus.ACTIVE;
    @Column(name = "stripe_subscription_id") private String stripeSubscriptionId;
    @Column(name = "stripe_customer_id") private String stripeCustomerId;
    @Column(precision = 10, scale = 2) private BigDecimal price;
    @Column(name = "billing_period") private String billingPeriod;
    @Column(name = "current_period_start") private LocalDateTime currentPeriodStart;
    @Column(name = "current_period_end") private LocalDateTime currentPeriodEnd;
    @Column(name = "cancelled_at") private LocalDateTime cancelledAt;

    public boolean isActive() { return status == SubscriptionStatus.ACTIVE; }
    public void cancel() { this.status = SubscriptionStatus.CANCELLED; this.cancelledAt = LocalDateTime.now(); }
}
