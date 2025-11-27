package com.freelms.assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Assignment Review Service Application
 *
 * Centralized assignment management:
 * - Text, file, code, and URL submissions
 * - Review queue for instructors
 * - Auto-grading with code execution sandbox
 * - Plagiarism detection
 * - OCR for handwritten submissions
 * - Grading rubrics
 * - Appeal management
 */
@SpringBootApplication(scanBasePackages = {"com.freelms.assignment", "com.freelms.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableAsync
public class AssignmentReviewServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssignmentReviewServiceApplication.class, args);
    }
}
