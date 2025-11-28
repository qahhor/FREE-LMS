package com.freelms.media.service;

import com.freelms.media.entity.MediaFile;
import com.freelms.media.entity.MediaVariant;
import com.freelms.media.repository.MediaFileRepository;
import com.freelms.media.repository.MediaVariantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Service for processing media files asynchronously.
 */
@Service
public class MediaProcessingJobService {

    private static final Logger log = LoggerFactory.getLogger(MediaProcessingJobService.class);

    private final MediaFileRepository mediaFileRepository;
    private final MediaVariantRepository mediaVariantRepository;
    private final FFmpegService ffmpegService;
    private final MinioStorageService storageService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public MediaProcessingJobService(
            MediaFileRepository mediaFileRepository,
            MediaVariantRepository mediaVariantRepository,
            FFmpegService ffmpegService,
            MinioStorageService storageService,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.mediaFileRepository = mediaFileRepository;
        this.mediaVariantRepository = mediaVariantRepository;
        this.ffmpegService = ffmpegService;
        this.storageService = storageService;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Process a media file asynchronously.
     */
    @Async
    @Transactional
    public void processMedia(UUID mediaId) {
        log.info("Starting processing for media: {}", mediaId);

        MediaFile mediaFile = mediaFileRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found: " + mediaId));

        try {
            updateStatus(mediaFile, MediaFile.ProcessingStatus.PROCESSING, 0);
            publishEvent("PROCESSING_STARTED", mediaId, Map.of("mediaId", mediaId.toString()));

            // Download raw file to temp location
            Path tempDir = Files.createTempDirectory("media-" + mediaId);
            Path rawFile = tempDir.resolve(mediaFile.getOriginalFilename());

            try (var inputStream = storageService.downloadRaw(mediaFile.getStoragePath())) {
                Files.copy(inputStream, rawFile);
            }

            // Get metadata
            FFmpegService.VideoMetadata metadata = ffmpegService.getVideoMetadata(rawFile.toString());
            updateMediaMetadata(mediaFile, metadata);
            updateStatus(mediaFile, MediaFile.ProcessingStatus.PROCESSING, 10);

            // Generate thumbnails
            Path thumbnailDir = tempDir.resolve("thumbnails");
            FFmpegService.ThumbnailResult thumbnailResult = ffmpegService.generateThumbnails(
                    rawFile.toString(), thumbnailDir.toString());

            if (thumbnailResult.isSuccess()) {
                storageService.uploadThumbnails(thumbnailDir, mediaId);
                mediaFile.setThumbnailPath("thumbnails/" + mediaId + "/medium.jpg");
                updateStatus(mediaFile, MediaFile.ProcessingStatus.PROCESSING, 25);
            }

            // Transcode to HLS
            Path hlsDir = tempDir.resolve("hls");
            FFmpegService.TranscodeResult transcodeResult = ffmpegService.transcodeToHLS(
                    rawFile.toString(),
                    hlsDir.toString(),
                    (quality, progress) -> {
                        int overallProgress = 25 + (int)(progress * 0.7);
                        updateStatus(mediaFile, MediaFile.ProcessingStatus.PROCESSING, overallProgress);
                    }
            ).get();

            if (transcodeResult.isSuccess()) {
                storageService.uploadProcessed(hlsDir, mediaId);

                // Create variant records
                createVariants(mediaFile, hlsDir);

                mediaFile.setStreamingPath("processed/" + mediaId + "/master.m3u8");
                updateStatus(mediaFile, MediaFile.ProcessingStatus.COMPLETED, 100);

                publishEvent("PROCESSING_COMPLETED", mediaId, Map.of(
                        "mediaId", mediaId.toString(),
                        "streamingPath", mediaFile.getStreamingPath(),
                        "thumbnailPath", mediaFile.getThumbnailPath()
                ));
            } else {
                throw new RuntimeException(transcodeResult.getError());
            }

            // Cleanup temp files
            deleteDirectory(tempDir);

            log.info("Processing completed for media: {}", mediaId);

        } catch (Exception e) {
            log.error("Processing failed for media {}: {}", mediaId, e.getMessage(), e);
            updateStatus(mediaFile, MediaFile.ProcessingStatus.FAILED, 0);
            mediaFile.setProcessingError(e.getMessage());
            mediaFileRepository.save(mediaFile);

            publishEvent("PROCESSING_FAILED", mediaId, Map.of(
                    "mediaId", mediaId.toString(),
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Retry failed processing job.
     */
    @Transactional
    public void retryProcessing(UUID mediaId) {
        MediaFile mediaFile = mediaFileRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found: " + mediaId));

        if (mediaFile.getProcessingStatus() != MediaFile.ProcessingStatus.FAILED) {
            throw new IllegalStateException("Can only retry failed jobs");
        }

        if (mediaFile.getRetryCount() >= 3) {
            throw new IllegalStateException("Maximum retry attempts reached");
        }

        mediaFile.setRetryCount(mediaFile.getRetryCount() + 1);
        mediaFile.setProcessingError(null);
        mediaFileRepository.save(mediaFile);

        processMedia(mediaId);
    }

    private void updateStatus(MediaFile mediaFile, MediaFile.ProcessingStatus status, int progress) {
        mediaFile.setProcessingStatus(status);
        mediaFile.setProcessingProgress(progress);
        if (status == MediaFile.ProcessingStatus.COMPLETED) {
            mediaFile.setProcessingCompletedAt(LocalDateTime.now());
        }
        mediaFileRepository.save(mediaFile);
    }

    private void updateMediaMetadata(MediaFile mediaFile, FFmpegService.VideoMetadata metadata) {
        mediaFile.setDurationSeconds(metadata.getDurationSeconds());
        mediaFile.setWidth(metadata.getWidth());
        mediaFile.setHeight(metadata.getHeight());
        mediaFile.setCodec(metadata.getCodec());
        mediaFile.setBitrate(metadata.getBitrate());
        mediaFile.setFrameRate(metadata.getFrameRate());
        mediaFileRepository.save(mediaFile);
    }

    private void createVariants(MediaFile mediaFile, Path hlsDir) throws Exception {
        String[] qualities = {"1080p", "720p", "480p", "360p"};
        int[][] specs = {{1920, 1080, 5000}, {1280, 720, 2500}, {854, 480, 1200}, {640, 360, 800}};

        for (int i = 0; i < qualities.length; i++) {
            Path qualityDir = hlsDir.resolve(qualities[i].replace("p", "") + "p");
            if (Files.exists(qualityDir)) {
                MediaVariant variant = new MediaVariant();
                variant.setMediaFile(mediaFile);
                variant.setQuality(qualities[i]);
                variant.setWidth(specs[i][0]);
                variant.setHeight(specs[i][1]);
                variant.setBitrate(specs[i][2]);
                variant.setFormat("HLS");
                variant.setStoragePath("processed/" + mediaFile.getId() + "/" + qualities[i].replace("p", "") + "p/playlist.m3u8");
                mediaVariantRepository.save(variant);
            }
        }
    }

    private void publishEvent(String eventType, UUID mediaId, Map<String, Object> data) {
        try {
            kafkaTemplate.send("media-events", mediaId.toString(),
                    Map.of("eventType", eventType, "data", data, "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("Failed to publish event: {}", e.getMessage());
        }
    }

    private void deleteDirectory(Path dir) {
        try {
            Files.walk(dir)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception e) {
                            log.warn("Failed to delete: {}", path);
                        }
                    });
        } catch (Exception e) {
            log.warn("Failed to cleanup temp directory: {}", e.getMessage());
        }
    }
}
