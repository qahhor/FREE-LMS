package com.freelms.social.controller;

import com.freelms.common.dto.ApiResponse;
import com.freelms.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/social")
@RequiredArgsConstructor
public class SocialLearningController {

    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<?>> getQuestions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long courseId) {
        return ResponseEntity.ok(ApiResponse.success(null, "Questions endpoint"));
    }

    @PostMapping("/questions")
    public ResponseEntity<ApiResponse<?>> createQuestion(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "Create question endpoint"));
    }

    @GetMapping("/study-groups")
    public ResponseEntity<ApiResponse<?>> getStudyGroups(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "Study groups endpoint"));
    }

    @PostMapping("/study-groups")
    public ResponseEntity<ApiResponse<?>> createStudyGroup(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "Create study group endpoint"));
    }

    @GetMapping("/peer-content")
    public ResponseEntity<ApiResponse<?>> getPeerContent(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(null, "Peer content endpoint"));
    }
}
