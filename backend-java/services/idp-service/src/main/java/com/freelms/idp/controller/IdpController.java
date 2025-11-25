package com.freelms.idp.controller;

import com.freelms.common.dto.ApiResponse;
import com.freelms.common.security.UserPrincipal;
import com.freelms.idp.dto.*;
import com.freelms.idp.service.IdpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/idp")
@RequiredArgsConstructor
public class IdpController {

    private final IdpService idpService;

    @PostMapping
    public ResponseEntity<ApiResponse<IdpDto>> createPlan(
            @Valid @RequestBody CreateIdpRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        IdpDto result = idpService.createPlan(request, principal.getId(), principal.getOrganizationId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "IDP created successfully"));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<ApiResponse<IdpDto>> getPlan(@PathVariable Long planId) {
        IdpDto result = idpService.getPlan(planId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<IdpDto>>> getMyPlans(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<IdpDto> result = idpService.getUserPlans(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{planId}/submit")
    public ResponseEntity<ApiResponse<IdpDto>> submitForApproval(@PathVariable Long planId) {
        IdpDto result = idpService.submitForApproval(planId);
        return ResponseEntity.ok(ApiResponse.success(result, "Plan submitted for approval"));
    }

    @PostMapping("/{planId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<IdpDto>> approvePlan(
            @PathVariable Long planId,
            @AuthenticationPrincipal UserPrincipal principal) {
        IdpDto result = idpService.approvePlan(planId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result, "Plan approved"));
    }

    @PostMapping("/{planId}/activate")
    public ResponseEntity<ApiResponse<IdpDto>> activatePlan(@PathVariable Long planId) {
        IdpDto result = idpService.activatePlan(planId);
        return ResponseEntity.ok(ApiResponse.success(result, "Plan activated"));
    }

    @PutMapping("/{planId}/goals/{goalId}/progress")
    public ResponseEntity<ApiResponse<IdpDto>> updateGoalProgress(
            @PathVariable Long planId,
            @PathVariable Long goalId,
            @RequestParam int progress) {
        IdpDto result = idpService.updateGoalProgress(planId, goalId, progress);
        return ResponseEntity.ok(ApiResponse.success(result, "Progress updated"));
    }

    @PostMapping("/{planId}/reviews")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<ApiResponse<IdpDto>> addReview(
            @PathVariable Long planId,
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        IdpDto result = idpService.addReview(planId, principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(result, "Review added"));
    }
}
