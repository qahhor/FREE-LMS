package com.freelms.search.dto;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for search results.
 */
public class SearchResponse {

    private List<SearchResult> results;

    private long totalHits;

    private int page;

    private int size;

    private int totalPages;

    private Map<String, Long> facets; // category -> count, entityType -> count

    private long searchTimeMs;

    private String query;

    // Getters and Setters
    public List<SearchResult> getResults() {
        return results;
    }

    public void setResults(List<SearchResult> results) {
        this.results = results;
    }

    public long getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(long totalHits) {
        this.totalHits = totalHits;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public Map<String, Long> getFacets() {
        return facets;
    }

    public void setFacets(Map<String, Long> facets) {
        this.facets = facets;
    }

    public long getSearchTimeMs() {
        return searchTimeMs;
    }

    public void setSearchTimeMs(long searchTimeMs) {
        this.searchTimeMs = searchTimeMs;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Individual search result item.
     */
    public static class SearchResult {
        private String id;
        private String entityType;
        private String entityId;
        private String title;
        private String description;
        private String highlightedTitle;
        private String highlightedDescription;
        private Float score;
        private String category;
        private Float rating;
        private String authorName;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getEntityType() {
            return entityType;
        }

        public void setEntityType(String entityType) {
            this.entityType = entityType;
        }

        public String getEntityId() {
            return entityId;
        }

        public void setEntityId(String entityId) {
            this.entityId = entityId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getHighlightedTitle() {
            return highlightedTitle;
        }

        public void setHighlightedTitle(String highlightedTitle) {
            this.highlightedTitle = highlightedTitle;
        }

        public String getHighlightedDescription() {
            return highlightedDescription;
        }

        public void setHighlightedDescription(String highlightedDescription) {
            this.highlightedDescription = highlightedDescription;
        }

        public Float getScore() {
            return score;
        }

        public void setScore(Float score) {
            this.score = score;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Float getRating() {
            return rating;
        }

        public void setRating(Float rating) {
            this.rating = rating;
        }

        public String getAuthorName() {
            return authorName;
        }

        public void setAuthorName(String authorName) {
            this.authorName = authorName;
        }
    }
}
