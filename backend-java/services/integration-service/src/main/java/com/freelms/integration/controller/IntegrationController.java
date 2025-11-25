package com.freelms.integration.controller;

import com.freelms.common.dto.ApiResponse;
import com.freelms.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/integrations")
@RequiredArgsConstructor
public class IntegrationController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getIntegrations(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "Integrations list endpoint"));
    }

    @PostMapping("/{integrationId}/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> triggerSync(@PathVariable Long integrationId) {
        return ResponseEntity.ok(ApiResponse.success(null, "Trigger sync endpoint"));
    }

    @GetMapping("/hr/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> syncHRUsers(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "HR users sync endpoint"));
    }

    @PostMapping("/calendar/event")
    public ResponseEntity<ApiResponse<?>> createCalendarEvent(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "Create calendar event endpoint"));
    }

    @GetMapping("/sso/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getSSOConfig(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "SSO config endpoint"));
    }

    @PostMapping("/webhook/{integrationId}")
    public ResponseEntity<ApiResponse<?>> handleWebhook(
            @PathVariable Long integrationId,
            @RequestBody String payload) {
        return ResponseEntity.ok(ApiResponse.success(null, "Webhook endpoint"));
    }
}
