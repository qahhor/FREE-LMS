package com.freelms.enrollment.controller;

import com.freelms.common.dto.ApiResponse;
import com.freelms.common.dto.PagedResponse;
import com.freelms.common.enums.EnrollmentStatus;
import com.freelms.common.security.CurrentUser;
import com.freelms.common.security.UserPrincipal;
import com.freelms.enrollment.dto.EnrollmentDto;
import com.freelms.enrollment.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "Enrollment management endpoints")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/courses/{courseId}")
    @Operation(summary = "Enroll in a course")
    public ResponseEntity<ApiResponse<EnrollmentDto>> enroll(
            @PathVariable Long courseId,
            @CurrentUser UserPrincipal userPrincipal) {
        EnrollmentDto enrollment = enrollmentService.enroll(userPrincipal.getId(), courseId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(enrollment, "Successfully enrolled in course"));
    }

    @GetMapping("/my-courses")
    @Operation(summary = "Get current user's enrollments")
    public ResponseEntity<ApiResponse<PagedResponse<EnrollmentDto>>> getMyEnrollments(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(required = false) EnrollmentStatus status,
            @PageableDefault(size = 20, sort = "enrolledAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<EnrollmentDto> enrollments;
        if (status != null) {
            enrollments = enrollmentService.getUserEnrollmentsByStatus(userPrincipal.getId(), status, pageable);
        } else {
            enrollments = enrollmentService.getUserEnrollments(userPrincipal.getId(), pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @GetMapping("/courses/{courseId}")
    @Operation(summary = "Get enrollment for specific course")
    public ResponseEntity<ApiResponse<EnrollmentDto>> getEnrollment(
            @PathVariable Long courseId,
            @CurrentUser UserPrincipal userPrincipal) {
        EnrollmentDto enrollment = enrollmentService.getEnrollment(userPrincipal.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success(enrollment));
    }

    @GetMapping("/courses/{courseId}/check")
    @Operation(summary = "Check if user is enrolled in course")
    public ResponseEntity<ApiResponse<Boolean>> checkEnrollment(
            @PathVariable Long courseId,
            @CurrentUser UserPrincipal userPrincipal) {
        boolean isEnrolled = enrollmentService.isEnrolled(userPrincipal.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success(isEnrolled));
    }

    @PutMapping("/{enrollmentId}/progress")
    @Operation(summary = "Update enrollment progress")
    public ResponseEntity<ApiResponse<EnrollmentDto>> updateProgress(
            @PathVariable Long enrollmentId,
            @RequestParam int progress) {
        EnrollmentDto enrollment = enrollmentService.updateProgress(enrollmentId, progress);
        return ResponseEntity.ok(ApiResponse.success(enrollment, "Progress updated"));
    }

    @PostMapping("/courses/{courseId}/complete")
    @Operation(summary = "Mark course as completed")
    public ResponseEntity<ApiResponse<EnrollmentDto>> completeCourse(
            @PathVariable Long courseId,
            @CurrentUser UserPrincipal userPrincipal) {
        EnrollmentDto enrollment = enrollmentService.completeCourse(userPrincipal.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success(enrollment, "Course completed successfully"));
    }

    @DeleteMapping("/courses/{courseId}")
    @Operation(summary = "Drop a course")
    public ResponseEntity<ApiResponse<Void>> dropCourse(
            @PathVariable Long courseId,
            @CurrentUser UserPrincipal userPrincipal) {
        enrollmentService.dropCourse(userPrincipal.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success(null, "Course dropped successfully"));
    }
}
