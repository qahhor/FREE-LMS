package com.freelms.learningpath.controller;

import com.freelms.common.dto.ApiResponse;
import com.freelms.common.dto.PagedResponse;
import com.freelms.common.enums.CourseStatus;
import com.freelms.common.security.UserPrincipal;
import com.freelms.learningpath.dto.*;
import com.freelms.learningpath.entity.LearningPathItemProgress;
import com.freelms.learningpath.service.LearningPathService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/learning-paths")
@RequiredArgsConstructor
public class LearningPathController {

    private final LearningPathService learningPathService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<LearningPathDto>> createLearningPath(
            @Valid @RequestBody CreateLearningPathRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        LearningPathDto result = learningPathService.createLearningPath(
                request, principal.getOrganizationId(), principal.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Learning path created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LearningPathDto>> getLearningPath(@PathVariable Long id) {
        LearningPathDto result = learningPathService.getLearningPath(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<LearningPathDto>>> getLearningPaths(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        PagedResponse<LearningPathDto> result = learningPathService.getLearningPaths(
                principal.getOrganizationId(), status, search, PageRequest.of(page, size, sort));

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<LearningPathDto>> publishLearningPath(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        LearningPathDto result = learningPathService.publishLearningPath(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result, "Learning path published successfully"));
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<ApiResponse<EnrollmentDto>> enrollInPath(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        EnrollmentDto result = learningPathService.enrollUser(id, principal.getId(), null, null, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Enrolled successfully"));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<EnrollmentDto>> assignToUser(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) LocalDateTime deadline,
            @AuthenticationPrincipal UserPrincipal principal) {

        EnrollmentDto result = learningPathService.enrollUser(id, userId, principal.getId(), note, deadline);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Learning path assigned successfully"));
    }

    @PutMapping("/enrollments/{enrollmentId}/progress")
    public ResponseEntity<ApiResponse<EnrollmentDto>> updateProgress(
            @PathVariable Long enrollmentId,
            @RequestParam Long itemId,
            @RequestParam LearningPathItemProgress.ProgressStatus status,
            @RequestParam(required = false) Integer score) {

        EnrollmentDto result = learningPathService.updateProgress(enrollmentId, itemId, status, score);
        return ResponseEntity.ok(ApiResponse.success(result, "Progress updated successfully"));
    }

    // Course Prerequisites
    @PostMapping("/courses/{courseId}/prerequisites")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> addPrerequisite(
            @PathVariable Long courseId,
            @RequestParam Long prerequisiteCourseId,
            @RequestParam(defaultValue = "true") boolean mandatory,
            @RequestParam(defaultValue = "100") int minCompletion) {

        learningPathService.addCoursePrerequisite(courseId, prerequisiteCourseId, mandatory, minCompletion);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Prerequisite added successfully"));
    }

    @GetMapping("/courses/{courseId}/prerequisites")
    public ResponseEntity<ApiResponse<?>> getCoursePrerequisites(@PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.success(learningPathService.getCoursePrerequisites(courseId)));
    }
}
