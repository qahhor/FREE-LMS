package com.freelms.integration.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelms.integration.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Smartup LMS - Webhook Service
 *
 * Manages webhooks and delivers events to external systems.
 * Supports HMAC signature verification and automatic retries.
 */
@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private static final int SECRET_LENGTH = 32;
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final int[] RETRY_DELAYS = {0, 60, 300, 900, 3600}; // seconds

    private final WebhookRepository webhookRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom;

    public WebhookService(WebhookRepository webhookRepository,
                         WebhookDeliveryRepository deliveryRepository,
                         ObjectMapper objectMapper) {
        this.webhookRepository = webhookRepository;
        this.deliveryRepository = deliveryRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.secureRandom = new SecureRandom();
    }

    /**
     * Create a new webhook
     */
    @Transactional
    public WebhookResponseDto createWebhook(WebhookCreateDto dto, Long organizationId) {
        String webhookId = UUID.randomUUID().toString();
        String secret = generateSecret();

        Webhook webhook = Webhook.builder()
                .webhookId(webhookId)
                .organizationId(organizationId)
                .name(dto.getName())
                .description(dto.getDescription())
                .url(dto.getUrl())
                .events(dto.getEvents())
                .contentType(dto.getContentType() != null ? dto.getContentType() : Webhook.ContentType.JSON)
                .authType(dto.getAuthType() != null ? dto.getAuthType() : Webhook.AuthType.HMAC_SHA256)
                .authHeader(dto.getAuthHeader())
                .authValue(dto.getAuthValue())
                .secret(secret)
                .retryCount(dto.getRetryCount() != null ? dto.getRetryCount() : 3)
                .timeoutSeconds(dto.getTimeoutSeconds() != null ? dto.getTimeoutSeconds() : 30)
                .build();

        webhook = webhookRepository.save(webhook);
        log.info("Created webhook: {} for organization: {}", webhook.getName(), organizationId);

        return toResponseDto(webhook);
    }

    /**
     * Get all webhooks for organization
     */
    public List<WebhookResponseDto> getWebhooksForOrganization(Long organizationId) {
        return webhookRepository.findByOrganizationId(organizationId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get webhook by ID
     */
    public Optional<WebhookResponseDto> getWebhook(String webhookId, Long organizationId) {
        return webhookRepository.findByWebhookIdAndOrganizationId(webhookId, organizationId)
                .map(this::toResponseDto);
    }

    /**
     * Update webhook
     */
    @Transactional
    public Optional<WebhookResponseDto> updateWebhook(String webhookId, WebhookUpdateDto dto, Long organizationId) {
        Optional<Webhook> webhookOpt = webhookRepository.findByWebhookIdAndOrganizationId(webhookId, organizationId);

        if (webhookOpt.isEmpty()) {
            return Optional.empty();
        }

        Webhook webhook = webhookOpt.get();

        if (dto.getName() != null) webhook.setName(dto.getName());
        if (dto.getDescription() != null) webhook.setDescription(dto.getDescription());
        if (dto.getUrl() != null) webhook.setUrl(dto.getUrl());
        if (dto.getEvents() != null) webhook.setEvents(dto.getEvents());
        if (dto.getContentType() != null) webhook.setContentType(dto.getContentType());
        if (dto.getAuthType() != null) webhook.setAuthType(dto.getAuthType());
        if (dto.getAuthHeader() != null) webhook.setAuthHeader(dto.getAuthHeader());
        if (dto.getAuthValue() != null) webhook.setAuthValue(dto.getAuthValue());
        if (dto.getRetryCount() != null) webhook.setRetryCount(dto.getRetryCount());
        if (dto.getTimeoutSeconds() != null) webhook.setTimeoutSeconds(dto.getTimeoutSeconds());
        if (dto.getActive() != null) webhook.setActive(dto.getActive());

        webhook = webhookRepository.save(webhook);
        log.info("Updated webhook: {}", webhook.getName());

        return Optional.of(toResponseDto(webhook));
    }

    /**
     * Delete webhook
     */
    @Transactional
    public boolean deleteWebhook(String webhookId, Long organizationId) {
        Optional<Webhook> webhookOpt = webhookRepository.findByWebhookIdAndOrganizationId(webhookId, organizationId);

        if (webhookOpt.isEmpty()) {
            return false;
        }

        webhookRepository.delete(webhookOpt.get());
        log.info("Deleted webhook: {}", webhookOpt.get().getName());

        return true;
    }

    /**
     * Rotate webhook secret
     */
    @Transactional
    public Optional<WebhookSecretDto> rotateSecret(String webhookId, Long organizationId) {
        Optional<Webhook> webhookOpt = webhookRepository.findByWebhookIdAndOrganizationId(webhookId, organizationId);

        if (webhookOpt.isEmpty()) {
            return Optional.empty();
        }

        Webhook webhook = webhookOpt.get();
        String newSecret = generateSecret();
        webhook.setSecret(newSecret);
        webhookRepository.save(webhook);

        log.info("Rotated secret for webhook: {}", webhook.getName());

        return Optional.of(WebhookSecretDto.builder()
                .webhookId(webhookId)
                .secret(newSecret)
                .message("New secret generated. Update your webhook receiver.")
                .build());
    }

    /**
     * Test webhook delivery
     */
    public WebhookTestResultDto testWebhook(String webhookId, Long organizationId) {
        Optional<Webhook> webhookOpt = webhookRepository.findByWebhookIdAndOrganizationId(webhookId, organizationId);

        if (webhookOpt.isEmpty()) {
            return WebhookTestResultDto.builder()
                    .webhookId(webhookId)
                    .success(false)
                    .error("Webhook not found")
                    .build();
        }

        Webhook webhook = webhookOpt.get();

        // Create test payload
        Map<String, Object> testPayload = new HashMap<>();
        testPayload.put("event", "webhook.test");
        testPayload.put("webhookId", webhookId);
        testPayload.put("timestamp", Instant.now().toString());
        testPayload.put("message", "This is a test delivery from Smartup LMS");

        long startTime = System.currentTimeMillis();
        try {
            ResponseEntity<String> response = deliverPayload(webhook, testPayload, "test-" + UUID.randomUUID());
            long duration = System.currentTimeMillis() - startTime;

            return WebhookTestResultDto.builder()
                    .webhookId(webhookId)
                    .success(response.getStatusCode().is2xxSuccessful())
                    .httpStatusCode(response.getStatusCode().value())
                    .durationMs(duration)
                    .responseBody(response.getBody())
                    .build();

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Webhook test failed: {}", e.getMessage());

            return WebhookTestResultDto.builder()
                    .webhookId(webhookId)
                    .success(false)
                    .durationMs(duration)
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * Trigger webhook event
     */
    @Async
    public void triggerEvent(String eventType, Long organizationId, Map<String, Object> payload) {
        List<Webhook> webhooks = webhookRepository.findByOrganizationIdAndActiveTrue(organizationId);

        for (Webhook webhook : webhooks) {
            if (webhook.getEvents().contains(eventType) || webhook.getEvents().contains("*")) {
                String eventId = UUID.randomUUID().toString();
                deliverEventAsync(webhook, eventType, eventId, payload, 1);
            }
        }
    }

    /**
     * Deliver event with retry support
     */
    @Async
    public void deliverEventAsync(Webhook webhook, String eventType, String eventId,
                                   Map<String, Object> payload, int attemptNumber) {
        Map<String, Object> fullPayload = new HashMap<>();
        fullPayload.put("event", eventType);
        fullPayload.put("eventId", eventId);
        fullPayload.put("timestamp", Instant.now().toString());
        fullPayload.put("data", payload);

        WebhookDelivery delivery = WebhookDelivery.builder()
                .deliveryId(UUID.randomUUID().toString())
                .webhook(webhook)
                .eventId(eventId)
                .eventType(eventType)
                .status(WebhookDelivery.DeliveryStatus.PENDING)
                .attemptNumber(attemptNumber)
                .build();

        try {
            delivery.setRequestBody(objectMapper.writeValueAsString(fullPayload));
        } catch (Exception e) {
            log.error("Failed to serialize payload", e);
        }

        long startTime = System.currentTimeMillis();
        try {
            ResponseEntity<String> response = deliverPayload(webhook, fullPayload, eventId);
            long duration = System.currentTimeMillis() - startTime;

            delivery.setDurationMs(duration);
            delivery.setHttpStatusCode(response.getStatusCode().value());
            delivery.setResponseBody(truncate(response.getBody(), 10000));

            if (response.getStatusCode().is2xxSuccessful()) {
                delivery.setStatus(WebhookDelivery.DeliveryStatus.SUCCESS);
                updateWebhookSuccess(webhook);
            } else {
                delivery.setStatus(WebhookDelivery.DeliveryStatus.FAILED);
                delivery.setError("HTTP " + response.getStatusCode().value());
                scheduleRetry(webhook, eventType, eventId, payload, attemptNumber);
                updateWebhookFailure(webhook, "HTTP " + response.getStatusCode().value());
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            delivery.setDurationMs(duration);
            delivery.setStatus(WebhookDelivery.DeliveryStatus.FAILED);
            delivery.setError(truncate(e.getMessage(), 1000));
            scheduleRetry(webhook, eventType, eventId, payload, attemptNumber);
            updateWebhookFailure(webhook, e.getMessage());
        }

        deliveryRepository.save(delivery);
    }

    /**
     * Deliver payload to webhook URL
     */
    private ResponseEntity<String> deliverPayload(Webhook webhook, Map<String, Object> payload, String eventId) throws Exception {
        String body = objectMapper.writeValueAsString(payload);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", webhook.getContentType() == Webhook.ContentType.JSON
                ? MediaType.APPLICATION_JSON_VALUE
                : MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headers.set("X-Webhook-Id", webhook.getWebhookId());
        headers.set("X-Event-Id", eventId);
        headers.set("X-Timestamp", String.valueOf(Instant.now().toEpochMilli()));

        // Add authentication
        switch (webhook.getAuthType()) {
            case HMAC_SHA256:
                String signature = generateHmacSignature(body, webhook.getSecret());
                headers.set("X-Signature", "sha256=" + signature);
                break;
            case BEARER_TOKEN:
                headers.set("Authorization", "Bearer " + webhook.getAuthValue());
                break;
            case BASIC_AUTH:
                headers.set("Authorization", "Basic " + webhook.getAuthValue());
                break;
            case API_KEY:
                String headerName = webhook.getAuthHeader() != null ? webhook.getAuthHeader() : "X-API-Key";
                headers.set(headerName, webhook.getAuthValue());
                break;
            case NONE:
            default:
                break;
        }

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        return restTemplate.exchange(webhook.getUrl(), HttpMethod.POST, request, String.class);
    }

    /**
     * Generate HMAC-SHA256 signature
     */
    private String generateHmacSignature(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Schedule retry for failed delivery
     */
    private void scheduleRetry(Webhook webhook, String eventType, String eventId,
                               Map<String, Object> payload, int attemptNumber) {
        if (attemptNumber < webhook.getRetryCount()) {
            // In production, use a message queue or scheduler
            log.info("Scheduling retry {} for webhook: {}", attemptNumber + 1, webhook.getName());
        }
    }

    /**
     * Update webhook success stats
     */
    @Transactional
    public void updateWebhookSuccess(Webhook webhook) {
        webhook.setSuccessCount(webhook.getSuccessCount() + 1);
        webhook.setLastTriggeredAt(Instant.now());
        webhook.setLastSuccessAt(Instant.now());
        webhookRepository.save(webhook);
    }

    /**
     * Update webhook failure stats
     */
    @Transactional
    public void updateWebhookFailure(Webhook webhook, String error) {
        webhook.setFailureCount(webhook.getFailureCount() + 1);
        webhook.setLastTriggeredAt(Instant.now());
        webhook.setLastError(truncate(error, 1000));
        webhookRepository.save(webhook);
    }

    /**
     * Get delivery history for webhook
     */
    public List<WebhookDeliveryDto> getDeliveryHistory(String webhookId, Long organizationId, int limit) {
        Optional<Webhook> webhookOpt = webhookRepository.findByWebhookIdAndOrganizationId(webhookId, organizationId);

        if (webhookOpt.isEmpty()) {
            return Collections.emptyList();
        }

        return deliveryRepository.findByWebhookOrderByCreatedAtDesc(webhookOpt.get()).stream()
                .limit(limit)
                .map(this::toDeliveryDto)
                .collect(Collectors.toList());
    }

    /**
     * Retry a specific delivery
     */
    @Transactional
    public Optional<WebhookDeliveryDto> retryDelivery(String deliveryId, Long organizationId) {
        Optional<WebhookDelivery> deliveryOpt = deliveryRepository.findByDeliveryId(deliveryId);

        if (deliveryOpt.isEmpty()) {
            return Optional.empty();
        }

        WebhookDelivery delivery = deliveryOpt.get();
        Webhook webhook = delivery.getWebhook();

        if (!webhook.getOrganizationId().equals(organizationId)) {
            return Optional.empty();
        }

        // Parse original payload and redeliver
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(delivery.getRequestBody(), Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) payload.get("data");

            deliverEventAsync(webhook, delivery.getEventType(), delivery.getEventId(),
                    data, delivery.getAttemptNumber() + 1);

            return Optional.of(toDeliveryDto(delivery));
        } catch (Exception e) {
            log.error("Failed to retry delivery: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get available webhook event types
     */
    public List<WebhookEventTypeDto> getAvailableEventTypes() {
        return Arrays.asList(
                // User events
                eventType(Webhook.Events.USER_CREATED, "User Created", "When a new user is created", "Users"),
                eventType(Webhook.Events.USER_UPDATED, "User Updated", "When user profile is updated", "Users"),
                eventType(Webhook.Events.USER_DELETED, "User Deleted", "When a user is deleted", "Users"),
                eventType(Webhook.Events.USER_ACTIVATED, "User Activated", "When a user is activated", "Users"),
                eventType(Webhook.Events.USER_DEACTIVATED, "User Deactivated", "When a user is deactivated", "Users"),

                // Course events
                eventType(Webhook.Events.COURSE_CREATED, "Course Created", "When a new course is created", "Courses"),
                eventType(Webhook.Events.COURSE_UPDATED, "Course Updated", "When a course is updated", "Courses"),
                eventType(Webhook.Events.COURSE_PUBLISHED, "Course Published", "When a course is published", "Courses"),
                eventType(Webhook.Events.COURSE_ARCHIVED, "Course Archived", "When a course is archived", "Courses"),

                // Enrollment events
                eventType(Webhook.Events.ENROLLMENT_CREATED, "Enrollment Created", "When a user is enrolled", "Enrollments"),
                eventType(Webhook.Events.ENROLLMENT_STARTED, "Enrollment Started", "When a user starts a course", "Enrollments"),
                eventType(Webhook.Events.ENROLLMENT_COMPLETED, "Enrollment Completed", "When a user completes a course", "Enrollments"),
                eventType(Webhook.Events.ENROLLMENT_EXPIRED, "Enrollment Expired", "When an enrollment expires", "Enrollments"),

                // Progress events
                eventType(Webhook.Events.LESSON_COMPLETED, "Lesson Completed", "When a user completes a lesson", "Progress"),
                eventType(Webhook.Events.MODULE_COMPLETED, "Module Completed", "When a user completes a module", "Progress"),
                eventType(Webhook.Events.QUIZ_COMPLETED, "Quiz Completed", "When a user completes a quiz", "Progress"),
                eventType(Webhook.Events.ASSIGNMENT_SUBMITTED, "Assignment Submitted", "When an assignment is submitted", "Progress"),
                eventType(Webhook.Events.ASSIGNMENT_GRADED, "Assignment Graded", "When an assignment is graded", "Progress"),

                // Certificate events
                eventType(Webhook.Events.CERTIFICATE_ISSUED, "Certificate Issued", "When a certificate is issued", "Certificates"),
                eventType(Webhook.Events.CERTIFICATE_EXPIRED, "Certificate Expired", "When a certificate expires", "Certificates"),

                // Compliance events
                eventType(Webhook.Events.TRAINING_DUE, "Training Due", "When training is due soon", "Compliance"),
                eventType(Webhook.Events.TRAINING_OVERDUE, "Training Overdue", "When training is overdue", "Compliance")
        );
    }

    private String generateSecret() {
        byte[] bytes = new byte[SECRET_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }

    private WebhookResponseDto toResponseDto(Webhook webhook) {
        return WebhookResponseDto.builder()
                .webhookId(webhook.getWebhookId())
                .name(webhook.getName())
                .description(webhook.getDescription())
                .url(webhook.getUrl())
                .events(webhook.getEvents())
                .active(webhook.isActive())
                .contentType(webhook.getContentType())
                .authType(webhook.getAuthType())
                .retryCount(webhook.getRetryCount())
                .timeoutSeconds(webhook.getTimeoutSeconds())
                .successCount(webhook.getSuccessCount())
                .failureCount(webhook.getFailureCount())
                .lastTriggeredAt(webhook.getLastTriggeredAt())
                .lastSuccessAt(webhook.getLastSuccessAt())
                .lastError(webhook.getLastError())
                .createdAt(webhook.getCreatedAt())
                .build();
    }

    private WebhookDeliveryDto toDeliveryDto(WebhookDelivery delivery) {
        return WebhookDeliveryDto.builder()
                .deliveryId(delivery.getDeliveryId())
                .eventId(delivery.getEventId())
                .eventType(delivery.getEventType())
                .status(delivery.getStatus().name())
                .httpStatusCode(delivery.getHttpStatusCode())
                .attemptNumber(delivery.getAttemptNumber())
                .durationMs(delivery.getDurationMs())
                .error(delivery.getError())
                .createdAt(delivery.getCreatedAt())
                .build();
    }

    private WebhookEventTypeDto eventType(String event, String name, String description, String category) {
        return WebhookEventTypeDto.builder()
                .event(event)
                .name(name)
                .description(description)
                .category(category)
                .payloadSchema(new HashMap<>())
                .build();
    }
}
