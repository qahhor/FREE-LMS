package com.freelms.media.service;

import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service for MinIO object storage operations.
 */
@Service
public class MinioStorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioStorageService.class);

    @Value("${minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${minio.secret-key:minioadmin}")
    private String secretKey;

    @Value("${minio.bucket.raw:lms-media-raw}")
    private String rawBucket;

    @Value("${minio.bucket.processed:lms-media-processed}")
    private String processedBucket;

    @Value("${minio.bucket.thumbnails:lms-media-thumbnails}")
    private String thumbnailsBucket;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        try {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            // Ensure buckets exist
            ensureBucketExists(rawBucket);
            ensureBucketExists(processedBucket);
            ensureBucketExists(thumbnailsBucket);

            log.info("MinIO client initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize MinIO client: {}", e.getMessage(), e);
        }
    }

    /**
     * Upload a file to raw bucket.
     */
    public String uploadRaw(MultipartFile file, UUID mediaId) {
        String objectName = "raw/" + mediaId + "/" + file.getOriginalFilename();
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(rawBucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            log.info("Uploaded raw file: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("Failed to upload raw file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    /**
     * Upload processed HLS files.
     */
    public void uploadProcessed(Path localDir, UUID mediaId) {
        try {
            String basePath = "processed/" + mediaId;
            Files.walk(localDir)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            String relativePath = localDir.relativize(file).toString();
                            String objectName = basePath + "/" + relativePath;

                            String contentType = Files.probeContentType(file);
                            if (contentType == null) {
                                if (file.toString().endsWith(".m3u8")) {
                                    contentType = "application/x-mpegURL";
                                } else if (file.toString().endsWith(".ts")) {
                                    contentType = "video/MP2T";
                                }
                            }

                            minioClient.putObject(PutObjectArgs.builder()
                                    .bucket(processedBucket)
                                    .object(objectName)
                                    .stream(Files.newInputStream(file), Files.size(file), -1)
                                    .contentType(contentType)
                                    .build());

                            log.debug("Uploaded processed file: {}", objectName);
                        } catch (Exception e) {
                            log.error("Failed to upload processed file: {}", e.getMessage());
                        }
                    });

            log.info("Uploaded all processed files for media: {}", mediaId);
        } catch (Exception e) {
            log.error("Failed to upload processed files: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload processed files", e);
        }
    }

    /**
     * Upload thumbnails.
     */
    public void uploadThumbnails(Path localDir, UUID mediaId) {
        try {
            String basePath = "thumbnails/" + mediaId;
            Files.walk(localDir)
                    .filter(Files::isRegularFile)
                    .filter(f -> f.toString().endsWith(".jpg") || f.toString().endsWith(".png"))
                    .forEach(file -> {
                        try {
                            String fileName = file.getFileName().toString();
                            String objectName = basePath + "/" + fileName;

                            minioClient.putObject(PutObjectArgs.builder()
                                    .bucket(thumbnailsBucket)
                                    .object(objectName)
                                    .stream(Files.newInputStream(file), Files.size(file), -1)
                                    .contentType("image/jpeg")
                                    .build());

                            log.debug("Uploaded thumbnail: {}", objectName);
                        } catch (Exception e) {
                            log.error("Failed to upload thumbnail: {}", e.getMessage());
                        }
                    });

            log.info("Uploaded thumbnails for media: {}", mediaId);
        } catch (Exception e) {
            log.error("Failed to upload thumbnails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload thumbnails", e);
        }
    }

    /**
     * Download file from raw bucket.
     */
    public InputStream downloadRaw(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(rawBucket)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("Failed to download raw file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    /**
     * Generate presigned URL for streaming.
     */
    public String getPresignedStreamUrl(UUID mediaId, String path) {
        try {
            String objectName = "processed/" + mediaId + "/" + path;
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(processedBucket)
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(4, TimeUnit.HOURS)
                    .build());
        } catch (Exception e) {
            log.error("Failed to generate presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate URL", e);
        }
    }

    /**
     * Generate presigned URL for thumbnail.
     */
    public String getPresignedThumbnailUrl(UUID mediaId, String size) {
        try {
            String objectName = "thumbnails/" + mediaId + "/" + size + ".jpg";
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(thumbnailsBucket)
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(24, TimeUnit.HOURS)
                    .build());
        } catch (Exception e) {
            log.error("Failed to generate thumbnail URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate URL", e);
        }
    }

    /**
     * Delete all files for a media.
     */
    public void deleteMediaFiles(UUID mediaId) {
        try {
            // Delete raw files
            deleteObjectsWithPrefix(rawBucket, "raw/" + mediaId + "/");

            // Delete processed files
            deleteObjectsWithPrefix(processedBucket, "processed/" + mediaId + "/");

            // Delete thumbnails
            deleteObjectsWithPrefix(thumbnailsBucket, "thumbnails/" + mediaId + "/");

            log.info("Deleted all files for media: {}", mediaId);
        } catch (Exception e) {
            log.error("Failed to delete media files: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete media files", e);
        }
    }

    /**
     * Get storage statistics.
     */
    public StorageStats getStorageStats() {
        StorageStats stats = new StorageStats();
        stats.setRawBucketSize(getBucketSize(rawBucket));
        stats.setProcessedBucketSize(getBucketSize(processedBucket));
        stats.setThumbnailsBucketSize(getBucketSize(thumbnailsBucket));
        return stats;
    }

    private void ensureBucketExists(String bucket) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucket)
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucket)
                        .build());
                log.info("Created bucket: {}", bucket);
            }
        } catch (Exception e) {
            log.error("Failed to ensure bucket exists: {}", e.getMessage(), e);
        }
    }

    private void deleteObjectsWithPrefix(String bucket, String prefix) {
        try {
            var objects = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .recursive(true)
                    .build());

            for (var result : objects) {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(result.get().objectName())
                        .build());
            }
        } catch (Exception e) {
            log.error("Failed to delete objects: {}", e.getMessage());
        }
    }

    private long getBucketSize(String bucket) {
        try {
            long totalSize = 0;
            var objects = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucket)
                    .recursive(true)
                    .build());

            for (var result : objects) {
                totalSize += result.get().size();
            }
            return totalSize;
        } catch (Exception e) {
            log.error("Failed to get bucket size: {}", e.getMessage());
            return 0;
        }
    }

    public static class StorageStats {
        private long rawBucketSize;
        private long processedBucketSize;
        private long thumbnailsBucketSize;

        public long getRawBucketSize() { return rawBucketSize; }
        public void setRawBucketSize(long rawBucketSize) { this.rawBucketSize = rawBucketSize; }
        public long getProcessedBucketSize() { return processedBucketSize; }
        public void setProcessedBucketSize(long processedBucketSize) { this.processedBucketSize = processedBucketSize; }
        public long getThumbnailsBucketSize() { return thumbnailsBucketSize; }
        public void setThumbnailsBucketSize(long thumbnailsBucketSize) { this.thumbnailsBucketSize = thumbnailsBucketSize; }
        public long getTotalSize() { return rawBucketSize + processedBucketSize + thumbnailsBucketSize; }
    }
}
