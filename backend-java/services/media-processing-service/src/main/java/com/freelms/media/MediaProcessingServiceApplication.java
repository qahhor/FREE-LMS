package com.freelms.media;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Media Processing Service Application
 *
 * Handles video transcoding, image processing, and document text extraction:
 * - Video transcoding to HLS/DASH for adaptive streaming
 * - Thumbnail generation
 * - Image compression and resizing
 * - Document text extraction (PDF, DOCX, PPTX)
 * - Audio transcription integration
 */
@SpringBootApplication(scanBasePackages = {"com.freelms.media", "com.freelms.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableAsync
public class MediaProcessingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediaProcessingServiceApplication.class, args);
    }
}
