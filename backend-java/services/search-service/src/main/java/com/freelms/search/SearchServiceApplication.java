package com.freelms.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Search Service Application
 *
 * Provides unified full-text search across all LMS entities:
 * - Courses, Lessons, Documents
 * - Users, Organizations
 * - Forum posts, Comments
 *
 * Uses Elasticsearch for indexing and search operations.
 */
@SpringBootApplication(scanBasePackages = {"com.freelms.search", "com.freelms.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class SearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
