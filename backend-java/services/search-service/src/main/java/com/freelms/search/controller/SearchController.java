package com.freelms.search.controller;

import com.freelms.search.dto.SearchRequest;
import com.freelms.search.dto.SearchResponse;
import com.freelms.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for search operations.
 */
@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "Unified search across all LMS content")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Perform a search query.
     */
    @GetMapping
    @Operation(summary = "Search content", description = "Search across courses, users, documents, and more")
    public ResponseEntity<SearchResponse> search(
            @Parameter(description = "Search query") @RequestParam String q,
            @Parameter(description = "Entity types to search") @RequestParam(required = false) List<String> type,
            @Parameter(description = "Category filter") @RequestParam(required = false) String category,
            @Parameter(description = "Difficulty filter") @RequestParam(required = false) String difficulty,
            @Parameter(description = "Organization ID") @RequestParam(required = false) Long organizationId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "relevance") String sort) {

        SearchRequest request = new SearchRequest();
        request.setQuery(q);
        request.setEntityTypes(type);
        request.setCategory(category);
        request.setDifficulty(difficulty);
        request.setOrganizationId(organizationId);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sort);

        return ResponseEntity.ok(searchService.search(request));
    }

    /**
     * Perform an advanced search with more options.
     */
    @PostMapping("/advanced")
    @Operation(summary = "Advanced search", description = "Search with advanced filters and options")
    public ResponseEntity<SearchResponse> advancedSearch(@Valid @RequestBody SearchRequest request) {
        return ResponseEntity.ok(searchService.search(request));
    }

    /**
     * Get autocomplete suggestions.
     */
    @GetMapping("/suggest")
    @Operation(summary = "Get suggestions", description = "Get autocomplete suggestions for search query")
    public ResponseEntity<List<String>> suggest(
            @Parameter(description = "Partial query") @RequestParam String q,
            @Parameter(description = "Max suggestions") @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(searchService.suggest(q, limit));
    }

    /**
     * Search within a specific course.
     */
    @GetMapping("/course/{courseId}")
    @Operation(summary = "Search within course", description = "Search content within a specific course")
    public ResponseEntity<SearchResponse> searchInCourse(
            @Parameter(description = "Course ID") @PathVariable Long courseId,
            @Parameter(description = "Search query") @RequestParam String q,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        SearchRequest request = new SearchRequest();
        request.setQuery(q);
        request.setEntityTypes(List.of("lesson", "document"));
        // Additional filtering by course would be added here
        request.setPage(page);
        request.setSize(size);

        return ResponseEntity.ok(searchService.search(request));
    }

    /**
     * Trigger reindexing for a specific entity type.
     */
    @PostMapping("/reindex/{entityType}")
    @Operation(summary = "Reindex", description = "Trigger reindexing for a specific entity type (admin only)")
    public ResponseEntity<Map<String, String>> reindex(
            @Parameter(description = "Entity type to reindex") @PathVariable String entityType) {
        searchService.reindex(entityType);
        return ResponseEntity.ok(Map.of(
                "status", "started",
                "entityType", entityType,
                "message", "Reindexing started for " + entityType
        ));
    }

    /**
     * Get search statistics.
     */
    @GetMapping("/stats")
    @Operation(summary = "Get stats", description = "Get search index statistics")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(searchService.getStats());
    }
}
