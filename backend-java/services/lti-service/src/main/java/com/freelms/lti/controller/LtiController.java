package com.freelms.lti.controller;

import com.freelms.lti.entity.LtiLaunch;
import com.freelms.lti.entity.LtiPlatform;
import com.freelms.lti.entity.LtiTool;
import com.freelms.lti.service.LtiOidcService;
import com.freelms.lti.service.LtiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lti")
@Tag(name = "LTI 1.3", description = "LTI 1.3 integration endpoints")
public class LtiController {

    private final LtiService ltiService;
    private final LtiOidcService oidcService;

    public LtiController(LtiService ltiService, LtiOidcService oidcService) {
        this.ltiService = ltiService;
        this.oidcService = oidcService;
    }

    // ==================== OIDC Endpoints ====================

    @PostMapping("/login")
    @Operation(summary = "OIDC login initiation endpoint")
    public ResponseEntity<Map<String, String>> oidcLogin(
            @RequestParam String iss,
            @RequestParam String login_hint,
            @RequestParam String target_link_uri,
            @RequestParam(required = false) String lti_message_hint,
            @RequestParam(required = false) String client_id,
            @RequestParam(required = false) String lti_deployment_id,
            HttpServletRequest request) {

        String redirectUrl = oidcService.handleOidcInitiation(
                iss, login_hint, target_link_uri, lti_message_hint, client_id, lti_deployment_id,
                request.getRemoteAddr(), request.getHeader("User-Agent"));

        return ResponseEntity.ok(Map.of("redirect_url", redirectUrl));
    }

    @GetMapping("/login")
    @Operation(summary = "OIDC login initiation endpoint (GET)")
    public ResponseEntity<Map<String, String>> oidcLoginGet(
            @RequestParam String iss,
            @RequestParam String login_hint,
            @RequestParam String target_link_uri,
            @RequestParam(required = false) String lti_message_hint,
            @RequestParam(required = false) String client_id,
            @RequestParam(required = false) String lti_deployment_id,
            HttpServletRequest request) {

        return oidcLogin(iss, login_hint, target_link_uri, lti_message_hint, client_id, lti_deployment_id, request);
    }

    @PostMapping("/callback")
    @Operation(summary = "OIDC callback endpoint")
    public ResponseEntity<Map<String, Object>> oidcCallback(
            @RequestParam String id_token,
            @RequestParam String state) {

        // In real implementation: validate JWT, extract claims
        LtiLaunch launch = ltiService.getLaunchByState(state);

        // Update launch status
        ltiService.updateLaunchStatus(state, LtiLaunch.LaunchStatus.TOKEN_VALIDATED);

        // Record successful launch
        if (launch.getPlatformId() != null) {
            ltiService.recordSuccessfulLaunch(launch.getPlatformId());
        }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "launchId", launch.getId(),
                "redirect", "/course/" + launch.getCourseId()
        ));
    }

    @GetMapping("/.well-known/jwks.json")
    @Operation(summary = "JWKS endpoint")
    public ResponseEntity<Map<String, Object>> getJwks() {
        // In real implementation: return actual JWKS
        return ResponseEntity.ok(Map.of(
                "keys", new Object[]{}
        ));
    }

    @GetMapping("/.well-known/openid-configuration")
    @Operation(summary = "OpenID Connect discovery endpoint")
    public ResponseEntity<Map<String, Object>> getOpenIdConfiguration() {
        return ResponseEntity.ok(oidcService.getPlatformConfiguration());
    }

    // ==================== Platform Management ====================

    @PostMapping("/platforms")
    @Operation(summary = "Register a new platform")
    public ResponseEntity<LtiPlatform> registerPlatform(@RequestBody LtiPlatform platform) {
        return ResponseEntity.ok(ltiService.registerPlatform(platform));
    }

    @GetMapping("/platforms/{platformId}")
    @Operation(summary = "Get platform by ID")
    public ResponseEntity<LtiPlatform> getPlatform(@PathVariable UUID platformId) {
        return ResponseEntity.ok(ltiService.getPlatform(platformId));
    }

    @PutMapping("/platforms/{platformId}")
    @Operation(summary = "Update platform")
    public ResponseEntity<LtiPlatform> updatePlatform(
            @PathVariable UUID platformId,
            @RequestBody LtiPlatform updates) {
        return ResponseEntity.ok(ltiService.updatePlatform(platformId, updates));
    }

    @GetMapping("/platforms")
    @Operation(summary = "List platforms")
    public ResponseEntity<List<LtiPlatform>> listPlatforms(
            @RequestParam(required = false) Long organizationId) {
        return ResponseEntity.ok(ltiService.getPlatforms(organizationId));
    }

    @PatchMapping("/platforms/{platformId}/status")
    @Operation(summary = "Update platform status")
    public ResponseEntity<Void> updatePlatformStatus(
            @PathVariable UUID platformId,
            @RequestParam LtiPlatform.PlatformStatus status) {
        ltiService.updatePlatformStatus(platformId, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/platforms/{platformId}/stats")
    @Operation(summary = "Get platform statistics")
    public ResponseEntity<Map<String, Object>> getPlatformStats(
            @PathVariable UUID platformId,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ltiService.getPlatformStats(platformId, days));
    }

    // ==================== Tool Management ====================

    @PostMapping("/tools")
    @Operation(summary = "Register a new tool")
    public ResponseEntity<LtiTool> registerTool(@RequestBody LtiTool tool) {
        return ResponseEntity.ok(ltiService.registerTool(tool));
    }

    @GetMapping("/tools/{toolId}")
    @Operation(summary = "Get tool by ID")
    public ResponseEntity<LtiTool> getTool(@PathVariable UUID toolId) {
        return ResponseEntity.ok(ltiService.getTool(toolId));
    }

    @PutMapping("/tools/{toolId}")
    @Operation(summary = "Update tool")
    public ResponseEntity<LtiTool> updateTool(
            @PathVariable UUID toolId,
            @RequestBody LtiTool updates) {
        return ResponseEntity.ok(ltiService.updateTool(toolId, updates));
    }

    @GetMapping("/tools")
    @Operation(summary = "List tools")
    public ResponseEntity<List<LtiTool>> listTools(
            @RequestParam(required = false) Long organizationId) {
        return ResponseEntity.ok(ltiService.getTools(organizationId));
    }

    @GetMapping("/tools/active")
    @Operation(summary = "List active tools for organization")
    public ResponseEntity<List<LtiTool>> listActiveTools(
            @RequestParam Long organizationId) {
        return ResponseEntity.ok(ltiService.getActiveTools(organizationId));
    }

    @PatchMapping("/tools/{toolId}/status")
    @Operation(summary = "Update tool status")
    public ResponseEntity<Void> updateToolStatus(
            @PathVariable UUID toolId,
            @RequestParam LtiTool.ToolStatus status) {
        ltiService.updateToolStatus(toolId, status);
        return ResponseEntity.ok().build();
    }

    // ==================== Launch Endpoints ====================

    @PostMapping("/launch/tool/{toolId}")
    @Operation(summary = "Generate tool launch URL")
    public ResponseEntity<Map<String, String>> launchTool(
            @PathVariable UUID toolId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String resourceLinkId,
            @RequestHeader("X-User-Id") Long userId) {

        String launchUrl = oidcService.generateToolLaunchUrl(
                toolId, userId, courseId, resourceLinkId, null);

        ltiService.recordToolLaunch(toolId);

        return ResponseEntity.ok(Map.of("launch_url", launchUrl));
    }

    @GetMapping("/launches/{launchId}")
    @Operation(summary = "Get launch session details")
    public ResponseEntity<LtiLaunch> getLaunch(@PathVariable UUID launchId) {
        // Would need to add this method to service
        return ResponseEntity.ok().build();
    }

    // ==================== Maintenance ====================

    @PostMapping("/cleanup")
    @Operation(summary = "Cleanup expired launch sessions")
    public ResponseEntity<Map<String, Object>> cleanup() {
        int cleaned = ltiService.cleanupExpiredLaunches();
        return ResponseEntity.ok(Map.of("cleanedCount", cleaned));
    }
}
