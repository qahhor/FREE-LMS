package com.freelms.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Resource Booking Service Application
 *
 * Physical resource management for blended learning:
 * - Training rooms and conference rooms
 * - Equipment (projectors, laptops, cameras)
 * - Trainer/instructor scheduling
 * - Availability calendars
 * - Booking approvals
 * - Conflict resolution
 */
@SpringBootApplication(scanBasePackages = {"com.freelms.booking", "com.freelms.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableScheduling
public class ResourceBookingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResourceBookingServiceApplication.class, args);
    }
}
