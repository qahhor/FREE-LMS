package com.freelms.feedback.controller;

import com.freelms.common.dto.ApiResponse;
import com.freelms.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    @GetMapping("/cycles")
    public ResponseEntity<ApiResponse<?>> getCycles(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "Feedback cycles endpoint"));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<?>> getPendingReviews(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "Pending reviews endpoint"));
    }

    @GetMapping("/summary/{userId}")
    public ResponseEntity<ApiResponse<?>> getUserSummary(
            @PathVariable Long userId,
            @RequestParam Long cycleId) {
        return ResponseEntity.ok(ApiResponse.success(null, "Feedback summary endpoint"));
    }
}
