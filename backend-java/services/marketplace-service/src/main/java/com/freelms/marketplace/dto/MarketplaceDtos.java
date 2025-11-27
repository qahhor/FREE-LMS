package com.freelms.marketplace.dto;

import com.freelms.marketplace.entity.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Smartup LMS - Marketplace DTOs
 */

// ==================== Item DTOs ====================

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceItemDto {
    private Long id;
    private String slug;
    private String name;
    private String nameUz;
    private String nameRu;
    private String nameEn;
    private String shortDescription;
    private String description;
    private MarketplaceItem.ItemType type;
    private String iconUrl;
    private String bannerUrl;
    private List<String> screenshots;
    private String videoUrl;
    private MarketplaceItem.PricingModel pricingModel;
    private BigDecimal price;
    private String priceCurrency;
    private BigDecimal monthlyPrice;
    private BigDecimal yearlyPrice;
    private Integer trialDays;
    private String publisherName;
    private boolean publisherVerified;
    private String version;
    private List<CategoryDto> categories;
    private Set<String> tags;
    private Long downloadCount;
    private Long installCount;
    private Double ratingAverage;
    private Long ratingCount;
    private boolean featured;
    private boolean editorChoice;
    private String documentationUrl;
    private String supportUrl;
    private Instant publishedAt;
    private Instant lastUpdatedAt;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class MarketplaceItemSummaryDto {
    private Long id;
    private String slug;
    private String name;
    private String shortDescription;
    private MarketplaceItem.ItemType type;
    private String iconUrl;
    private MarketplaceItem.PricingModel pricingModel;
    private BigDecimal price;
    private String priceCurrency;
    private String publisherName;
    private boolean publisherVerified;
    private Double ratingAverage;
    private Long ratingCount;
    private Long installCount;
    private boolean featured;
}

// ==================== Module DTOs ====================

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ModuleDetailDto {
    private MarketplaceItemDto item;
    private MarketplaceModule.ModuleType moduleType;
    private Set<String> requiredPermissions;
    private Set<String> providedEndpoints;
    private boolean hasDashboardWidget;
    private boolean hasSettingsPage;
    private boolean hasAdminPage;
    private boolean sandboxEnabled;
    private boolean securityAuditPassed;
    private Map<String, String> requirements;
    private Set<String> dependencies;
    private Set<String> hooks;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ModuleInstallRequestDto {
    private String moduleSlug;
    private Map<String, String> configuration;
    private boolean startTrial;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ModuleInstallationDto {
    private Long id;
    private Long moduleId;
    private String moduleName;
    private String moduleSlug;
    private String installedVersion;
    private ModuleInstallation.InstallationStatus status;
    private Map<String, String> settings;
    private boolean active;
    private boolean trial;
    private Instant trialEndsAt;
    private boolean updateAvailable;
    private String availableVersion;
    private Instant installedAt;
    private Instant lastUsedAt;
    private Long usageCount;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ModuleConfigUpdateDto {
    private Map<String, String> settings;
    private boolean active;
    private boolean autoUpdate;
}

// ==================== Course DTOs ====================

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CourseDetailDto {
    private MarketplaceItemDto item;
    private MarketplaceCourse.CourseType courseType;
    private MarketplaceCourse.DifficultyLevel difficultyLevel;
    private Integer moduleCount;
    private Integer lessonCount;
    private Integer quizCount;
    private Integer assignmentCount;
    private Integer videoCount;
    private Integer totalDurationMinutes;
    private String formattedDuration;
    private Set<String> languages;
    private boolean hasSubtitles;
    private boolean hasDownloadableResources;
    private Integer resourceCount;
    private List<String> learningOutcomes;
    private List<String> prerequisites;
    private List<String> targetAudience;
    private Set<String> skills;
    private boolean hasCertificate;
    private Double cpeCredits;
    private String accreditationBody;
    private InstructorDto instructor;
    private MarketplaceCourse.LicenseType licenseType;
    private Integer maxUsers;
    private Integer validityDays;
    private Long enrollmentCount;
    private Double completionRate;
    private Double averageQuizScore;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class InstructorDto {
    private Long id;
    private String name;
    private String title;
    private String bio;
    private String avatarUrl;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CoursePurchaseRequestDto {
    private String courseSlug;
    private CourseSubscription.SubscriptionType subscriptionType;
    private Integer maxEnrollments;
    private boolean autoRenew;
    private String paymentMethodId;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CourseSubscriptionDto {
    private Long id;
    private Long courseId;
    private String courseName;
    private String courseSlug;
    private CourseSubscription.SubscriptionStatus status;
    private CourseSubscription.SubscriptionType subscriptionType;
    private BigDecimal purchasePrice;
    private Instant startsAt;
    private Instant expiresAt;
    private boolean autoRenew;
    private Integer maxEnrollments;
    private Integer currentEnrollments;
    private Integer remainingSeats;
    private Long courseCopyId;
    private boolean contentImported;
    private Instant createdAt;
}

// ==================== Category DTOs ====================

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CategoryDto {
    private Long id;
    private String slug;
    private String name;
    private String nameUz;
    private String nameRu;
    private String nameEn;
    private String description;
    private String icon;
    private String color;
    private Long parentId;
    private Long itemCount;
    private List<CategoryDto> children;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CategoryCreateDto {
    private String slug;
    private String name;
    private String nameUz;
    private String nameRu;
    private String nameEn;
    private String description;
    private String icon;
    private String color;
    private Long parentId;
    private Integer sortOrder;
}

// ==================== Review DTOs ====================

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ReviewDto {
    private Long id;
    private Long itemId;
    private Long userId;
    private String userName;
    private String userAvatarUrl;
    private String organizationName;
    private Integer rating;
    private Integer ratingUsability;
    private Integer ratingFeatures;
    private Integer ratingSupport;
    private Integer ratingValue;
    private String title;
    private String content;
    private List<String> pros;
    private List<String> cons;
    private String versionReviewed;
    private String usageDuration;
    private boolean verifiedPurchase;
    private boolean verifiedUsage;
    private Long helpfulCount;
    private Long notHelpfulCount;
    private String publisherResponse;
    private Instant publisherResponseAt;
    private boolean featured;
    private Instant createdAt;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ReviewCreateDto {
    private Integer rating;
    private Integer ratingUsability;
    private Integer ratingFeatures;
    private Integer ratingSupport;
    private Integer ratingValue;
    private String title;
    private String content;
    private List<String> pros;
    private List<String> cons;
    private String usageDuration;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class RatingDistributionDto {
    private Long totalReviews;
    private Double averageRating;
    private Map<Integer, Long> distribution; // 1-5 stars -> count
    private Map<String, Double> categoryRatings; // usability, features, etc.
}

// ==================== Search & Filter DTOs ====================

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class MarketplaceSearchDto {
    private String query;
    private MarketplaceItem.ItemType type;
    private List<Long> categoryIds;
    private List<String> tags;
    private MarketplaceItem.PricingModel pricingModel;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minRating;
    private String sortBy; // popular, newest, rating, price_asc, price_desc
    private Integer page;
    private Integer size;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class MarketplaceSearchResultDto {
    private List<MarketplaceItemSummaryDto> items;
    private long totalItems;
    private int totalPages;
    private int currentPage;
    private Map<String, Long> facets; // category counts, tag counts, etc.
}

// ==================== Dashboard DTOs ====================

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class MarketplaceDashboardDto {
    private List<MarketplaceItemSummaryDto> featuredItems;
    private List<MarketplaceItemSummaryDto> popularModules;
    private List<MarketplaceItemSummaryDto> popularCourses;
    private List<MarketplaceItemSummaryDto> newArrivals;
    private List<MarketplaceItemSummaryDto> editorChoice;
    private List<CategoryDto> featuredCategories;
    private MarketplaceStatsDto stats;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class MarketplaceStatsDto {
    private Long totalModules;
    private Long totalCourses;
    private Long totalPublishers;
    private Long totalDownloads;
    private Long freeItems;
}

// ==================== Organization Dashboard ====================

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class OrganizationMarketplaceDto {
    private List<ModuleInstallationDto> installedModules;
    private List<CourseSubscriptionDto> subscribedCourses;
    private List<MarketplaceItemSummaryDto> recommendedItems;
    private List<ModuleInstallationDto> updateAvailable;
    private OrganizationUsageStatsDto usageStats;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class OrganizationUsageStatsDto {
    private Integer totalInstalledModules;
    private Integer activeModules;
    private Integer totalSubscribedCourses;
    private Integer activeCourses;
    private Long totalCourseEnrollments;
    private Long totalModuleUsage;
}
