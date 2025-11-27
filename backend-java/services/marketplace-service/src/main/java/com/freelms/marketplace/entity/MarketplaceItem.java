package com.freelms.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Smartup LMS - Base Marketplace Item Entity
 *
 * Abstract base class for all marketplace items (modules, courses, templates).
 */
@Entity
@Table(name = "marketplace_items")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "item_type", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketplaceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(name = "name_uz")
    private String nameUz;

    @Column(name = "name_ru")
    private String nameRu;

    @Column(name = "name_en")
    private String nameEn;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_uz", columnDefinition = "TEXT")
    private String descriptionUz;

    @Column(name = "description_ru", columnDefinition = "TEXT")
    private String descriptionRu;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "short_description")
    private String shortDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", insertable = false, updatable = false)
    private ItemType type;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "banner_url")
    private String bannerUrl;

    @ElementCollection
    @CollectionTable(name = "marketplace_item_screenshots", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "screenshot_url")
    private List<String> screenshots = new ArrayList<>();

    @Column(name = "video_url")
    private String videoUrl;

    // Pricing
    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_model")
    private PricingModel pricingModel = PricingModel.FREE;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "price_currency")
    private String priceCurrency = "UZS";

    @Column(name = "monthly_price", precision = 12, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(name = "yearly_price", precision = 12, scale = 2)
    private BigDecimal yearlyPrice;

    @Column(name = "trial_days")
    private Integer trialDays;

    // Publisher/Developer
    @Column(name = "publisher_id")
    private Long publisherId;

    @Column(name = "publisher_name")
    private String publisherName;

    @Column(name = "publisher_url")
    private String publisherUrl;

    @Column(name = "publisher_verified")
    private boolean publisherVerified;

    // Versioning
    @Column(nullable = false)
    private String version = "1.0.0";

    @Column(name = "min_platform_version")
    private String minPlatformVersion;

    @Column(name = "changelog", columnDefinition = "TEXT")
    private String changelog;

    // Categories and Tags
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "marketplace_item_categories",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "marketplace_item_tags", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    // Stats
    @Column(name = "download_count")
    private Long downloadCount = 0L;

    @Column(name = "install_count")
    private Long installCount = 0L;

    @Column(name = "active_installs")
    private Long activeInstalls = 0L;

    @Column(name = "rating_average")
    private Double ratingAverage = 0.0;

    @Column(name = "rating_count")
    private Long ratingCount = 0L;

    @Column(name = "review_count")
    private Long reviewCount = 0L;

    // Status
    @Enumerated(EnumType.STRING)
    private ItemStatus status = ItemStatus.DRAFT;

    @Column(name = "featured")
    private boolean featured;

    @Column(name = "featured_order")
    private Integer featuredOrder;

    @Column(name = "editor_choice")
    private boolean editorChoice;

    // Compatibility
    @ElementCollection
    @CollectionTable(name = "marketplace_item_requirements", joinColumns = @JoinColumn(name = "item_id"))
    @MapKeyColumn(name = "requirement_key")
    @Column(name = "requirement_value")
    private Map<String, String> requirements = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "marketplace_item_dependencies", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "dependency_slug")
    private Set<String> dependencies = new HashSet<>();

    // Support
    @Column(name = "documentation_url")
    private String documentationUrl;

    @Column(name = "support_url")
    private String supportUrl;

    @Column(name = "support_email")
    private String supportEmail;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    // Enums
    public enum ItemType {
        MODULE,          // Functional module/plugin
        COURSE,          // Ready-made course
        TEMPLATE,        // Course/lesson template
        INTEGRATION,     // Third-party integration
        THEME,           // UI theme
        WIDGET           // Dashboard widget
    }

    public enum PricingModel {
        FREE,            // Completely free
        FREEMIUM,        // Free with paid features
        ONE_TIME,        // One-time purchase
        SUBSCRIPTION,    // Monthly/yearly subscription
        PER_USER,        // Price per user
        CUSTOM           // Contact for pricing
    }

    public enum ItemStatus {
        DRAFT,           // Not yet submitted
        PENDING_REVIEW,  // Waiting for approval
        APPROVED,        // Approved and visible
        REJECTED,        // Rejected with feedback
        SUSPENDED,       // Temporarily disabled
        DEPRECATED,      // No longer maintained
        ARCHIVED         // Removed from marketplace
    }

    // Helper methods
    public void incrementDownloads() {
        this.downloadCount++;
    }

    public void incrementInstalls() {
        this.installCount++;
        this.activeInstalls++;
    }

    public void decrementActiveInstalls() {
        if (this.activeInstalls > 0) {
            this.activeInstalls--;
        }
    }

    public void updateRating(double newRating) {
        double totalRating = this.ratingAverage * this.ratingCount;
        this.ratingCount++;
        this.ratingAverage = (totalRating + newRating) / this.ratingCount;
    }

    public boolean isFree() {
        return this.pricingModel == PricingModel.FREE ||
               (this.price == null || this.price.compareTo(BigDecimal.ZERO) == 0);
    }

    public boolean isPublished() {
        return this.status == ItemStatus.APPROVED && this.publishedAt != null;
    }
}
