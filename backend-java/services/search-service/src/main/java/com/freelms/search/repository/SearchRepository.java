package com.freelms.search.repository;

import com.freelms.search.entity.SearchableDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch repository for searchable documents.
 */
@Repository
public interface SearchRepository extends ElasticsearchRepository<SearchableDocument, String> {

    /**
     * Find documents by entity type.
     */
    List<SearchableDocument> findByEntityType(String entityType);

    /**
     * Find documents by entity type and entity ID.
     */
    SearchableDocument findByEntityTypeAndEntityId(String entityType, String entityId);

    /**
     * Find documents by organization ID.
     */
    List<SearchableDocument> findByOrganizationId(Long organizationId);

    /**
     * Delete documents by entity type and entity ID.
     */
    void deleteByEntityTypeAndEntityId(String entityType, String entityId);

    /**
     * Find documents by category.
     */
    List<SearchableDocument> findByCategory(String category);
}
