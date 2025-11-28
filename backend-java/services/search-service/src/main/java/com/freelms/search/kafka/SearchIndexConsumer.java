package com.freelms.search.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelms.search.entity.SearchableDocument;
import com.freelms.search.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Kafka consumer for indexing events from various services.
 * Listens to entity lifecycle events and updates the search index accordingly.
 */
@Component
public class SearchIndexConsumer {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexConsumer.class);

    private final SearchService searchService;
    private final ObjectMapper objectMapper;

    public SearchIndexConsumer(SearchService searchService, ObjectMapper objectMapper) {
        this.searchService = searchService;
        this.objectMapper = objectMapper;
    }

    /**
     * Listen for course events (created, updated, deleted).
     */
    @KafkaListener(
            topics = "course-events",
            groupId = "search-indexer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleCourseEvent(
            @Payload String payload,
            @Header("event-type") String eventType,
            Acknowledgment ack) {
        try {
            log.debug("Received course event: type={}", eventType);
            JsonNode node = objectMapper.readTree(payload);

            switch (eventType) {
                case "COURSE_CREATED", "COURSE_UPDATED" -> indexCourse(node);
                case "COURSE_DELETED" -> deleteDocument("course", node.get("id").asText());
                case "COURSE_PUBLISHED" -> {
                    indexCourse(node);
                    log.info("Course published and indexed: {}", node.get("id").asText());
                }
                default -> log.warn("Unknown course event type: {}", eventType);
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing course event: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen for user events.
     */
    @KafkaListener(
            topics = "user-events",
            groupId = "search-indexer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserEvent(
            @Payload String payload,
            @Header("event-type") String eventType,
            Acknowledgment ack) {
        try {
            log.debug("Received user event: type={}", eventType);
            JsonNode node = objectMapper.readTree(payload);

            switch (eventType) {
                case "USER_CREATED", "USER_UPDATED", "USER_PROFILE_UPDATED" -> indexUser(node);
                case "USER_DELETED" -> deleteDocument("user", node.get("id").asText());
                default -> log.warn("Unknown user event type: {}", eventType);
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing user event: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen for lesson events.
     */
    @KafkaListener(
            topics = "lesson-events",
            groupId = "search-indexer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleLessonEvent(
            @Payload String payload,
            @Header("event-type") String eventType,
            Acknowledgment ack) {
        try {
            log.debug("Received lesson event: type={}", eventType);
            JsonNode node = objectMapper.readTree(payload);

            switch (eventType) {
                case "LESSON_CREATED", "LESSON_UPDATED" -> indexLesson(node);
                case "LESSON_DELETED" -> deleteDocument("lesson", node.get("id").asText());
                default -> log.warn("Unknown lesson event type: {}", eventType);
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing lesson event: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen for document/file events.
     */
    @KafkaListener(
            topics = "document-events",
            groupId = "search-indexer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDocumentEvent(
            @Payload String payload,
            @Header("event-type") String eventType,
            Acknowledgment ack) {
        try {
            log.debug("Received document event: type={}", eventType);
            JsonNode node = objectMapper.readTree(payload);

            switch (eventType) {
                case "DOCUMENT_UPLOADED", "DOCUMENT_UPDATED" -> indexDocument(node);
                case "DOCUMENT_DELETED" -> deleteDocument("document", node.get("id").asText());
                case "DOCUMENT_PROCESSED" -> {
                    // Document text extraction completed
                    indexDocument(node);
                    log.info("Document processed and indexed: {}", node.get("id").asText());
                }
                default -> log.warn("Unknown document event type: {}", eventType);
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing document event: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen for forum/discussion events.
     */
    @KafkaListener(
            topics = "forum-events",
            groupId = "search-indexer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleForumEvent(
            @Payload String payload,
            @Header("event-type") String eventType,
            Acknowledgment ack) {
        try {
            log.debug("Received forum event: type={}", eventType);
            JsonNode node = objectMapper.readTree(payload);

            switch (eventType) {
                case "POST_CREATED", "POST_UPDATED" -> indexForumPost(node);
                case "POST_DELETED" -> deleteDocument("forum_post", node.get("id").asText());
                case "ANSWER_ACCEPTED" -> {
                    // Update the post with accepted answer flag
                    indexForumPost(node);
                }
                default -> log.warn("Unknown forum event type: {}", eventType);
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing forum event: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen for organization events.
     */
    @KafkaListener(
            topics = "organization-events",
            groupId = "search-indexer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrganizationEvent(
            @Payload String payload,
            @Header("event-type") String eventType,
            Acknowledgment ack) {
        try {
            log.debug("Received organization event: type={}", eventType);
            JsonNode node = objectMapper.readTree(payload);

            switch (eventType) {
                case "ORGANIZATION_CREATED", "ORGANIZATION_UPDATED" -> indexOrganization(node);
                case "ORGANIZATION_DELETED" -> deleteDocument("organization", node.get("id").asText());
                default -> log.warn("Unknown organization event type: {}", eventType);
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing organization event: {}", e.getMessage(), e);
        }
    }

    // Indexing methods

    private void indexCourse(JsonNode node) {
        SearchableDocument doc = SearchableDocument.builder()
                .entityType("course")
                .entityId(node.get("id").asText())
                .title(getTextOrNull(node, "title"))
                .description(getTextOrNull(node, "description"))
                .content(getTextOrNull(node, "syllabus"))
                .category(getTextOrNull(node, "category"))
                .organizationId(getLongOrNull(node, "organizationId"))
                .authorId(getLongOrNull(node, "instructorId"))
                .authorName(getTextOrNull(node, "instructorName"))
                .tags(getListOrEmpty(node, "tags"))
                .difficulty(getTextOrNull(node, "difficulty"))
                .language(getTextOrNull(node, "language"))
                .rating(getDoubleOrNull(node, "rating"))
                .enrollmentCount(getIntOrZero(node, "enrollmentCount"))
                .isPublic(getBooleanOrFalse(node, "isPublic"))
                .status(getTextOrNull(node, "status"))
                .thumbnailUrl(getTextOrNull(node, "thumbnailUrl"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        searchService.indexDocument(doc);
    }

    private void indexUser(JsonNode node) {
        SearchableDocument doc = SearchableDocument.builder()
                .entityType("user")
                .entityId(node.get("id").asText())
                .title(getTextOrNull(node, "firstName") + " " + getTextOrNull(node, "lastName"))
                .description(getTextOrNull(node, "bio"))
                .content(getTextOrNull(node, "headline"))
                .organizationId(getLongOrNull(node, "organizationId"))
                .authorName(getTextOrNull(node, "firstName") + " " + getTextOrNull(node, "lastName"))
                .tags(getListOrEmpty(node, "skills"))
                .isPublic(getBooleanOrFalse(node, "profilePublic"))
                .thumbnailUrl(getTextOrNull(node, "avatarUrl"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        searchService.indexDocument(doc);
    }

    private void indexLesson(JsonNode node) {
        SearchableDocument doc = SearchableDocument.builder()
                .entityType("lesson")
                .entityId(node.get("id").asText())
                .title(getTextOrNull(node, "title"))
                .description(getTextOrNull(node, "description"))
                .content(getTextOrNull(node, "content"))
                .category(getTextOrNull(node, "courseTitle"))
                .organizationId(getLongOrNull(node, "organizationId"))
                .authorId(getLongOrNull(node, "courseInstructorId"))
                .parentId(getTextOrNull(node, "courseId"))
                .tags(getListOrEmpty(node, "tags"))
                .isPublic(getBooleanOrFalse(node, "isPublic"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        searchService.indexDocument(doc);
    }

    private void indexDocument(JsonNode node) {
        SearchableDocument doc = SearchableDocument.builder()
                .entityType("document")
                .entityId(node.get("id").asText())
                .title(getTextOrNull(node, "fileName"))
                .description(getTextOrNull(node, "description"))
                .content(getTextOrNull(node, "extractedText"))
                .organizationId(getLongOrNull(node, "organizationId"))
                .authorId(getLongOrNull(node, "uploadedBy"))
                .parentId(getTextOrNull(node, "courseId"))
                .tags(getListOrEmpty(node, "tags"))
                .isPublic(getBooleanOrFalse(node, "isPublic"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        searchService.indexDocument(doc);
    }

    private void indexForumPost(JsonNode node) {
        SearchableDocument doc = SearchableDocument.builder()
                .entityType("forum_post")
                .entityId(node.get("id").asText())
                .title(getTextOrNull(node, "title"))
                .description(getTextOrNull(node, "preview"))
                .content(getTextOrNull(node, "content"))
                .category(getTextOrNull(node, "forumName"))
                .organizationId(getLongOrNull(node, "organizationId"))
                .authorId(getLongOrNull(node, "authorId"))
                .authorName(getTextOrNull(node, "authorName"))
                .parentId(getTextOrNull(node, "courseId"))
                .tags(getListOrEmpty(node, "tags"))
                .isPublic(getBooleanOrFalse(node, "isPublic"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        searchService.indexDocument(doc);
    }

    private void indexOrganization(JsonNode node) {
        SearchableDocument doc = SearchableDocument.builder()
                .entityType("organization")
                .entityId(node.get("id").asText())
                .title(getTextOrNull(node, "name"))
                .description(getTextOrNull(node, "description"))
                .content(getTextOrNull(node, "industry"))
                .organizationId(getLongOrNull(node, "id"))
                .thumbnailUrl(getTextOrNull(node, "logoUrl"))
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        searchService.indexDocument(doc);
    }

    private void deleteDocument(String entityType, String entityId) {
        searchService.deleteDocument(entityType, entityId);
    }

    // Helper methods

    private String getTextOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }

    private Long getLongOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asLong() : null;
    }

    private Double getDoubleOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asDouble() : null;
    }

    private Integer getIntOrZero(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asInt() : 0;
    }

    private Boolean getBooleanOrFalse(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return fieldNode != null && !fieldNode.isNull() && fieldNode.asBoolean();
    }

    private List<String> getListOrEmpty(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode != null && fieldNode.isArray()) {
            return objectMapper.convertValue(fieldNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        }
        return List.of();
    }
}
