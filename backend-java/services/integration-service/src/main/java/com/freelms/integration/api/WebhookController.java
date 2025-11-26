package com.freelms.integration.api;

import com.freelms.common.dto.ApiResponse;
import com.freelms.common.dto.PageResponse;
import com.freelms.integration.dto.*;
import com.freelms.integration.service.WebhookService;
import com.freelms.integration.token.ApiToken;
import com.freelms.integration.webhook.Webhook;
import com.freelms.integration.webhook.WebhookDelivery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Smartup LMS - Webhook Management Controller
 *
 * Manage webhook subscriptions for real-time event notifications.
 */
@RestController
@RequestMapping("/api/external/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Webhook subscription management")
@SecurityRequirement(name = "ApiToken")
public class WebhookController {

    private final WebhookService webhookService;

    @GetMapping
    @Operation(summary = "List webhooks", description = "Get all webhook subscriptions")
    public ResponseEntity<ApiResponse<List<WebhookResponseDto>>> listWebhooks(
            @RequestAttribute("apiToken") ApiToken token) {

        webhookService.validateScope(token, ApiToken.Scopes.WEBHOOKS_MANAGE);
        List<WebhookResponseDto> webhooks = webhookService.listWebhooks(token.getOrganizationId());
        return ResponseEntity.ok(ApiResponse.success(webhooks));
    }

    @GetMapping("/{webhookId}")
    @Operation(summary = "Get webhook", description = "Get webhook details by ID")
    public ResponseEntity<ApiResponse<WebhookResponseDto>> getWebhook(
            @RequestAttribute("apiToken") ApiToken token,
            @PathVariable String webhookId) {

        webhookService.validateScope(token, ApiToken.Scopes.WEBHOOKS_MANAGE);
        WebhookResponseDto webhook = webhookService.getWebhook(token.getOrganizationId(), webhookId);
        return ResponseEntity.ok(ApiResponse.success(webhook));
    }

    @PostMapping
    @Operation(summary = "Create webhook", description = "Subscribe to webhook events")
    public ResponseEntity<ApiResponse<WebhookResponseDto>> createWebhook(
            @RequestAttribute("apiToken") ApiToken token,
            @Valid @RequestBody WebhookCreateDto request) {

        webhookService.validateScope(token, ApiToken.Scopes.WEBHOOKS_MANAGE);
        WebhookResponseDto webhook = webhookService.createWebhook(
                token.getOrganizationId(), token.getCreatedBy(), request);
        return ResponseEntity.ok(ApiResponse.success(webhook, "Webhook created successfully"));
    }

    @PutMapping("/{webhookId}")
    @Operation(summary = "Update webhook", description = "Update webhook configuration")
    public ResponseEntity<ApiResponse<WebhookResponseDto>> updateWebhook(
            @RequestAttribute("apiToken") ApiToken token,
            @PathVariable String webhookId,
            @Valid @RequestBody WebhookUpdateDto request) {

        webhookService.validateScope(token, ApiToken.Scopes.WEBHOOKS_MANAGE);
        WebhookResponseDto webhook = webhookService.updateWebhook(
                token.getOrganizationId(), webhookId, request);
        return ResponseEntity.ok(ApiResponse.success(webhook, "Webhook updated successfully"));
    }

    @DeleteMapping("/{webhookId}")
    @Operation(summary = "Delete webhook", description = "Unsubscribe from webhook events")
    public ResponseEntity<ApiResponse<Void>> deleteWebhook(
            @RequestAttribute("apiToken") ApiToken token,
            @PathVariable String webhookId) {

        webhookService.validateScope(token, ApiToken.Scopes.WEBHOOKS_MANAGE);
        webhookService.deleteWebhook(token.getOrganizationId(), webhookId);
        return ResponseEntity.ok(ApiResponse.success(null, "Webhook deleted successfully"));
    }

    @PostMapping("/{webhookId}/test")
    @Operation(summary = "Test webhook", description = "Send a test event to webhook URL")
    public ResponseEntity<ApiResponse<WebhookTestResultDto>> testWebhook(
            @RequestAttribute("apiToken") ApiToken token,
            @PathVariable String webhookId) {

        webhookService.validateScope(token, ApiToken.Scopes.WEBHOOKS_MANAGE);
        WebhookTestResultDto result = webhookService.testWebhook(token.getOrganizationId(), webhookId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{webhookId}/rotate-secret")
    @Operation(summary = "Rotate secret", description = "Generate new webhook signing secret")
    public ResponseEntity<ApiResponse<WebhookSecretDto>> rotateSecret(
            @RequestAttribute("apiToken") ApiToken token,
            @PathVariable String webhookId) {

        webhookService.validateScope(token, ApiToken.Scopes.WEBHOOKS_MANAGE);
        WebhookSecretDto result = webhookService.rotateSecret(token.getOrganizationId(), webhookId);
        return ResponseEntity.ok(ApiResponse.success(result, "Secret rotated successfully"));
    }

    @GetMapping("/{webhookId}/deliveries")
    @Operation(summary = "List deliveries", description = "Get webhook delivery history")
    public ResponseEntity<ApiResponse<PageResponse<WebhookDeliveryDto>>> listDeliveries(
            @RequestAttribute("apiToken") ApiToken token,
            @PathVariable String webhookId,
            @RequestParam(required = false) String status,
            Pageable pageable) {

        webhookService.validateScope(token, ApiToken.Scopes.WEBHOOKS_MANAGE);
        PageResponse<WebhookDeliveryDto> deliveries = webhookService.listDeliveries(
                token.getOrganizationId(), webhookId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(deliveries));
    }

    @PostMapping("/{webhookId}/deliveries/{deliveryId}/retry")
    @Operation(summary = "Retry delivery", description = "Retry a failed webhook delivery")
    public ResponseEntity<ApiResponse<WebhookDeliveryDto>> retryDelivery(
            @RequestAttribute("apiToken") ApiToken token,
            @PathVariable String webhookId,
            @PathVariable String deliveryId) {

        webhookService.validateScope(token, ApiToken.Scopes.WEBHOOKS_MANAGE);
        WebhookDeliveryDto delivery = webhookService.retryDelivery(
                token.getOrganizationId(), webhookId, deliveryId);
        return ResponseEntity.ok(ApiResponse.success(delivery));
    }

    @GetMapping("/events")
    @Operation(summary = "List available events", description = "Get all available webhook event types")
    public ResponseEntity<ApiResponse<List<WebhookEventTypeDto>>> listEventTypes() {
        List<WebhookEventTypeDto> events = webhookService.listEventTypes();
        return ResponseEntity.ok(ApiResponse.success(events));
    }
}
