package com.freelms.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Event Service Application
 *
 * Manages live learning events:
 * - Webinars and workshops
 * - Live training sessions
 * - Video conference integrations (Zoom, Teams, Meet, BBB, Jitsi)
 * - Event registration and attendance tracking
 * - Calendar management and synchronization
 */
@SpringBootApplication(scanBasePackages = {"com.freelms.event", "com.freelms.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableScheduling
public class EventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }
}
