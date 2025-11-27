package com.freelms.media.controller;

import com.freelms.media.dto.MediaFileResponse;
import com.freelms.media.dto.MediaUploadResponse;
import com.freelms.media.dto.ProcessingJobResponse;
import com.freelms.media.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for media operations.
 */
@RestController
@RequestMapping("/api/v1/media")
@Tag(name = "Media", description = "Media upload and processing operations")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    /**
     * Upload a media file for processing.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload media", description = "Upload a media file for processing (video, image, document)")
    public ResponseEntity<MediaUploadResponse> uploadMedia(
            @Parameter(description = "Media file") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Course ID") @RequestParam(required = false) Long courseId,
            @Parameter(description = "Lesson ID") @RequestParam(required = false) Long lessonId,
            @Parameter(description = "Organization ID") @RequestParam(required = false) Long organizationId,
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(mediaService.uploadMedia(file, courseId, lessonId, organizationId, userId));
    }

    /**
     * Get media file details.
     */
    @GetMapping("/{mediaId}")
    @Operation(summary = "Get media", description = "Get media file details and processing status")
    public ResponseEntity<MediaFileResponse> getMedia(
            @Parameter(description = "Media ID") @PathVariable UUID mediaId) {
        return ResponseEntity.ok(mediaService.getMedia(mediaId));
    }

    /**
     * Get processing job status.
     */
    @GetMapping("/jobs/{jobId}")
    @Operation(summary = "Get job status", description = "Get processing job status")
    public ResponseEntity<ProcessingJobResponse> getJobStatus(
            @Parameter(description = "Job ID") @PathVariable UUID jobId) {
        return ResponseEntity.ok(mediaService.getJobStatus(jobId));
    }

    /**
     * Get HLS streaming manifest.
     */
    @GetMapping("/{mediaId}/stream")
    @Operation(summary = "Get stream URL", description = "Get HLS streaming manifest URL")
    public ResponseEntity<Map<String, String>> getStreamUrl(
            @Parameter(description = "Media ID") @PathVariable UUID mediaId,
            @Parameter(description = "Quality") @RequestParam(defaultValue = "auto") String quality) {
        return ResponseEntity.ok(mediaService.getStreamUrl(mediaId, quality));
    }

    /**
     * Get thumbnail URL.
     */
    @GetMapping("/{mediaId}/thumbnail")
    @Operation(summary = "Get thumbnail", description = "Get thumbnail URL for media")
    public ResponseEntity<Map<String, String>> getThumbnail(
            @Parameter(description = "Media ID") @PathVariable UUID mediaId,
            @Parameter(description = "Size") @RequestParam(defaultValue = "medium") String size) {
        return ResponseEntity.ok(mediaService.getThumbnailUrl(mediaId, size));
    }

    /**
     * Get transcript/extracted text.
     */
    @GetMapping("/{mediaId}/transcript")
    @Operation(summary = "Get transcript", description = "Get video transcript or extracted document text")
    public ResponseEntity<Map<String, Object>> getTranscript(
            @Parameter(description = "Media ID") @PathVariable UUID mediaId) {
        return ResponseEntity.ok(mediaService.getTranscript(mediaId));
    }

    /**
     * Delete media file and all variants.
     */
    @DeleteMapping("/{mediaId}")
    @Operation(summary = "Delete media", description = "Delete media file and all processed variants")
    public ResponseEntity<Void> deleteMedia(
            @Parameter(description = "Media ID") @PathVariable UUID mediaId) {
        mediaService.deleteMedia(mediaId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get storage statistics.
     */
    @GetMapping("/stats")
    @Operation(summary = "Get stats", description = "Get storage and processing statistics")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(mediaService.getStats());
    }

    /**
     * Trigger cleanup of orphaned files.
     */
    @PostMapping("/cleanup")
    @Operation(summary = "Cleanup", description = "Remove orphaned files from storage (admin only)")
    public ResponseEntity<Map<String, Object>> cleanup() {
        return ResponseEntity.ok(mediaService.cleanup());
    }
}
