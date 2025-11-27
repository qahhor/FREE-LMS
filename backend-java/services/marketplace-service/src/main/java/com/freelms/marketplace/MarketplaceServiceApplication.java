package com.freelms.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Smartup LMS - Marketplace Service
 *
 * Provides marketplace functionality for:
 * - Functional modules and plugins
 * - Ready-made educational courses
 * - Third-party integrations
 * - Extensions and add-ons
 */
@SpringBootApplication(scanBasePackages = {"com.freelms.marketplace", "com.freelms.common"})
@EnableDiscoveryClient
@EnableCaching
@EnableAsync
@EnableScheduling
public class MarketplaceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketplaceServiceApplication.class, args);
    }
}
