package com.freelms.integration.api;

import com.freelms.common.dto.ApiResponse;
import com.freelms.common.security.UserPrincipal;
import com.freelms.integration.dto.*;
import com.freelms.integration.service.ApiTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Smartup LMS - API Token Management Controller
 *
 * Manage API tokens for external system integration.
 * Only accessible by organization admins via JWT authentication.
 */
@RestController
@RequestMapping("/api/v1/api-tokens")
@RequiredArgsConstructor
@Tag(name = "API Tokens", description = "API token management for integrations")
@SecurityRequirement(name = "JWT")
@PreAuthorize("hasRole('ADMIN')")
public class ApiTokenController {

    private final ApiTokenService tokenService;

    @GetMapping
    @Operation(summary = "List API tokens", description = "Get all API tokens for organization")
    public ResponseEntity<ApiResponse<List<ApiTokenResponseDto>>> listTokens(
            @AuthenticationPrincipal UserPrincipal principal) {

        List<ApiTokenResponseDto> tokens = tokenService.listTokens(principal.getOrganizationId());
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }

    @GetMapping("/{tokenId}")
    @Operation(summary = "Get API token", description = "Get API token details")
    public ResponseEntity<ApiResponse<ApiTokenResponseDto>> getToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long tokenId) {

        ApiTokenResponseDto token = tokenService.getToken(principal.getOrganizationId(), tokenId);
        return ResponseEntity.ok(ApiResponse.success(token));
    }

    @PostMapping
    @Operation(summary = "Create API token", description = "Create new API token. Token value shown only once!")
    public ResponseEntity<ApiResponse<ApiTokenCreatedDto>> createToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ApiTokenCreateDto request) {

        ApiTokenCreatedDto token = tokenService.createToken(
                principal.getOrganizationId(),
                principal.getId(),
                request);
        return ResponseEntity.ok(ApiResponse.success(token,
                "Token created successfully. Save the token value - it won't be shown again!"));
    }

    @PutMapping("/{tokenId}")
    @Operation(summary = "Update API token", description = "Update token settings (not the token value)")
    public ResponseEntity<ApiResponse<ApiTokenResponseDto>> updateToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long tokenId,
            @Valid @RequestBody ApiTokenUpdateDto request) {

        ApiTokenResponseDto token = tokenService.updateToken(
                principal.getOrganizationId(), tokenId, request);
        return ResponseEntity.ok(ApiResponse.success(token, "Token updated successfully"));
    }

    @DeleteMapping("/{tokenId}")
    @Operation(summary = "Revoke API token", description = "Permanently revoke an API token")
    public ResponseEntity<ApiResponse<Void>> revokeToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long tokenId) {

        tokenService.revokeToken(principal.getOrganizationId(), tokenId);
        return ResponseEntity.ok(ApiResponse.success(null, "Token revoked successfully"));
    }

    @PostMapping("/{tokenId}/regenerate")
    @Operation(summary = "Regenerate API token", description = "Generate new token value. Old value becomes invalid!")
    public ResponseEntity<ApiResponse<ApiTokenCreatedDto>> regenerateToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long tokenId) {

        ApiTokenCreatedDto token = tokenService.regenerateToken(
                principal.getOrganizationId(), tokenId);
        return ResponseEntity.ok(ApiResponse.success(token,
                "Token regenerated. Save the new value - it won't be shown again!"));
    }

    @GetMapping("/{tokenId}/usage")
    @Operation(summary = "Get token usage", description = "Get API token usage statistics")
    public ResponseEntity<ApiResponse<ApiTokenUsageDto>> getTokenUsage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long tokenId,
            @RequestParam(defaultValue = "30") int days) {

        ApiTokenUsageDto usage = tokenService.getTokenUsage(
                principal.getOrganizationId(), tokenId, days);
        return ResponseEntity.ok(ApiResponse.success(usage));
    }

    @GetMapping("/scopes")
    @Operation(summary = "List available scopes", description = "Get all available API scopes")
    public ResponseEntity<ApiResponse<List<ApiScopeDto>>> listScopes() {
        List<ApiScopeDto> scopes = tokenService.listAvailableScopes();
        return ResponseEntity.ok(ApiResponse.success(scopes));
    }
}
