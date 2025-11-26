package com.freelms.integration.token;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Smartup LMS - API Token Entity
 *
 * Represents an API token for external system integration.
 * Supports scoped permissions and rate limiting.
 */
@Entity
@Table(name = "api_tokens", indexes = {
    @Index(name = "idx_api_token_key", columnList = "tokenKey", unique = true),
    @Index(name = "idx_api_token_org", columnList = "organizationId"),
    @Index(name = "idx_api_token_active", columnList = "active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenKey;

    @Column(nullable = false)
    private String tokenHash;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Long organizationId;

    @Column(nullable = false)
    private Long createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType type;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "api_token_scopes", joinColumns = @JoinColumn(name = "token_id"))
    @Column(name = "scope")
    @Builder.Default
    private Set<String> scopes = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    private Instant expiresAt;

    private Instant lastUsedAt;

    private String lastUsedIp;

    @Column(nullable = false)
    @Builder.Default
    private Long requestCount = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Integer rateLimit = 1000; // requests per minute

    @Column(nullable = false)
    @Builder.Default
    private Integer rateLimitWindow = 60; // seconds

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "api_token_allowed_ips", joinColumns = @JoinColumn(name = "token_id"))
    @Column(name = "ip_address")
    @Builder.Default
    private Set<String> allowedIps = new HashSet<>();

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public enum TokenType {
        READ_ONLY,      // Can only read data
        READ_WRITE,     // Can read and write
        WEBHOOK,        // For incoming webhooks
        FULL_ACCESS     // Full API access (admin)
    }

    /**
     * Available API scopes
     */
    public static class Scopes {
        public static final String USERS_READ = "users:read";
        public static final String USERS_WRITE = "users:write";
        public static final String COURSES_READ = "courses:read";
        public static final String COURSES_WRITE = "courses:write";
        public static final String ENROLLMENTS_READ = "enrollments:read";
        public static final String ENROLLMENTS_WRITE = "enrollments:write";
        public static final String PROGRESS_READ = "progress:read";
        public static final String PROGRESS_WRITE = "progress:write";
        public static final String REPORTS_READ = "reports:read";
        public static final String WEBHOOKS_MANAGE = "webhooks:manage";
        public static final String ORGANIZATION_READ = "organization:read";
        public static final String ORGANIZATION_WRITE = "organization:write";
    }

    /**
     * Check if token has specific scope
     */
    public boolean hasScope(String scope) {
        return scopes.contains(scope) || scopes.contains("*");
    }

    /**
     * Check if token is valid (active and not expired)
     */
    public boolean isValid() {
        if (!active) return false;
        if (expiresAt != null && Instant.now().isAfter(expiresAt)) return false;
        return true;
    }

    /**
     * Check if IP is allowed
     */
    public boolean isIpAllowed(String ip) {
        if (allowedIps.isEmpty()) return true;
        return allowedIps.contains(ip) || allowedIps.contains("*");
    }

    /**
     * Generate new token key prefix for display
     */
    public static String generateTokenKey() {
        return "slms_" + UUID.randomUUID().toString().replace("-", "");
    }
}
