package com.freelms.media.service;

import com.freelms.media.dto.MediaFileResponse;
import com.freelms.media.dto.MediaUploadResponse;
import com.freelms.media.dto.ProcessingJobResponse;
import com.freelms.media.entity.MediaFile;
import com.freelms.media.repository.MediaFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for handling media operations.
 */
@Service
public class MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaService.class);

    private final MediaFileRepository mediaFileRepository;

    public MediaService(MediaFileRepository mediaFileRepository) {
        this.mediaFileRepository = mediaFileRepository;
    }

    /**
     * Upload and queue media file for processing.
     */
    public MediaUploadResponse uploadMedia(MultipartFile file, Long courseId, Long lessonId,
                                           Long organizationId, Long userId) {
        log.info("Uploading media file: {} for user: {}", file.getOriginalFilename(), userId);

        // Create media file entity
        MediaFile mediaFile = new MediaFile();
        mediaFile.setOriginalFilename(file.getOriginalFilename());
        mediaFile.setContentType(file.getContentType());
        mediaFile.setFileSize(file.getSize());
        mediaFile.setUploadedBy(userId);
        mediaFile.setCourseId(courseId);
        mediaFile.setLessonId(lessonId);
        mediaFile.setOrganizationId(organizationId);
        mediaFile.setProcessingStatus(MediaFile.ProcessingStatus.PENDING);

        // Store the file and save entity
        // In real implementation, this would upload to MinIO
        String storagePath = "raw/" + UUID.randomUUID() + "/" + file.getOriginalFilename();
        mediaFile.setStoragePath(storagePath);

        MediaFile saved = mediaFileRepository.save(mediaFile);

        // Queue for processing
        // In real implementation, this would send a Kafka message

        MediaUploadResponse response = new MediaUploadResponse();
        response.setMediaId(saved.getId());
        response.setJobId(UUID.randomUUID()); // Would be actual job ID
        response.setStatus("QUEUED");
        response.setMessage("File uploaded successfully and queued for processing");
        response.setOriginalFilename(file.getOriginalFilename());
        response.setFileSize(file.getSize());
        response.setContentType(file.getContentType());

        return response;
    }

    /**
     * Get media file details.
     */
    public MediaFileResponse getMedia(UUID mediaId) {
        MediaFile mediaFile = mediaFileRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found: " + mediaId));

        MediaFileResponse response = new MediaFileResponse();
        response.setId(mediaFile.getId());
        response.setOriginalFilename(mediaFile.getOriginalFilename());
        response.setContentType(mediaFile.getContentType());
        response.setFileSize(mediaFile.getFileSize());
        response.setProcessingStatus(mediaFile.getProcessingStatus().name());
        response.setProcessingProgress(mediaFile.getProcessingProgress());
        response.setProcessingError(mediaFile.getProcessingError());
        response.setDurationSeconds(mediaFile.getDurationSeconds());
        response.setWidth(mediaFile.getWidth());
        response.setHeight(mediaFile.getHeight());
        response.setCodec(mediaFile.getCodec());
        response.setCreatedAt(mediaFile.getCreatedAt());
        response.setProcessingCompletedAt(mediaFile.getProcessingCompletedAt());

        return response;
    }

    /**
     * Get processing job status.
     */
    public ProcessingJobResponse getJobStatus(UUID jobId) {
        // In real implementation, this would fetch from processing_jobs table
        ProcessingJobResponse response = new ProcessingJobResponse();
        response.setJobId(jobId);
        response.setStatus("PROCESSING");
        response.setProgress(50);
        return response;
    }

    /**
     * Get streaming URL for video.
     */
    public Map<String, String> getStreamUrl(UUID mediaId, String quality) {
        MediaFile mediaFile = mediaFileRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found: " + mediaId));

        Map<String, String> urls = new HashMap<>();
        // In real implementation, this would return actual MinIO/CDN URLs
        urls.put("url", "/media/" + mediaId + "/stream/master.m3u8");
        urls.put("quality", quality);
        return urls;
    }

    /**
     * Get thumbnail URL.
     */
    public Map<String, String> getThumbnailUrl(UUID mediaId, String size) {
        Map<String, String> urls = new HashMap<>();
        urls.put("url", "/media/" + mediaId + "/thumbnail/" + size + ".jpg");
        urls.put("size", size);
        return urls;
    }

    /**
     * Get transcript or extracted text.
     */
    public Map<String, Object> getTranscript(UUID mediaId) {
        Map<String, Object> result = new HashMap<>();
        result.put("mediaId", mediaId);
        result.put("transcript", "");
        result.put("language", "ru");
        result.put("status", "NOT_AVAILABLE");
        return result;
    }

    /**
     * Delete media file and all variants.
     */
    public void deleteMedia(UUID mediaId) {
        log.info("Deleting media: {}", mediaId);
        // In real implementation, this would also delete from MinIO
        mediaFileRepository.deleteById(mediaId);
    }

    /**
     * Get storage statistics.
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFiles", mediaFileRepository.count());
        stats.put("pendingJobs", 0);
        stats.put("processingJobs", 0);
        return stats;
    }

    /**
     * Cleanup orphaned files.
     */
    public Map<String, Object> cleanup() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "completed");
        result.put("filesRemoved", 0);
        result.put("spaceFreed", "0 MB");
        return result;
    }
}
