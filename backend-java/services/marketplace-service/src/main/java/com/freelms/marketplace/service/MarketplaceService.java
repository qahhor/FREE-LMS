package com.freelms.marketplace.service;

import com.freelms.marketplace.dto.*;
import com.freelms.marketplace.entity.*;
import com.freelms.marketplace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Smartup LMS - Marketplace Service
 *
 * Main service for marketplace operations: browsing, searching, and managing items.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplaceService {

    private final MarketplaceItemRepository itemRepository;
    private final MarketplaceModuleRepository moduleRepository;
    private final MarketplaceCourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;

    // ==================== Dashboard ====================

    @Cacheable(value = "marketplace-dashboard", key = "'main'")
    public MarketplaceDashboardDto getDashboard() {
        log.info("Loading marketplace dashboard");

        List<MarketplaceItemSummaryDto> featured = itemRepository.findFeaturedItems().stream()
                .map(this::toSummaryDto)
                .limit(12)
                .collect(Collectors.toList());

        List<MarketplaceItemSummaryDto> popularModules = moduleRepository
                .findMostInstalled(PageRequest.of(0, 8))
                .map(this::toSummaryDto)
                .getContent();

        List<MarketplaceItemSummaryDto> popularCourses = courseRepository
                .findMostEnrolled(PageRequest.of(0, 8))
                .map(this::toSummaryDto)
                .getContent();

        List<MarketplaceItemSummaryDto> newArrivals = itemRepository
                .findNewItems(PageRequest.of(0, 8))
                .map(this::toSummaryDto)
                .getContent();

        List<MarketplaceItemSummaryDto> editorChoice = itemRepository.findEditorChoiceItems().stream()
                .map(this::toSummaryDto)
                .limit(6)
                .collect(Collectors.toList());

        List<CategoryDto> featuredCategories = categoryRepository.findByFeaturedTrueAndActiveTrue().stream()
                .map(this::toCategoryDto)
                .collect(Collectors.toList());

        MarketplaceStatsDto stats = MarketplaceStatsDto.builder()
                .totalModules(moduleRepository.count())
                .totalCourses(courseRepository.count())
                .totalPublishers(getUniquePublisherCount())
                .totalDownloads(getTotalDownloads())
                .freeItems(countFreeItems())
                .build();

        return MarketplaceDashboardDto.builder()
                .featuredItems(featured)
                .popularModules(popularModules)
                .popularCourses(popularCourses)
                .newArrivals(newArrivals)
                .editorChoice(editorChoice)
                .featuredCategories(featuredCategories)
                .stats(stats)
                .build();
    }

    // ==================== Search & Browse ====================

    public MarketplaceSearchResultDto search(MarketplaceSearchDto searchDto) {
        log.info("Searching marketplace: query={}, type={}", searchDto.getQuery(), searchDto.getType());

        Pageable pageable = createPageable(searchDto);
        Page<MarketplaceItem> results;

        if (searchDto.getQuery() != null && !searchDto.getQuery().isBlank()) {
            results = itemRepository.search(searchDto.getQuery(), pageable);
        } else if (searchDto.getType() != null) {
            results = itemRepository.findByTypeAndStatus(searchDto.getType(),
                    MarketplaceItem.ItemStatus.APPROVED, pageable);
        } else if (searchDto.getCategoryIds() != null && !searchDto.getCategoryIds().isEmpty()) {
            results = itemRepository.findByCategory(searchDto.getCategoryIds().get(0), pageable);
        } else {
            results = itemRepository.findByStatus(MarketplaceItem.ItemStatus.APPROVED, pageable);
        }

        List<MarketplaceItemSummaryDto> items = results.getContent().stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());

        return MarketplaceSearchResultDto.builder()
                .items(items)
                .totalItems(results.getTotalElements())
                .totalPages(results.getTotalPages())
                .currentPage(results.getNumber())
                .facets(new HashMap<>()) // Implement faceting if needed
                .build();
    }

    public Page<MarketplaceItemSummaryDto> browseModules(String moduleType, Pageable pageable) {
        Page<MarketplaceModule> modules;

        if (moduleType != null) {
            MarketplaceModule.ModuleType type = MarketplaceModule.ModuleType.valueOf(moduleType.toUpperCase());
            modules = moduleRepository.findByModuleTypeAndStatus(type,
                    MarketplaceItem.ItemStatus.APPROVED, pageable);
        } else {
            modules = moduleRepository.findAll(pageable);
        }

        return modules.map(this::toSummaryDto);
    }

    public Page<MarketplaceItemSummaryDto> browseCourses(String difficulty, String language, Pageable pageable) {
        Page<MarketplaceCourse> courses;

        if (difficulty != null) {
            MarketplaceCourse.DifficultyLevel level =
                    MarketplaceCourse.DifficultyLevel.valueOf(difficulty.toUpperCase());
            courses = courseRepository.findByDifficultyLevelAndStatus(level,
                    MarketplaceItem.ItemStatus.APPROVED, pageable);
        } else if (language != null) {
            courses = courseRepository.findByLanguage(language, pageable);
        } else {
            courses = courseRepository.findAll(pageable);
        }

        return courses.map(this::toSummaryDto);
    }

    // ==================== Item Details ====================

    public Optional<MarketplaceItemDto> getItemBySlug(String slug) {
        return itemRepository.findBySlug(slug)
                .map(this::toItemDto);
    }

    public Optional<ModuleDetailDto> getModuleDetails(String slug) {
        return moduleRepository.findBySlug(slug)
                .map(this::toModuleDetailDto);
    }

    public Optional<CourseDetailDto> getCourseDetails(String slug) {
        return courseRepository.findBySlug(slug)
                .map(this::toCourseDetailDto);
    }

    // ==================== Categories ====================

    @Cacheable(value = "marketplace-categories", key = "'all'")
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findByParentIsNullAndActiveTrue().stream()
                .map(this::toCategoryDtoWithChildren)
                .collect(Collectors.toList());
    }

    public Optional<CategoryDto> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(this::toCategoryDtoWithChildren);
    }

    public Page<MarketplaceItemSummaryDto> getItemsByCategory(String categorySlug, Pageable pageable) {
        Optional<Category> category = categoryRepository.findBySlug(categorySlug);
        if (category.isEmpty()) {
            return Page.empty();
        }

        return itemRepository.findByCategory(category.get().getId(), pageable)
                .map(this::toSummaryDto);
    }

    // ==================== Reviews ====================

    public Page<ReviewDto> getItemReviews(Long itemId, Pageable pageable) {
        return reviewRepository.findByItemIdAndStatus(itemId, Review.ReviewStatus.APPROVED, pageable)
                .map(this::toReviewDto);
    }

    public RatingDistributionDto getRatingDistribution(Long itemId) {
        Double average = reviewRepository.calculateAverageRating(itemId);
        Long total = reviewRepository.countApprovedReviews(itemId);
        List<Object[]> distribution = reviewRepository.getRatingDistribution(itemId);

        Map<Integer, Long> distMap = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distMap.put(i, 0L);
        }
        for (Object[] row : distribution) {
            distMap.put((Integer) row[0], (Long) row[1]);
        }

        return RatingDistributionDto.builder()
                .totalReviews(total != null ? total : 0L)
                .averageRating(average != null ? average : 0.0)
                .distribution(distMap)
                .categoryRatings(new HashMap<>())
                .build();
    }

    @Transactional
    public ReviewDto createReview(Long itemId, Long userId, ReviewCreateDto dto) {
        Optional<MarketplaceItem> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new IllegalArgumentException("Item not found");
        }

        // Check if already reviewed
        if (reviewRepository.findByItemIdAndUserId(itemId, userId).isPresent()) {
            throw new IllegalArgumentException("Already reviewed this item");
        }

        Review review = Review.builder()
                .item(itemOpt.get())
                .userId(userId)
                .rating(dto.getRating())
                .ratingUsability(dto.getRatingUsability())
                .ratingFeatures(dto.getRatingFeatures())
                .ratingSupport(dto.getRatingSupport())
                .ratingValue(dto.getRatingValue())
                .title(dto.getTitle())
                .content(dto.getContent())
                .pros(dto.getPros())
                .cons(dto.getCons())
                .usageDuration(dto.getUsageDuration())
                .status(Review.ReviewStatus.PENDING)
                .build();

        review = reviewRepository.save(review);
        log.info("Created review for item: {} by user: {}", itemId, userId);

        return toReviewDto(review);
    }

    // ==================== Helper Methods ====================

    private Pageable createPageable(MarketplaceSearchDto dto) {
        int page = dto.getPage() != null ? dto.getPage() : 0;
        int size = dto.getSize() != null ? dto.getSize() : 20;

        Sort sort = Sort.by(Sort.Direction.DESC, "downloadCount");
        if (dto.getSortBy() != null) {
            sort = switch (dto.getSortBy()) {
                case "newest" -> Sort.by(Sort.Direction.DESC, "publishedAt");
                case "rating" -> Sort.by(Sort.Direction.DESC, "ratingAverage");
                case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
                case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
                default -> Sort.by(Sort.Direction.DESC, "downloadCount");
            };
        }

        return PageRequest.of(page, size, sort);
    }

    private Long getUniquePublisherCount() {
        return 50L; // Placeholder - implement actual query
    }

    private Long getTotalDownloads() {
        return 100000L; // Placeholder - implement actual sum
    }

    private Long countFreeItems() {
        return moduleRepository.findFreeModules(PageRequest.of(0, 1)).getTotalElements() +
               courseRepository.findFreeCourses(PageRequest.of(0, 1)).getTotalElements();
    }

    // ==================== Mappers ====================

    private MarketplaceItemSummaryDto toSummaryDto(MarketplaceItem item) {
        return MarketplaceItemSummaryDto.builder()
                .id(item.getId())
                .slug(item.getSlug())
                .name(item.getName())
                .shortDescription(item.getShortDescription())
                .type(item.getType())
                .iconUrl(item.getIconUrl())
                .pricingModel(item.getPricingModel())
                .price(item.getPrice())
                .priceCurrency(item.getPriceCurrency())
                .publisherName(item.getPublisherName())
                .publisherVerified(item.isPublisherVerified())
                .ratingAverage(item.getRatingAverage())
                .ratingCount(item.getRatingCount())
                .installCount(item.getInstallCount())
                .featured(item.isFeatured())
                .build();
    }

    private MarketplaceItemDto toItemDto(MarketplaceItem item) {
        return MarketplaceItemDto.builder()
                .id(item.getId())
                .slug(item.getSlug())
                .name(item.getName())
                .nameUz(item.getNameUz())
                .nameRu(item.getNameRu())
                .nameEn(item.getNameEn())
                .shortDescription(item.getShortDescription())
                .description(item.getDescription())
                .type(item.getType())
                .iconUrl(item.getIconUrl())
                .bannerUrl(item.getBannerUrl())
                .screenshots(item.getScreenshots())
                .videoUrl(item.getVideoUrl())
                .pricingModel(item.getPricingModel())
                .price(item.getPrice())
                .priceCurrency(item.getPriceCurrency())
                .monthlyPrice(item.getMonthlyPrice())
                .yearlyPrice(item.getYearlyPrice())
                .trialDays(item.getTrialDays())
                .publisherName(item.getPublisherName())
                .publisherVerified(item.isPublisherVerified())
                .version(item.getVersion())
                .tags(item.getTags())
                .downloadCount(item.getDownloadCount())
                .installCount(item.getInstallCount())
                .ratingAverage(item.getRatingAverage())
                .ratingCount(item.getRatingCount())
                .featured(item.isFeatured())
                .editorChoice(item.isEditorChoice())
                .documentationUrl(item.getDocumentationUrl())
                .supportUrl(item.getSupportUrl())
                .publishedAt(item.getPublishedAt())
                .lastUpdatedAt(item.getLastUpdatedAt())
                .build();
    }

    private ModuleDetailDto toModuleDetailDto(MarketplaceModule module) {
        return ModuleDetailDto.builder()
                .item(toItemDto(module))
                .moduleType(module.getModuleType())
                .requiredPermissions(module.getRequiredPermissions())
                .providedEndpoints(module.getProvidedEndpoints())
                .hasDashboardWidget(module.isHasDashboardWidget())
                .hasSettingsPage(module.isHasSettingsPage())
                .hasAdminPage(module.isHasAdminPage())
                .sandboxEnabled(module.isSandboxEnabled())
                .securityAuditPassed(module.isSecurityAuditPassed())
                .requirements(module.getRequirements())
                .dependencies(module.getDependencies())
                .hooks(module.getHooks())
                .build();
    }

    private CourseDetailDto toCourseDetailDto(MarketplaceCourse course) {
        InstructorDto instructor = InstructorDto.builder()
                .id(course.getInstructorId())
                .name(course.getInstructorName())
                .title(course.getInstructorTitle())
                .bio(course.getInstructorBio())
                .avatarUrl(course.getInstructorAvatarUrl())
                .build();

        return CourseDetailDto.builder()
                .item(toItemDto(course))
                .courseType(course.getCourseType())
                .difficultyLevel(course.getDifficultyLevel())
                .moduleCount(course.getModuleCount())
                .lessonCount(course.getLessonCount())
                .quizCount(course.getQuizCount())
                .assignmentCount(course.getAssignmentCount())
                .videoCount(course.getVideoCount())
                .totalDurationMinutes(course.getTotalDurationMinutes())
                .formattedDuration(course.getFormattedDuration())
                .languages(course.getLanguages())
                .hasSubtitles(course.isHasSubtitles())
                .hasDownloadableResources(course.isHasDownloadableResources())
                .resourceCount(course.getResourceCount())
                .learningOutcomes(course.getLearningOutcomes())
                .prerequisites(course.getPrerequisites())
                .targetAudience(course.getTargetAudience())
                .skills(course.getSkills())
                .hasCertificate(course.isHasCertificate())
                .cpeCredits(course.getCpeCredits())
                .accreditationBody(course.getAccreditationBody())
                .instructor(instructor)
                .licenseType(course.getLicenseType())
                .maxUsers(course.getMaxUsers())
                .validityDays(course.getValidityDays())
                .enrollmentCount(course.getEnrollmentCount())
                .completionRate(course.getCompletionRate())
                .averageQuizScore(course.getAverageQuizScore())
                .build();
    }

    private CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .slug(category.getSlug())
                .name(category.getName())
                .nameUz(category.getNameUz())
                .nameRu(category.getNameRu())
                .nameEn(category.getNameEn())
                .description(category.getDescription())
                .icon(category.getIcon())
                .color(category.getColor())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .itemCount(category.getItemCount())
                .build();
    }

    private CategoryDto toCategoryDtoWithChildren(Category category) {
        CategoryDto dto = toCategoryDto(category);
        dto.setChildren(category.getChildren().stream()
                .filter(Category::isActive)
                .map(this::toCategoryDto)
                .collect(Collectors.toList()));
        return dto;
    }

    private ReviewDto toReviewDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .itemId(review.getItem().getId())
                .userId(review.getUserId())
                .userName(review.getUserName())
                .userAvatarUrl(review.getUserAvatarUrl())
                .organizationName(review.getOrganizationName())
                .rating(review.getRating())
                .ratingUsability(review.getRatingUsability())
                .ratingFeatures(review.getRatingFeatures())
                .ratingSupport(review.getRatingSupport())
                .ratingValue(review.getRatingValue())
                .title(review.getTitle())
                .content(review.getContent())
                .pros(review.getPros())
                .cons(review.getCons())
                .versionReviewed(review.getVersionReviewed())
                .usageDuration(review.getUsageDuration())
                .verifiedPurchase(review.isVerifiedPurchase())
                .verifiedUsage(review.isVerifiedUsage())
                .helpfulCount(review.getHelpfulCount())
                .notHelpfulCount(review.getNotHelpfulCount())
                .publisherResponse(review.getPublisherResponse())
                .publisherResponseAt(review.getPublisherResponseAt())
                .featured(review.isFeatured())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
