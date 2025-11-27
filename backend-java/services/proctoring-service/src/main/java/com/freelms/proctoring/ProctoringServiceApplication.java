package com.freelms.proctoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Proctoring Service Application
 *
 * AI-powered exam integrity monitoring:
 * - Webcam analysis (face detection, gaze tracking)
 * - Screen recording and monitoring
 * - Audio detection for unauthorized communication
 * - Browser lockdown (tab switching, copy-paste blocking)
 * - Identity verification
 * - Live and recorded proctoring modes
 */
@SpringBootApplication(scanBasePackages = {"com.freelms.proctoring", "com.freelms.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class ProctoringServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProctoringServiceApplication.class, args);
    }
}
