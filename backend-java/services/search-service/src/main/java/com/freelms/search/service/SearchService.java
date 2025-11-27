package com.freelms.search.service;

import com.freelms.search.dto.SearchRequest;
import com.freelms.search.dto.SearchResponse;
import com.freelms.search.entity.SearchableDocument;
import com.freelms.search.repository.SearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for handling search operations.
 */
@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final SearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public SearchService(SearchRepository searchRepository, ElasticsearchOperations elasticsearchOperations) {
        this.searchRepository = searchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /**
     * Perform a search based on the given request.
     */
    public SearchResponse search(SearchRequest request) {
        long startTime = System.currentTimeMillis();

        Criteria criteria = buildCriteria(request);
        Query query = new CriteriaQuery(criteria)
                .setPageable(PageRequest.of(request.getPage(), request.getSize()));

        SearchHits<SearchableDocument> searchHits = elasticsearchOperations.search(query, SearchableDocument.class);

        SearchResponse response = new SearchResponse();
        response.setQuery(request.getQuery());
        response.setTotalHits(searchHits.getTotalHits());
        response.setPage(request.getPage());
        response.setSize(request.getSize());
        response.setTotalPages((int) Math.ceil((double) searchHits.getTotalHits() / request.getSize()));
        response.setResults(mapToResults(searchHits.getSearchHits()));
        response.setFacets(buildFacets(searchHits));
        response.setSearchTimeMs(System.currentTimeMillis() - startTime);

        log.debug("Search completed in {}ms, found {} results for query: {}",
                response.getSearchTimeMs(), response.getTotalHits(), request.getQuery());

        return response;
    }

    /**
     * Get autocomplete suggestions.
     */
    public List<String> suggest(String query, int limit) {
        Criteria criteria = new Criteria("title").contains(query)
                .or(new Criteria("description").contains(query));

        Query searchQuery = new CriteriaQuery(criteria)
                .setPageable(PageRequest.of(0, limit));

        SearchHits<SearchableDocument> hits = elasticsearchOperations.search(searchQuery, SearchableDocument.class);

        return hits.getSearchHits().stream()
                .map(hit -> hit.getContent().getTitle())
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Index a new document.
     */
    public void indexDocument(SearchableDocument document) {
        searchRepository.save(document);
        log.info("Indexed document: {} - {}", document.getEntityType(), document.getEntityId());
    }

    /**
     * Delete a document from index.
     */
    public void deleteDocument(String entityType, String entityId) {
        searchRepository.deleteByEntityTypeAndEntityId(entityType, entityId);
        log.info("Deleted document from index: {} - {}", entityType, entityId);
    }

    /**
     * Reindex all documents of a specific type.
     */
    public void reindex(String entityType) {
        log.info("Starting reindex for entity type: {}", entityType);
        // Implementation would fetch data from respective services and reindex
        // This is a placeholder for the actual implementation
    }

    /**
     * Get search statistics.
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDocuments", searchRepository.count());
        // Add more statistics as needed
        return stats;
    }

    private Criteria buildCriteria(SearchRequest request) {
        Criteria criteria = new Criteria("title").contains(request.getQuery())
                .or(new Criteria("description").contains(request.getQuery()))
                .or(new Criteria("content").contains(request.getQuery()));

        if (request.getEntityTypes() != null && !request.getEntityTypes().isEmpty()) {
            criteria = criteria.and(new Criteria("entityType").in(request.getEntityTypes()));
        }

        if (request.getCategory() != null) {
            criteria = criteria.and(new Criteria("category").is(request.getCategory()));
        }

        if (request.getOrganizationId() != null) {
            criteria = criteria.and(new Criteria("organizationId").is(request.getOrganizationId()));
        }

        if (request.getDifficulty() != null) {
            criteria = criteria.and(new Criteria("difficulty").is(request.getDifficulty()));
        }

        if (request.getLanguage() != null) {
            criteria = criteria.and(new Criteria("language").is(request.getLanguage()));
        }

        if (Boolean.TRUE.equals(request.getPublicOnly())) {
            criteria = criteria.and(new Criteria("isPublic").is(true));
        }

        return criteria;
    }

    private List<SearchResponse.SearchResult> mapToResults(List<SearchHit<SearchableDocument>> hits) {
        return hits.stream().map(hit -> {
            SearchableDocument doc = hit.getContent();
            SearchResponse.SearchResult result = new SearchResponse.SearchResult();
            result.setId(doc.getId());
            result.setEntityType(doc.getEntityType());
            result.setEntityId(doc.getEntityId());
            result.setTitle(doc.getTitle());
            result.setDescription(doc.getDescription());
            result.setScore(hit.getScore());
            result.setCategory(doc.getCategory());
            result.setRating(doc.getRating());
            result.setAuthorName(doc.getAuthorName());

            // Handle highlighting if available
            if (hit.getHighlightFields().containsKey("title")) {
                result.setHighlightedTitle(String.join("...", hit.getHighlightField("title")));
            }
            if (hit.getHighlightFields().containsKey("description")) {
                result.setHighlightedDescription(String.join("...", hit.getHighlightField("description")));
            }

            return result;
        }).collect(Collectors.toList());
    }

    private Map<String, Long> buildFacets(SearchHits<SearchableDocument> hits) {
        Map<String, Long> facets = new HashMap<>();

        // Count by entity type
        hits.getSearchHits().stream()
                .map(hit -> hit.getContent().getEntityType())
                .forEach(type -> facets.merge("type:" + type, 1L, Long::sum));

        // Count by category
        hits.getSearchHits().stream()
                .map(hit -> hit.getContent().getCategory())
                .filter(cat -> cat != null)
                .forEach(cat -> facets.merge("category:" + cat, 1L, Long::sum));

        return facets;
    }
}
