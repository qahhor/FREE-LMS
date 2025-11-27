package com.freelms.lti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * LTI Service Application
 *
 * Learning Tools Interoperability integration:
 * - LTI 1.1 legacy support
 * - LTI 1.3 with OAuth 2.0
 * - LTI Advantage (AGS, NRPS, Deep Linking)
 * - Provider mode (FREE LMS as tool)
 * - Consumer mode (external tools in FREE LMS)
 * - Grade passback
 * - Roster sync
 */
@SpringBootApplication(scanBasePackages = {"com.freelms.lti", "com.freelms.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class LtiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LtiServiceApplication.class, args);
    }
}
