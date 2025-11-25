package com.freelms.mentoring.controller;

import com.freelms.common.dto.ApiResponse;
import com.freelms.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mentoring")
@RequiredArgsConstructor
public class MentoringController {

    @GetMapping("/mentors")
    public ResponseEntity<ApiResponse<?>> getMentors(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String expertise,
            @RequestParam(required = false) Long skillId) {
        return ResponseEntity.ok(ApiResponse.success(null, "Mentors list endpoint"));
    }

    @PostMapping("/mentors/profile")
    public ResponseEntity<ApiResponse<?>> createMentorProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "Create mentor profile endpoint"));
    }

    @GetMapping("/relationships/my")
    public ResponseEntity<ApiResponse<?>> getMyRelationships(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "My mentoring relationships endpoint"));
    }

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<?>> scheduleSession(
            @RequestParam Long relationshipId,
            @RequestParam String title) {
        return ResponseEntity.ok(ApiResponse.success(null, "Schedule session endpoint"));
    }
}
