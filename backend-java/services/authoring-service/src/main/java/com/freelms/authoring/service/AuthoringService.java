package com.freelms.authoring.service;

import com.freelms.authoring.entity.InteractiveContent;
import com.freelms.authoring.repository.InteractiveContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Service for interactive content authoring operations.
 */
@Service
public class AuthoringService {

    private static final Logger log = LoggerFactory.getLogger(AuthoringService.class);

    private final InteractiveContentRepository contentRepository;
    private final H5PService h5pService;
    private final ScormService scormService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AuthoringService(
            InteractiveContentRepository contentRepository,
            H5PService h5pService,
            ScormService scormService,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.contentRepository = contentRepository;
        this.h5pService = h5pService;
        this.scormService = scormService;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Create new interactive content.
     */
    @Transactional
    public InteractiveContent createContent(InteractiveContent content, Long authorId, Long organizationId) {
        content.setAuthorId(authorId);
        content.setOrganizationId(organizationId);
        content.setStatus(InteractiveContent.ContentStatus.DRAFT);
        content.setVersion(1);

        InteractiveContent saved = contentRepository.save(content);

        publishEvent("CONTENT_CREATED", saved);
        log.info("Created interactive content: {}", saved.getId());

        return saved;
    }

    /**
     * Import H5P package.
     */
    @Transactional
    public InteractiveContent importH5P(MultipartFile file, Long authorId, Long organizationId) throws Exception {
        log.info("Importing H5P package: {}", file.getOriginalFilename());

        // Parse H5P package
        H5PService.H5PMetadata metadata = h5pService.parseH5PPackage(file);

        InteractiveContent content = new InteractiveContent();
        content.setTitle(metadata.getTitle());
        content.setDescription(metadata.getDescription());
        content.setContentType(InteractiveContent.ContentType.H5P);
        content.setFormat(InteractiveContent.ContentFormat.H5P_PACKAGE);
        content.setH5pContentType(metadata.getContentType());
        content.setAuthorId(authorId);
        content.setOrganizationId(organizationId);
        content.setStatus(InteractiveContent.ContentStatus.DRAFT);

        // Store the package
        String storagePath = h5pService.storeH5PPackage(file, content.getId());
        content.setStoragePath(storagePath);
        content.setContentData(metadata.toJson());

        InteractiveContent saved = contentRepository.save(content);

        publishEvent("H5P_IMPORTED", saved);

        return saved;
    }

    /**
     * Import SCORM package.
     */
    @Transactional
    public InteractiveContent importScorm(MultipartFile file, Long authorId, Long organizationId) throws Exception {
        log.info("Importing SCORM package: {}", file.getOriginalFilename());

        // Parse SCORM manifest
        ScormService.ScormMetadata metadata = scormService.parseScormPackage(file);

        InteractiveContent content = new InteractiveContent();
        content.setTitle(metadata.getTitle());
        content.setDescription(metadata.getDescription());
        content.setContentType(InteractiveContent.ContentType.SCORM);
        content.setFormat(InteractiveContent.ContentFormat.SCORM_PACKAGE);
        content.setScormVersion(metadata.getVersion());
        content.setAuthorId(authorId);
        content.setOrganizationId(organizationId);
        content.setStatus(InteractiveContent.ContentStatus.DRAFT);

        // Store the package
        String storagePath = scormService.storeScormPackage(file, content.getId());
        content.setStoragePath(storagePath);
        content.setContentData(metadata.toJson());

        if (metadata.getMasteryScore() != null) {
            content.setPassingScore(metadata.getMasteryScore());
        }

        InteractiveContent saved = contentRepository.save(content);

        publishEvent("SCORM_IMPORTED", saved);

        return saved;
    }

    /**
     * Get content by ID.
     */
    public InteractiveContent getContent(UUID contentId) {
        return contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found: " + contentId));
    }

    /**
     * Update content.
     */
    @Transactional
    public InteractiveContent updateContent(UUID contentId, InteractiveContent updates) {
        InteractiveContent content = getContent(contentId);

        content.setTitle(updates.getTitle());
        content.setDescription(updates.getDescription());
        content.setContentData(updates.getContentData());
        content.setMaxScore(updates.getMaxScore());
        content.setPassingScore(updates.getPassingScore());
        content.setTimeLimitMinutes(updates.getTimeLimitMinutes());
        content.setAttemptLimit(updates.getAttemptLimit());
        content.setShowFeedback(updates.isShowFeedback());
        content.setShuffleQuestions(updates.isShuffleQuestions());
        content.setAllowReview(updates.isAllowReview());

        InteractiveContent saved = contentRepository.save(content);

        publishEvent("CONTENT_UPDATED", saved);

        return saved;
    }

    /**
     * Create new version of content.
     */
    @Transactional
    public InteractiveContent createNewVersion(UUID contentId) {
        InteractiveContent original = getContent(contentId);

        InteractiveContent newVersion = new InteractiveContent();
        newVersion.setTitle(original.getTitle());
        newVersion.setDescription(original.getDescription());
        newVersion.setContentType(original.getContentType());
        newVersion.setFormat(original.getFormat());
        newVersion.setH5pContentType(original.getH5pContentType());
        newVersion.setScormVersion(original.getScormVersion());
        newVersion.setContentData(original.getContentData());
        newVersion.setStoragePath(original.getStoragePath());
        newVersion.setOrganizationId(original.getOrganizationId());
        newVersion.setAuthorId(original.getAuthorId());
        newVersion.setCourseId(original.getCourseId());
        newVersion.setLessonId(original.getLessonId());
        newVersion.setStatus(InteractiveContent.ContentStatus.DRAFT);
        newVersion.setVersion(original.getVersion() + 1);
        newVersion.setParentContentId(original.getParentContentId() != null ?
                original.getParentContentId() : original.getId());
        newVersion.setMaxScore(original.getMaxScore());
        newVersion.setPassingScore(original.getPassingScore());
        newVersion.setTimeLimitMinutes(original.getTimeLimitMinutes());
        newVersion.setAttemptLimit(original.getAttemptLimit());
        newVersion.setShowFeedback(original.isShowFeedback());
        newVersion.setShuffleQuestions(original.isShuffleQuestions());
        newVersion.setAllowReview(original.isAllowReview());

        InteractiveContent saved = contentRepository.save(newVersion);

        log.info("Created new version {} of content {}", saved.getVersion(), original.getId());

        return saved;
    }

    /**
     * Publish content.
     */
    @Transactional
    public InteractiveContent publishContent(UUID contentId) {
        InteractiveContent content = getContent(contentId);

        content.setStatus(InteractiveContent.ContentStatus.PUBLISHED);
        content.setPublishedAt(LocalDateTime.now());

        // Generate embed code
        content.setEmbedCode(generateEmbedCode(content));

        InteractiveContent saved = contentRepository.save(content);

        publishEvent("CONTENT_PUBLISHED", saved);

        return saved;
    }

    /**
     * Get content versions.
     */
    public List<InteractiveContent> getContentVersions(UUID contentId) {
        InteractiveContent content = getContent(contentId);
        UUID parentId = content.getParentContentId() != null ? content.getParentContentId() : content.getId();
        return contentRepository.findByParentContentIdOrderByVersionDesc(parentId);
    }

    /**
     * List content by organization.
     */
    public Page<InteractiveContent> listContent(Long organizationId, InteractiveContent.ContentStatus status,
                                                 Pageable pageable) {
        if (status != null) {
            return contentRepository.findByOrganizationIdAndStatus(organizationId, status, pageable);
        }
        return contentRepository.findByOrganizationId(organizationId, pageable);
    }

    /**
     * Delete content.
     */
    @Transactional
    public void deleteContent(UUID contentId) {
        InteractiveContent content = getContent(contentId);

        // Delete storage files
        if (content.getStoragePath() != null) {
            try {
                Files.deleteIfExists(Path.of(content.getStoragePath()));
            } catch (IOException e) {
                log.warn("Failed to delete storage file: {}", e.getMessage());
            }
        }

        contentRepository.delete(content);

        publishEvent("CONTENT_DELETED", Map.of("contentId", contentId));
    }

    /**
     * Get available H5P content types.
     */
    public List<Map<String, Object>> getH5PContentTypes() {
        return h5pService.getAvailableContentTypes();
    }

    /**
     * Get SCORM launch URL.
     */
    public String getScormLaunchUrl(UUID contentId, Long userId) {
        InteractiveContent content = getContent(contentId);
        if (content.getContentType() != InteractiveContent.ContentType.SCORM) {
            throw new IllegalArgumentException("Content is not a SCORM package");
        }
        return scormService.generateLaunchUrl(content, userId);
    }

    private String generateEmbedCode(InteractiveContent content) {
        return String.format(
                "<iframe src=\"/api/v1/authoring/content/%s/embed\" " +
                "width=\"100%%\" height=\"600\" frameborder=\"0\" " +
                "allowfullscreen=\"allowfullscreen\"></iframe>",
                content.getId()
        );
    }

    private void publishEvent(String eventType, Object data) {
        try {
            kafkaTemplate.send("authoring-events", Map.of("eventType", eventType, "data", data));
        } catch (Exception e) {
            log.error("Failed to publish event: {}", e.getMessage());
        }
    }
}
