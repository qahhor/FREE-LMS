package com.freelms.compliance.controller;

import com.freelms.common.dto.ApiResponse;
import com.freelms.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/compliance")
@RequiredArgsConstructor
public class ComplianceController {

    @GetMapping("/trainings")
    public ResponseEntity<ApiResponse<?>> getComplianceTrainings(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "Compliance trainings endpoint"));
    }

    @GetMapping("/my-status")
    public ResponseEntity<ApiResponse<?>> getMyComplianceStatus(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "My compliance status endpoint"));
    }

    @GetMapping("/certifications")
    public ResponseEntity<ApiResponse<?>> getMyCertifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "My certifications endpoint"));
    }

    @PostMapping("/certifications")
    public ResponseEntity<ApiResponse<?>> addCertification(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "Add certification endpoint"));
    }

    @GetMapping("/audit-log")
    public ResponseEntity<ApiResponse<?>> getAuditLog(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String entityType) {
        return ResponseEntity.ok(ApiResponse.success(null, "Audit log endpoint"));
    }

    @PostMapping("/e-sign")
    public ResponseEntity<ApiResponse<?>> signDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String documentType,
            @RequestParam Long documentId,
            @RequestParam String statement) {
        return ResponseEntity.ok(ApiResponse.success(null, "E-signature endpoint"));
    }
}
