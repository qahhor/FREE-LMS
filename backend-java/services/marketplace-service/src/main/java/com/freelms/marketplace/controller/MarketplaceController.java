package com.freelms.marketplace.controller;

import com.freelms.marketplace.dto.*;
import com.freelms.marketplace.service.MarketplaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Smartup LMS - Marketplace Controller
 *
 * REST API for browsing and searching marketplace items.
 */
@RestController
@RequestMapping("/api/v1/marketplace")
@RequiredArgsConstructor
@Tag(name = "Marketplace", description = "Marketplace browsing and search API")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    @Operation(summary = "Get marketplace dashboard", description = "Returns featured items, popular modules/courses, and statistics")
    public ResponseEntity<MarketplaceDashboardDto> getDashboard() {
        return ResponseEntity.ok(marketplaceService.getDashboard());
    }

    // ==================== Search ====================

    @PostMapping("/search")
    @Operation(summary = "Search marketplace", description = "Search items with filters and pagination")
    public ResponseEntity<MarketplaceSearchResultDto> search(@RequestBody MarketplaceSearchDto searchDto) {
        return ResponseEntity.ok(marketplaceService.search(searchDto));
    }

    @GetMapping("/search")
    @Operation(summary = "Quick search", description = "Simple text search")
    public ResponseEntity<MarketplaceSearchResultDto> quickSearch(
            @RequestParam String q,
            @RequestParam(required = false) String type,
            Pageable pageable) {
        MarketplaceSearchDto dto = MarketplaceSearchDto.builder()
                .query(q)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .build();
        return ResponseEntity.ok(marketplaceService.search(dto));
    }

    // ==================== Browse Modules ====================

    @GetMapping("/modules")
    @Operation(summary = "Browse modules", description = "List all marketplace modules")
    public ResponseEntity<Page<MarketplaceItemSummaryDto>> browseModules(
            @RequestParam(required = false) String type,
            Pageable pageable) {
        return ResponseEntity.ok(marketplaceService.browseModules(type, pageable));
    }

    @GetMapping("/modules/{slug}")
    @Operation(summary = "Get module details", description = "Get detailed information about a module")
    public ResponseEntity<ModuleDetailDto> getModuleDetails(@PathVariable String slug) {
        return marketplaceService.getModuleDetails(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Browse Courses ====================

    @GetMapping("/courses")
    @Operation(summary = "Browse courses", description = "List all marketplace courses")
    public ResponseEntity<Page<MarketplaceItemSummaryDto>> browseCourses(
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String language,
            Pageable pageable) {
        return ResponseEntity.ok(marketplaceService.browseCourses(difficulty, language, pageable));
    }

    @GetMapping("/courses/{slug}")
    @Operation(summary = "Get course details", description = "Get detailed information about a course")
    public ResponseEntity<CourseDetailDto> getCourseDetails(@PathVariable String slug) {
        return marketplaceService.getCourseDetails(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Categories ====================

    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Returns category tree")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        return ResponseEntity.ok(marketplaceService.getAllCategories());
    }

    @GetMapping("/categories/{slug}")
    @Operation(summary = "Get category details", description = "Get category with items")
    public ResponseEntity<CategoryDto> getCategoryBySlug(@PathVariable String slug) {
        return marketplaceService.getCategoryBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/categories/{slug}/items")
    @Operation(summary = "Get items by category", description = "List items in a category")
    public ResponseEntity<Page<MarketplaceItemSummaryDto>> getItemsByCategory(
            @PathVariable String slug,
            Pageable pageable) {
        return ResponseEntity.ok(marketplaceService.getItemsByCategory(slug, pageable));
    }

    // ==================== Item Details ====================

    @GetMapping("/items/{slug}")
    @Operation(summary = "Get item by slug", description = "Get any marketplace item by its slug")
    public ResponseEntity<MarketplaceItemDto> getItemBySlug(@PathVariable String slug) {
        return marketplaceService.getItemBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Reviews ====================

    @GetMapping("/items/{id}/reviews")
    @Operation(summary = "Get item reviews", description = "Get reviews for an item")
    public ResponseEntity<Page<ReviewDto>> getItemReviews(
            @PathVariable Long id,
            Pageable pageable) {
        return ResponseEntity.ok(marketplaceService.getItemReviews(id, pageable));
    }

    @GetMapping("/items/{id}/ratings")
    @Operation(summary = "Get rating distribution", description = "Get rating breakdown for an item")
    public ResponseEntity<RatingDistributionDto> getRatingDistribution(@PathVariable Long id) {
        return ResponseEntity.ok(marketplaceService.getRatingDistribution(id));
    }

    @PostMapping("/items/{id}/reviews")
    @Operation(summary = "Create review", description = "Submit a review for an item")
    public ResponseEntity<ReviewDto> createReview(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ReviewCreateDto dto) {
        return ResponseEntity.ok(marketplaceService.createReview(id, userId, dto));
    }
}
