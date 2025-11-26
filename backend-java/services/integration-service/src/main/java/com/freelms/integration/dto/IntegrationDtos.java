package com.freelms.integration.dto;

import com.freelms.integration.token.ApiToken;
import com.freelms.integration.webhook.Webhook;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Smartup LMS - Integration API DTOs
 */

// ============================================================================
// API Token DTOs
// ============================================================================

@Data
@Builder
public class ApiTokenCreateDto {
    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    private ApiToken.TokenType type;

    @NotEmpty
    private Set<String> scopes;

    private Instant expiresAt;

    private Integer rateLimit;

    private Set<String> allowedIps;
}

@Data
@Builder
public class ApiTokenUpdateDto {
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private Set<String> scopes;

    private Instant expiresAt;

    private Integer rateLimit;

    private Set<String> allowedIps;

    private Boolean active;
}

@Data
@Builder
public class ApiTokenResponseDto {
    private Long id;
    private String tokenKeyPrefix;  // First 8 chars only
    private String name;
    private String description;
    private ApiToken.TokenType type;
    private Set<String> scopes;
    private boolean active;
    private Instant expiresAt;
    private Instant lastUsedAt;
    private String lastUsedIp;
    private Long requestCount;
    private Integer rateLimit;
    private Set<String> allowedIps;
    private Instant createdAt;
}

@Data
@Builder
public class ApiTokenCreatedDto {
    private Long id;
    private String token;  // Full token - shown only once!
    private String name;
    private ApiToken.TokenType type;
    private Set<String> scopes;
    private Instant expiresAt;
    private String message;
}

@Data
@Builder
public class ApiTokenUsageDto {
    private Long tokenId;
    private Long totalRequests;
    private Long successfulRequests;
    private Long failedRequests;
    private Map<String, Long> requestsByEndpoint;
    private Map<String, Long> requestsByDay;
    private Instant lastUsedAt;
}

@Data
@Builder
public class ApiScopeDto {
    private String scope;
    private String name;
    private String description;
    private String category;
}

// ============================================================================
// Webhook DTOs
// ============================================================================

@Data
@Builder
public class WebhookCreateDto {
    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotBlank
    @Pattern(regexp = "^https://.*", message = "URL must use HTTPS")
    private String url;

    @NotEmpty
    private Set<String> events;

    private Webhook.ContentType contentType;

    private Webhook.AuthType authType;

    private String authHeader;

    private String authValue;

    private Integer retryCount;

    private Integer timeoutSeconds;
}

@Data
@Builder
public class WebhookUpdateDto {
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @Pattern(regexp = "^https://.*", message = "URL must use HTTPS")
    private String url;

    private Set<String> events;

    private Webhook.ContentType contentType;

    private Webhook.AuthType authType;

    private String authHeader;

    private String authValue;

    private Integer retryCount;

    private Integer timeoutSeconds;

    private Boolean active;
}

@Data
@Builder
public class WebhookResponseDto {
    private String webhookId;
    private String name;
    private String description;
    private String url;
    private Set<String> events;
    private boolean active;
    private Webhook.ContentType contentType;
    private Webhook.AuthType authType;
    private Integer retryCount;
    private Integer timeoutSeconds;
    private Long successCount;
    private Long failureCount;
    private Instant lastTriggeredAt;
    private Instant lastSuccessAt;
    private String lastError;
    private Instant createdAt;
}

@Data
@Builder
public class WebhookSecretDto {
    private String webhookId;
    private String secret;
    private String message;
}

@Data
@Builder
public class WebhookTestResultDto {
    private String webhookId;
    private boolean success;
    private Integer httpStatusCode;
    private Long durationMs;
    private String responseBody;
    private String error;
}

@Data
@Builder
public class WebhookDeliveryDto {
    private String deliveryId;
    private String eventId;
    private String eventType;
    private String status;
    private Integer httpStatusCode;
    private Integer attemptNumber;
    private Long durationMs;
    private String error;
    private Instant createdAt;
}

@Data
@Builder
public class WebhookEventTypeDto {
    private String event;
    private String name;
    private String description;
    private String category;
    private Map<String, String> payloadSchema;
}

// ============================================================================
// User Export/Import DTOs
// ============================================================================

@Data
@Builder
public class UserImportDto {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String externalId;  // ID in external system (HR, ERP)

    private String employeeId;

    private String department;

    private String position;

    private String managerId;

    private String phone;

    private String timezone;

    private String language;

    private Map<String, Object> customFields;

    private List<String> groups;

    private String role;
}

@Data
@Builder
public class UserExportDto {
    private String id;
    private String externalId;
    private String email;
    private String firstName;
    private String lastName;
    private String employeeId;
    private String department;
    private String position;
    private String managerId;
    private String status;
    private String role;
    private String phone;
    private String timezone;
    private String language;
    private Map<String, Object> customFields;
    private List<String> groups;
    private Instant createdAt;
    private Instant lastLoginAt;
}

@Data
@Builder
public class BulkImportResultDto {
    private int total;
    private int created;
    private int updated;
    private int failed;
    private List<BulkImportErrorDto> errors;
}

@Data
@Builder
public class BulkImportErrorDto {
    private int row;
    private String identifier;
    private String error;
}

// ============================================================================
// Course Export DTOs
// ============================================================================

@Data
@Builder
public class CourseExportDto {
    private String id;
    private String externalId;
    private String title;
    private String description;
    private String category;
    private String level;
    private String language;
    private Integer durationMinutes;
    private String status;
    private String thumbnailUrl;
    private Integer moduleCount;
    private Integer lessonCount;
    private Double averageRating;
    private Integer enrollmentCount;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;
}

// ============================================================================
// Enrollment DTOs
// ============================================================================

@Data
@Builder
public class EnrollmentCreateDto {
    @NotBlank
    private String userId;

    @NotBlank
    private String courseId;

    private Instant dueDate;

    private boolean mandatory;

    private String assignedBy;

    private Map<String, Object> metadata;
}

@Data
@Builder
public class BulkEnrollmentDto {
    @NotEmpty
    private List<String> userIds;

    @NotBlank
    private String courseId;

    private Instant dueDate;

    private boolean mandatory;
}

@Data
@Builder
public class EnrollmentExportDto {
    private String id;
    private String userId;
    private String userEmail;
    private String userName;
    private String courseId;
    private String courseTitle;
    private String status;
    private Integer progressPercent;
    private Instant enrolledAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant dueDate;
    private boolean mandatory;
    private Integer timeSpentMinutes;
    private Double score;
}

// ============================================================================
// Progress DTOs
// ============================================================================

@Data
@Builder
public class ProgressExportDto {
    private String enrollmentId;
    private String userId;
    private String courseId;
    private String lessonId;
    private String lessonTitle;
    private String status;
    private Integer progressPercent;
    private Integer timeSpentSeconds;
    private Double score;
    private Instant startedAt;
    private Instant completedAt;
}

@Data
@Builder
public class UserProgressSummaryDto {
    private String userId;
    private Integer totalEnrollments;
    private Integer completedCourses;
    private Integer inProgressCourses;
    private Integer totalTimeSpentMinutes;
    private Double averageScore;
    private Integer certificatesEarned;
    private Integer achievementsUnlocked;
    private Instant lastActivityAt;
}

// ============================================================================
// Report DTOs
// ============================================================================

@Data
@Builder
public class CompletionReportDto {
    private Integer totalEnrollments;
    private Integer completedEnrollments;
    private Double completionRate;
    private Double averageCompletionTimeHours;
    private Map<String, CompletionStatDto> byDepartment;
    private Map<String, CompletionStatDto> byCourse;
    private List<CompletionTrendDto> trend;
}

@Data
@Builder
public class CompletionStatDto {
    private String name;
    private Integer total;
    private Integer completed;
    private Double rate;
}

@Data
@Builder
public class CompletionTrendDto {
    private String date;
    private Integer enrollments;
    private Integer completions;
}

@Data
@Builder
public class ComplianceReportDto {
    private Integer totalUsers;
    private Integer compliantUsers;
    private Integer nonCompliantUsers;
    private Double complianceRate;
    private List<ComplianceItemDto> overdueTrainings;
    private List<ComplianceItemDto> upcomingDue;
}

@Data
@Builder
public class ComplianceItemDto {
    private String userId;
    private String userName;
    private String department;
    private String courseId;
    private String courseTitle;
    private Instant dueDate;
    private Integer daysOverdue;
}

@Data
@Builder
public class ActivityReportDto {
    private String date;
    private Integer activeUsers;
    private Integer logins;
    private Integer lessonsCompleted;
    private Integer timeSpentMinutes;
}

// ============================================================================
// Certificate DTOs
// ============================================================================

@Data
@Builder
public class CertificateExportDto {
    private String id;
    private String certificateNumber;
    private String userId;
    private String userName;
    private String courseId;
    private String courseTitle;
    private Instant issuedAt;
    private Instant expiresAt;
    private boolean valid;
    private String downloadUrl;
    private String verificationUrl;
}

@Data
@Builder
public class CertificateVerificationDto {
    private String certificateNumber;
    private boolean valid;
    private String holderName;
    private String courseTitle;
    private Instant issuedAt;
    private Instant expiresAt;
    private String issuerOrganization;
    private String verifiedAt;
}
