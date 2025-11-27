package com.freelms.marketplace.repository;

import com.freelms.marketplace.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Smartup LMS - Marketplace Repositories
 */

@Repository
interface MarketplaceItemRepository extends JpaRepository<MarketplaceItem, Long> {

    Optional<MarketplaceItem> findBySlug(String slug);

    Page<MarketplaceItem> findByStatus(MarketplaceItem.ItemStatus status, Pageable pageable);

    Page<MarketplaceItem> findByTypeAndStatus(MarketplaceItem.ItemType type,
                                               MarketplaceItem.ItemStatus status,
                                               Pageable pageable);

    @Query("SELECT i FROM MarketplaceItem i WHERE i.status = 'APPROVED' AND i.featured = true ORDER BY i.featuredOrder")
    List<MarketplaceItem> findFeaturedItems();

    @Query("SELECT i FROM MarketplaceItem i WHERE i.status = 'APPROVED' ORDER BY i.downloadCount DESC")
    Page<MarketplaceItem> findPopularItems(Pageable pageable);

    @Query("SELECT i FROM MarketplaceItem i WHERE i.status = 'APPROVED' ORDER BY i.publishedAt DESC")
    Page<MarketplaceItem> findNewItems(Pageable pageable);

    @Query("SELECT i FROM MarketplaceItem i WHERE i.status = 'APPROVED' " +
           "AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<MarketplaceItem> search(@Param("query") String query, Pageable pageable);

    @Query("SELECT i FROM MarketplaceItem i JOIN i.categories c WHERE c.id = :categoryId AND i.status = 'APPROVED'")
    Page<MarketplaceItem> findByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

    List<MarketplaceItem> findByPublisherId(Long publisherId);

    @Query("SELECT i FROM MarketplaceItem i WHERE i.status = 'APPROVED' AND i.editorChoice = true")
    List<MarketplaceItem> findEditorChoiceItems();
}

@Repository
interface MarketplaceModuleRepository extends JpaRepository<MarketplaceModule, Long> {

    Optional<MarketplaceModule> findBySlug(String slug);

    Page<MarketplaceModule> findByModuleTypeAndStatus(MarketplaceModule.ModuleType type,
                                                       MarketplaceItem.ItemStatus status,
                                                       Pageable pageable);

    @Query("SELECT m FROM MarketplaceModule m WHERE m.status = 'APPROVED' ORDER BY m.installCount DESC")
    Page<MarketplaceModule> findMostInstalled(Pageable pageable);

    @Query("SELECT m FROM MarketplaceModule m WHERE m.status = 'APPROVED' AND m.pricingModel = 'FREE'")
    Page<MarketplaceModule> findFreeModules(Pageable pageable);

    @Query("SELECT m FROM MarketplaceModule m WHERE m.status = 'APPROVED' AND :tag MEMBER OF m.tags")
    Page<MarketplaceModule> findByTag(@Param("tag") String tag, Pageable pageable);
}

@Repository
interface MarketplaceCourseRepository extends JpaRepository<MarketplaceCourse, Long> {

    Optional<MarketplaceCourse> findBySlug(String slug);

    Page<MarketplaceCourse> findByCourseTypeAndStatus(MarketplaceCourse.CourseType type,
                                                       MarketplaceItem.ItemStatus status,
                                                       Pageable pageable);

    Page<MarketplaceCourse> findByDifficultyLevelAndStatus(MarketplaceCourse.DifficultyLevel level,
                                                            MarketplaceItem.ItemStatus status,
                                                            Pageable pageable);

    @Query("SELECT c FROM MarketplaceCourse c WHERE c.status = 'APPROVED' ORDER BY c.enrollmentCount DESC")
    Page<MarketplaceCourse> findMostEnrolled(Pageable pageable);

    @Query("SELECT c FROM MarketplaceCourse c WHERE c.status = 'APPROVED' AND c.pricingModel = 'FREE'")
    Page<MarketplaceCourse> findFreeCourses(Pageable pageable);

    @Query("SELECT c FROM MarketplaceCourse c WHERE c.status = 'APPROVED' AND :skill MEMBER OF c.skills")
    Page<MarketplaceCourse> findBySkill(@Param("skill") String skill, Pageable pageable);

    @Query("SELECT c FROM MarketplaceCourse c WHERE c.status = 'APPROVED' AND :lang MEMBER OF c.languages")
    Page<MarketplaceCourse> findByLanguage(@Param("lang") String language, Pageable pageable);
}

@Repository
interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIsNullAndActiveTrue();

    List<Category> findByParentIdAndActiveTrue(Long parentId);

    List<Category> findByFeaturedTrueAndActiveTrue();

    @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.sortOrder, c.name")
    List<Category> findAllActiveOrdered();
}

@Repository
interface ModuleInstallationRepository extends JpaRepository<ModuleInstallation, Long> {

    Optional<ModuleInstallation> findByOrganizationIdAndModuleId(Long organizationId, Long moduleId);

    List<ModuleInstallation> findByOrganizationId(Long organizationId);

    List<ModuleInstallation> findByOrganizationIdAndStatus(Long organizationId,
                                                            ModuleInstallation.InstallationStatus status);

    List<ModuleInstallation> findByOrganizationIdAndActiveTrue(Long organizationId);

    @Query("SELECT i FROM ModuleInstallation i WHERE i.updateAvailable = true AND i.autoUpdate = true")
    List<ModuleInstallation> findAutoUpdatePending();

    @Query("SELECT i FROM ModuleInstallation i WHERE i.trial = true AND i.trialEndsAt < CURRENT_TIMESTAMP")
    List<ModuleInstallation> findExpiredTrials();

    long countByModuleIdAndStatus(Long moduleId, ModuleInstallation.InstallationStatus status);
}

@Repository
interface CourseSubscriptionRepository extends JpaRepository<CourseSubscription, Long> {

    Optional<CourseSubscription> findByOrganizationIdAndCourseId(Long organizationId, Long courseId);

    List<CourseSubscription> findByOrganizationId(Long organizationId);

    List<CourseSubscription> findByOrganizationIdAndStatus(Long organizationId,
                                                            CourseSubscription.SubscriptionStatus status);

    @Query("SELECT s FROM CourseSubscription s WHERE s.status = 'ACTIVE' " +
           "AND s.expiresAt IS NOT NULL AND s.expiresAt < CURRENT_TIMESTAMP")
    List<CourseSubscription> findExpiredSubscriptions();

    @Query("SELECT s FROM CourseSubscription s WHERE s.autoRenew = true " +
           "AND s.expiresAt IS NOT NULL " +
           "AND s.expiresAt BETWEEN CURRENT_TIMESTAMP AND :renewalWindow")
    List<CourseSubscription> findUpcomingRenewals(@Param("renewalWindow") java.time.Instant renewalWindow);

    long countByCourseIdAndStatus(Long courseId, CourseSubscription.SubscriptionStatus status);
}

@Repository
interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByItemIdAndStatus(Long itemId, Review.ReviewStatus status, Pageable pageable);

    Optional<Review> findByItemIdAndUserId(Long itemId, Long userId);

    List<Review> findByItemIdAndStatusAndFeaturedTrue(Long itemId, Review.ReviewStatus status);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.item.id = :itemId AND r.status = 'APPROVED'")
    Double calculateAverageRating(@Param("itemId") Long itemId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.item.id = :itemId AND r.status = 'APPROVED'")
    Long countApprovedReviews(@Param("itemId") Long itemId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.item.id = :itemId AND r.status = 'APPROVED' GROUP BY r.rating")
    List<Object[]> getRatingDistribution(@Param("itemId") Long itemId);

    Page<Review> findByStatus(Review.ReviewStatus status, Pageable pageable);

    List<Review> findByUserId(Long userId);
}
