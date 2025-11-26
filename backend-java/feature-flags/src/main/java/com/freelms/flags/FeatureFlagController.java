package com.freelms.flags;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * FREE LMS - Feature Flag REST Controller
 *
 * API for evaluating and managing feature flags.
 */
@RestController
@RequestMapping("/api/feature-flags")
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    public FeatureFlagController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    /**
     * Evaluate a single feature flag
     */
    @GetMapping("/{flagKey}")
    public ResponseEntity<Map<String, Object>> evaluateFlag(
            @PathVariable String flagKey,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Map<String, String> attributes) {

        EvaluationContext context = buildContext(user, attributes);
        Object value = featureFlagService.evaluate(flagKey, context);

        Map<String, Object> response = new HashMap<>();
        response.put("key", flagKey);
        response.put("value", value);
        response.put("evaluatedAt", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * Evaluate multiple feature flags at once
     */
    @PostMapping("/evaluate")
    public ResponseEntity<Map<String, Object>> evaluateFlags(
            @RequestBody EvaluateFlagsRequest request,
            @AuthenticationPrincipal UserPrincipal user) {

        EvaluationContext.Builder contextBuilder = EvaluationContext.builder();

        if (user != null) {
            contextBuilder
                    .userId(user.getId())
                    .organizationId(user.getOrganizationId())
                    .role(user.getRole())
                    .email(user.getEmail());
        }

        if (request.getContext() != null) {
            contextBuilder.customAttributes(request.getContext());
        }

        EvaluationContext context = contextBuilder.build();

        Map<String, Object> results = new HashMap<>();
        for (String flagKey : request.getFlags()) {
            results.put(flagKey, featureFlagService.evaluate(flagKey, context));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("flags", results);
        response.put("evaluatedAt", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * Check if a flag is enabled (boolean shortcut)
     */
    @GetMapping("/{flagKey}/enabled")
    public ResponseEntity<Map<String, Object>> isFlagEnabled(
            @PathVariable String flagKey,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Map<String, String> attributes) {

        EvaluationContext context = buildContext(user, attributes);
        boolean enabled = featureFlagService.isEnabled(flagKey, context);

        Map<String, Object> response = new HashMap<>();
        response.put("key", flagKey);
        response.put("enabled", enabled);

        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // Admin Endpoints
    // =========================================================================

    /**
     * Create a new feature flag
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeatureFlag> createFlag(
            @RequestBody FeatureFlag flag,
            @AuthenticationPrincipal UserPrincipal user) {

        flag.setCreatedBy(user.getId());
        FeatureFlag saved = featureFlagService.saveFlag(flag);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Update a feature flag
     */
    @PutMapping("/{flagKey}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeatureFlag> updateFlag(
            @PathVariable String flagKey,
            @RequestBody FeatureFlag flag) {

        flag.setKey(flagKey);
        FeatureFlag saved = featureFlagService.saveFlag(flag);
        return ResponseEntity.ok(saved);
    }

    /**
     * Toggle a feature flag on/off
     */
    @PostMapping("/{flagKey}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleFlag(
            @PathVariable String flagKey,
            @RequestBody ToggleRequest request) {

        // This is a simplified toggle - in production, you'd fetch and update
        Map<String, Object> response = new HashMap<>();
        response.put("key", flagKey);
        response.put("enabled", request.isEnabled());
        response.put("message", "Flag " + (request.isEnabled() ? "enabled" : "disabled"));

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a feature flag
     */
    @DeleteMapping("/{flagKey}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFlag(@PathVariable String flagKey) {
        featureFlagService.deleteFlag(flagKey);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private EvaluationContext buildContext(UserPrincipal user, Map<String, String> attributes) {
        EvaluationContext.Builder builder = EvaluationContext.builder();

        if (user != null) {
            builder.userId(user.getId())
                   .organizationId(user.getOrganizationId())
                   .role(user.getRole())
                   .email(user.getEmail());
        } else {
            builder.userId("anonymous-" + System.currentTimeMillis())
                   .role("ANONYMOUS");
        }

        if (attributes != null) {
            attributes.forEach(builder::customAttribute);
        }

        return builder.build();
    }

    // =========================================================================
    // Request/Response DTOs
    // =========================================================================

    static class EvaluateFlagsRequest {
        private List<String> flags;
        private Map<String, Object> context;

        public List<String> getFlags() { return flags; }
        public void setFlags(List<String> flags) { this.flags = flags; }
        public Map<String, Object> getContext() { return context; }
        public void setContext(Map<String, Object> context) { this.context = context; }
    }

    static class ToggleRequest {
        private boolean enabled;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    // Placeholder for user principal
    interface UserPrincipal {
        String getId();
        String getOrganizationId();
        String getRole();
        String getEmail();
    }
}
