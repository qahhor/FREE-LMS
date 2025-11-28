package com.freelms.authoring.controller;

import com.freelms.authoring.entity.InteractiveContent;
import com.freelms.authoring.service.AuthoringService;
import com.freelms.authoring.service.H5PService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for interactive content authoring.
 */
@RestController
@RequestMapping("/api/v1/authoring")
@Tag(name = "Authoring", description = "Interactive content authoring (H5P, SCORM)")
public class AuthoringController {

    private final AuthoringService authoringService;

    public AuthoringController(AuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    @PostMapping("/content")
    @Operation(summary = "Create content", description = "Create new interactive content")
    public ResponseEntity<InteractiveContent> createContent(
            @RequestBody InteractiveContent content,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-Organization-Id", required = false) Long organizationId) {
        return ResponseEntity.ok(authoringService.createContent(content, userId, organizationId));
    }

    @PostMapping(value = "/import/h5p", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import H5P", description = "Import H5P content package")
    public ResponseEntity<InteractiveContent> importH5P(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-Organization-Id", required = false) Long organizationId) throws Exception {
        return ResponseEntity.ok(authoringService.importH5P(file, userId, organizationId));
    }

    @PostMapping(value = "/import/scorm", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import SCORM", description = "Import SCORM 1.2/2004 package")
    public ResponseEntity<InteractiveContent> importScorm(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-Organization-Id", required = false) Long organizationId) throws Exception {
        return ResponseEntity.ok(authoringService.importScorm(file, userId, organizationId));
    }

    @GetMapping("/content/{contentId}")
    @Operation(summary = "Get content", description = "Get interactive content by ID")
    public ResponseEntity<InteractiveContent> getContent(@PathVariable UUID contentId) {
        return ResponseEntity.ok(authoringService.getContent(contentId));
    }

    @PutMapping("/content/{contentId}")
    @Operation(summary = "Update content", description = "Update interactive content")
    public ResponseEntity<InteractiveContent> updateContent(
            @PathVariable UUID contentId,
            @RequestBody InteractiveContent updates) {
        return ResponseEntity.ok(authoringService.updateContent(contentId, updates));
    }

    @PostMapping("/content/{contentId}/version")
    @Operation(summary = "Create version", description = "Create new version of content")
    public ResponseEntity<InteractiveContent> createNewVersion(@PathVariable UUID contentId) {
        return ResponseEntity.ok(authoringService.createNewVersion(contentId));
    }

    @PostMapping("/content/{contentId}/publish")
    @Operation(summary = "Publish content", description = "Publish interactive content")
    public ResponseEntity<InteractiveContent> publishContent(@PathVariable UUID contentId) {
        return ResponseEntity.ok(authoringService.publishContent(contentId));
    }

    @GetMapping("/content/{contentId}/versions")
    @Operation(summary = "Get versions", description = "Get all versions of content")
    public ResponseEntity<List<InteractiveContent>> getContentVersions(@PathVariable UUID contentId) {
        return ResponseEntity.ok(authoringService.getContentVersions(contentId));
    }

    @GetMapping("/content")
    @Operation(summary = "List content", description = "List interactive content")
    public ResponseEntity<Page<InteractiveContent>> listContent(
            @RequestHeader(value = "X-Organization-Id", required = false) Long organizationId,
            @RequestParam(required = false) InteractiveContent.ContentStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(authoringService.listContent(organizationId, status, pageable));
    }

    @DeleteMapping("/content/{contentId}")
    @Operation(summary = "Delete content", description = "Delete interactive content")
    public ResponseEntity<Void> deleteContent(@PathVariable UUID contentId) {
        authoringService.deleteContent(contentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/h5p/content-types")
    @Operation(summary = "Get H5P types", description = "Get available H5P content types")
    public ResponseEntity<List<Map<String, Object>>> getH5PContentTypes() {
        return ResponseEntity.ok(authoringService.getH5PContentTypes());
    }

    @GetMapping("/scorm/{contentId}/launch")
    @Operation(summary = "Launch SCORM", description = "Get SCORM launch URL")
    public ResponseEntity<Map<String, String>> getScormLaunchUrl(
            @PathVariable UUID contentId,
            @RequestHeader("X-User-Id") Long userId) {
        String url = authoringService.getScormLaunchUrl(contentId, userId);
        return ResponseEntity.ok(Map.of("launchUrl", url));
    }

    @GetMapping("/content/{contentId}/embed")
    @Operation(summary = "Get embed", description = "Get content embed page")
    public ResponseEntity<String> getEmbedContent(@PathVariable UUID contentId) {
        InteractiveContent content = authoringService.getContent(contentId);
        if (content.getContentType() == InteractiveContent.ContentType.H5P) {
            // Return H5P embed HTML
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(content.getEmbedCode());
        }
        return ResponseEntity.ok(content.getEmbedCode());
    }
}
