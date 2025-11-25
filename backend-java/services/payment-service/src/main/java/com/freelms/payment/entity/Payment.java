package com.freelms.payment.entity;

import com.freelms.common.entity.BaseEntity;
import com.freelms.common.enums.PaymentGateway;
import com.freelms.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_user", columnList = "user_id"),
    @Index(name = "idx_payments_status", columnList = "status"),
    @Index(name = "idx_payments_external_id", columnList = "external_payment_id")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Payment extends BaseEntity {
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal amount;
    @Column(length = 3) @Builder.Default private String currency = "USD";
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private PaymentStatus status = PaymentStatus.PENDING;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private PaymentGateway gateway;
    @Column(name = "external_payment_id") private String externalPaymentId;
    @Column(length = 500) private String description;
    @Column(name = "subscription_id") private Long subscriptionId;
    @Column(name = "course_id") private Long courseId;
    @Column(name = "paid_at") private LocalDateTime paidAt;
    @Column(name = "failure_reason", length = 500) private String failureReason;

    public void markAsPaid(String externalId) { this.status = PaymentStatus.COMPLETED; this.externalPaymentId = externalId; this.paidAt = LocalDateTime.now(); }
    public void markAsFailed(String reason) { this.status = PaymentStatus.FAILED; this.failureReason = reason; }
}
