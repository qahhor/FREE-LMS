package com.freelms.reporting.controller;

import com.freelms.common.dto.ApiResponse;
import com.freelms.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reporting")
@RequiredArgsConstructor
public class ReportingController {

    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<ApiResponse<?>> getReports(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "Reports list endpoint"));
    }

    @PostMapping("/reports/{reportId}/execute")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<ApiResponse<?>> executeReport(
            @PathVariable Long reportId,
            @RequestParam(required = false) String format) {
        return ResponseEntity.ok(ApiResponse.success(null, "Execute report endpoint"));
    }

    @GetMapping("/roi")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<?>> getTrainingROI(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "Training ROI endpoint"));
    }

    @GetMapping("/heatmap")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<?>> getActivityHeatmap(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "Activity heatmap endpoint"));
    }

    @GetMapping("/funnel/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<?>> getCourseFunnel(@PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.success(null, "Course funnel endpoint"));
    }

    @GetMapping("/export/bi")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> exportForBI(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String format) {
        return ResponseEntity.ok(ApiResponse.success(null, "BI export endpoint"));
    }

    @GetMapping("/dashboards")
    public ResponseEntity<ApiResponse<?>> getDashboards(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "Dashboards endpoint"));
    }
}
