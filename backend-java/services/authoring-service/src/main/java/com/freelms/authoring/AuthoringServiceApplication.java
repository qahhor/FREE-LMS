package com.freelms.authoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Authoring Service Application
 *
 * Content creation and editing platform:
 * - H5P interactive content (videos, presentations, timeline)
 * - SCORM 1.2/2004 package creation and import
 * - Rich text editor (Notion-like)
 * - Visual quiz builder
 * - Content versioning and collaboration
 */
@SpringBootApplication(scanBasePackages = {"com.freelms.authoring", "com.freelms.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class AuthoringServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthoringServiceApplication.class, args);
    }
}
