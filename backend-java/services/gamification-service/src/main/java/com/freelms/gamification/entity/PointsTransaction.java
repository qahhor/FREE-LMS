package com.freelms.gamification.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "points_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsTransaction extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency_type", nullable = false)
    @Builder.Default
    private CurrencyType currencyType = CurrencyType.POINTS;

    @Column(name = "source_type")
    private String sourceType; // "course", "quiz", "achievement", "challenge", "bonus"

    @Column(name = "source_id")
    private Long sourceId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "balance_after")
    private Long balanceAfter;

    @Column(name = "transaction_at", nullable = false)
    @Builder.Default
    private LocalDateTime transactionAt = LocalDateTime.now();

    public enum TransactionType {
        EARNED,
        SPENT,
        BONUS,
        ADJUSTMENT,
        EXPIRED
    }

    public enum CurrencyType {
        POINTS,
        COINS,
        EXPERIENCE
    }
}
