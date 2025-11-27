package com.freelms.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Audit Logging Service Application
 *
 * Centralized compliance and security logging:
 * - Immutable audit trail
 * - User activity tracking
 * - Security event monitoring
 * - GDPR data access reports
 * - SOC2/ISO 27001 compliance
 * - Alert rules and notifications
 * - Log archival and retention
 */
@SpringBootApplication(scanBasePackages = {"com.freelms.audit", "com.freelms.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableScheduling
public class AuditLoggingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuditLoggingServiceApplication.class, args);
    }
}
