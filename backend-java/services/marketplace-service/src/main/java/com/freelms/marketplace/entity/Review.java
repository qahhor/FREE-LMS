package com.freelms.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Smartup LMS - Marketplace Review Entity
 */
@Entity
@Table(name = "marketplace_reviews",
       uniqueConstraints = @UniqueConstraint(columnNames = {"item_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private MarketplaceItem item;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_avatar_url")
    private String userAvatarUrl;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "organization_name")
    private String organizationName;

    // Rating
    @Column(nullable = false)
    private Integer rating; // 1-5

    @Column(name = "rating_usability")
    private Integer ratingUsability;

    @Column(name = "rating_features")
    private Integer ratingFeatures;

    @Column(name = "rating_support")
    private Integer ratingSupport;

    @Column(name = "rating_value")
    private Integer ratingValue;

    // Review content
    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ElementCollection
    @CollectionTable(name = "review_pros", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "pro")
    private List<String> pros = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "review_cons", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "con")
    private List<String> cons = new ArrayList<>();

    // Version reviewed
    @Column(name = "version_reviewed")
    private String versionReviewed;

    @Column(name = "usage_duration")
    private String usageDuration; // "< 1 month", "1-6 months", "6-12 months", "> 1 year"

    // Moderation
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(name = "moderated_by")
    private Long moderatedBy;

    @Column(name = "moderated_at")
    private Instant moderatedAt;

    @Column(name = "moderation_note")
    private String moderationNote;

    // Verification
    @Column(name = "verified_purchase")
    private boolean verifiedPurchase;

    @Column(name = "verified_usage")
    private boolean verifiedUsage;

    // Engagement
    @Column(name = "helpful_count")
    private Long helpfulCount = 0L;

    @Column(name = "not_helpful_count")
    private Long notHelpfulCount = 0L;

    @Column(name = "report_count")
    private Integer reportCount = 0;

    // Publisher response
    @Column(name = "publisher_response", columnDefinition = "TEXT")
    private String publisherResponse;

    @Column(name = "publisher_response_at")
    private Instant publisherResponseAt;

    // Flags
    @Column(name = "featured")
    private boolean featured;

    @Column(name = "pinned")
    private boolean pinned;

    @Column(name = "edited")
    private boolean edited;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum ReviewStatus {
        PENDING,      // Waiting for moderation
        APPROVED,     // Visible to all
        REJECTED,     // Not approved
        FLAGGED,      // Needs review
        HIDDEN        // Hidden by admin
    }

    // Helper methods
    public double getHelpfulPercentage() {
        long total = helpfulCount + notHelpfulCount;
        if (total == 0) return 0;
        return (helpfulCount * 100.0) / total;
    }

    public void markHelpful() {
        this.helpfulCount++;
    }

    public void markNotHelpful() {
        this.notHelpfulCount++;
    }

    public void report() {
        this.reportCount++;
        if (this.reportCount >= 5 && this.status == ReviewStatus.APPROVED) {
            this.status = ReviewStatus.FLAGGED;
        }
    }
}
