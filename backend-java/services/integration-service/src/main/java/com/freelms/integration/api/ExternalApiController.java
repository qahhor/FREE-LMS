package com.freelms.integration.api;

import com.freelms.common.dto.ApiResponse;
import com.freelms.common.dto.PageResponse;
import com.freelms.integration.dto.*;
import com.freelms.integration.service.ExternalApiService;
import com.freelms.integration.token.ApiToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Smartup LMS - External API Controller
 *
 * REST API for ERP/CRM/HRIS integration.
 * All endpoints require API token authentication.
 *
 * Authentication: Bearer token in Authorization header
 * Example: Authorization: Bearer slms_xxxxxxxxxxxx
 */
@RestController
@RequestMapping("/api/external/v1")
@RequiredArgsConstructor
@Tag(name = "External API", description = "API for ERP/CRM/HRIS integration")
@SecurityRequirement(name = "ApiToken")
public class ExternalApiController {

    private final ExternalApiService apiService;

    // =========================================================================
    // Users API - For HR System Integration
    // =========================================================================

    @GetMapping("/users")
    @Operation(summary = "List users", description = "Get paginated list of users. Requires scope: users:read")
    public ResponseEntity<ApiResponse<PageResponse<UserExportDto>>> getUsers(
            @RequestAttribute("apiToken") ApiToken token,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search,
            Pageable pageable) {

        apiService.validateScope(token, ApiToken.Scopes.USERS_READ);
        PageResponse<UserExportDto> users = apiService.getUsers(
                token.getOrganizationId(), status, department, role, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user by ID", description = "Get single user details. Requires scope: users:read")
    public ResponseEntity<ApiResponse<UserExportDto>> getUser(
            @RequestAttribute("apiToken") ApiToken token,
            @PathVariable String userId) {

        apiService.validateScope(token, ApiToken.Scopes.USERS_READ);
        UserExportDto user = apiService.getUser(token.getOrganizationId(), userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/users")
    @Operation(summary = "Create user", description = "Create new user from HR system. Requires scope: users:write")
    public ResponseEntity<ApiResponse<UserExportDto>> createUser(
            @RequestAttribute("apiToken") ApiToken token,
            @Valid @RequestBody UserImportDto request) {

        apiService.validateScope(token, ApiToken.Scopes.USERS_WRITE);
        UserExportDto user = apiService.createUser(token.getOrganizationId(), request);
        return ResponseEntity.ok(ApiResponse.success(user, "User created successfully"));
    }

    @PutMapping("/users/{userId}")
    @Operation(summary = "Update user", description = "Update user from HR system. Requires scope: users:write")
    public ResponseEntity<ApiResponse<UserExportDto>> updateUser(
            @RequestAttribute("apiToken") ApiToken token,
            @PathVariable String userId,
            @Valid @RequestBody UserImportDto request) {

        apiService.validateScope(token, ApiToken.Scopes.USERS_WRITE);
        UserExportDto user = apiService.updateUser(token.getOrganizationId(), userId, request);
        return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
    }

    @PostMapping("/users/bulk")
    @Operation(summary = "Bulk import users", description = "Import multiple users. Requires scope: users:write")
    public ResponseEntity<ApiResponse<BulkImportResultDto>> bulkImportUsers(
            @RequestAttribute("apiToken") ApiToken token,
            @Valid @RequestBody List<UserImportDto> users) {

        apiService.validateScope(token, ApiToken.Scopes.USERS_WRITE);
        BulkImportResultDto result = apiService.bulkImportUsers(token.getOrganizationId(), users);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Deactivate user", description = "Deactivate user (soft delete). Requires scope: users:write")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(
            @RequestAttribute("apiToken") ApiToken token,
            @PathVariable String userId) {

        apiService.validateScope(token, ApiToken.Scopes.USERS_WRITE);
        apiService.deactivateUser(token.getOrganizationId(), userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User deactivated successfully"));
    }

    // =========================================================================
    // Courses API - For Content Integration
    // =========================================================================

    @GetMapping("/courses")
    @Operation(summary = "List courses", description = "Get paginated list of courses. Requires scope: courses:read")
    public ResponseEntity<ApiResponse<PageResponse<CourseExportDto>>> getCourses(
            @RequestAttribute("apiToken") ApiToken token,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            Pageable pageable) {

        apiService.validateScope(token, ApiToken.Scopes.COURSES_READ);
        PageResponse<CourseExportDto> courses = apiService.getCourses(
                token.getOrganizationId(), category, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/courses/{courseId}")
    @Operation(summary = "Get course by ID", description = "Get course details with structure. Requires scope: courses:read")
    public ResponseEntity<ApiResponse<CourseExportDto>> getCourse(
            @RequestAttribute("apiToken") ApiToken token,
            @PathVariable String courseId) {

        apiService.validateScope(token, ApiToken.Scopes.COURSES_READ);
        CourseExportDto course = apiService.getCourse(token.getOrganizationId(), courseId);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    // =========================================================================
    // Enrollments API - For LMS Integration
    // =========================================================================

    @GetMapping("/enrollments")
    @Operation(summary = "List enrollments", description = "Get enrollments. Requires scope: enrollments:read")
    public ResponseEntity<ApiResponse<PageResponse<EnrollmentExportDto>>> getEnrollments(
            @RequestAttribute("apiToken") ApiToken token,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String status,
            Pageable pageable) {

        apiService.validateScope(token, ApiToken.Scopes.ENROLLMENTS_READ);
        PageResponse<EnrollmentExportDto> enrollments = apiService.getEnrollments(
                token.getOrganizationId(), userId, courseId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @PostMapping("/enrollments")
    @Operation(summary = "Create enrollment", description = "Enroll user in course. Requires scope: enrollments:write")
    public ResponseEntity<ApiResponse<EnrollmentExportDto>> createEnrollment(
            @RequestAttribute("apiToken") ApiToken token,
            @Valid @RequestBody EnrollmentCreateDto request) {

        apiService.validateScope(token, ApiToken.Scopes.ENROLLMENTS_WRITE);
        EnrollmentExportDto enrollment = apiService.createEnrollment(token.getOrganizationId(), request);
        return ResponseEntity.ok(ApiResponse.success(enrollment, "Enrollment created successfully"));
    }

    @PostMapping("/enrollments/bulk")
    @Operation(summary = "Bulk enroll users", description = "Enroll multiple users. Requires scope: enrollments:write")
    public ResponseEntity<ApiResponse<BulkImportResultDto>> bulkEnroll(
            @RequestAttribute("apiToken") ApiToken token,
            @Valid @RequestBody BulkEnrollmentDto request) {

        apiService.validateScope(token, ApiToken.Scopes.ENROLLMENTS_WRITE);
        BulkImportResultDto result = apiService.bulkEnroll(token.getOrganizationId(), request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // =========================================================================
    // Progress API - For Reporting Integration
    // =========================================================================

    @GetMapping("/progress")
    @Operation(summary = "Get learning progress", description = "Get progress data. Requires scope: progress:read")
    public ResponseEntity<ApiResponse<PageResponse<ProgressExportDto>>> getProgress(
            @RequestAttribute("apiToken") ApiToken token,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            Pageable pageable) {

        apiService.validateScope(token, ApiToken.Scopes.PROGRESS_READ);
        PageResponse<ProgressExportDto> progress = apiService.getProgress(
                token.getOrganizationId(), userId, courseId, dateFrom, dateTo, pageable);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    @GetMapping("/progress/user/{userId}")
    @Operation(summary = "Get user progress summary", description = "Get user's overall progress. Requires scope: progress:read")
    public ResponseEntity<ApiResponse<UserProgressSummaryDto>> getUserProgressSummary(
            @RequestAttribute("apiToken") ApiToken token,
            @PathVariable String userId) {

        apiService.validateScope(token, ApiToken.Scopes.PROGRESS_READ);
        UserProgressSummaryDto summary = apiService.getUserProgressSummary(token.getOrganizationId(), userId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    // =========================================================================
    // Reports API - For BI/Analytics Integration
    // =========================================================================

    @GetMapping("/reports/completion")
    @Operation(summary = "Completion report", description = "Get completion statistics. Requires scope: reports:read")
    public ResponseEntity<ApiResponse<CompletionReportDto>> getCompletionReport(
            @RequestAttribute("apiToken") ApiToken token,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String groupBy) {

        apiService.validateScope(token, ApiToken.Scopes.REPORTS_READ);
        CompletionReportDto report = apiService.getCompletionReport(
                token.getOrganizationId(), dateFrom, dateTo, groupBy);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/reports/compliance")
    @Operation(summary = "Compliance report", description = "Get compliance status. Requires scope: reports:read")
    public ResponseEntity<ApiResponse<ComplianceReportDto>> getComplianceReport(
            @RequestAttribute("apiToken") ApiToken token,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String trainingType) {

        apiService.validateScope(token, ApiToken.Scopes.REPORTS_READ);
        ComplianceReportDto report = apiService.getComplianceReport(
                token.getOrganizationId(), department, trainingType);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/reports/activity")
    @Operation(summary = "Activity report", description = "Get learning activity. Requires scope: reports:read")
    public ResponseEntity<ApiResponse<List<ActivityReportDto>>> getActivityReport(
            @RequestAttribute("apiToken") ApiToken token,
            @RequestParam String dateFrom,
            @RequestParam String dateTo,
            @RequestParam(defaultValue = "day") String interval) {

        apiService.validateScope(token, ApiToken.Scopes.REPORTS_READ);
        List<ActivityReportDto> report = apiService.getActivityReport(
                token.getOrganizationId(), dateFrom, dateTo, interval);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    // =========================================================================
    // Certificates API
    // =========================================================================

    @GetMapping("/certificates")
    @Operation(summary = "List certificates", description = "Get issued certificates. Requires scope: progress:read")
    public ResponseEntity<ApiResponse<PageResponse<CertificateExportDto>>> getCertificates(
            @RequestAttribute("apiToken") ApiToken token,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) Boolean valid,
            Pageable pageable) {

        apiService.validateScope(token, ApiToken.Scopes.PROGRESS_READ);
        PageResponse<CertificateExportDto> certificates = apiService.getCertificates(
                token.getOrganizationId(), userId, courseId, valid, pageable);
        return ResponseEntity.ok(ApiResponse.success(certificates));
    }

    @GetMapping("/certificates/{certificateId}/verify")
    @Operation(summary = "Verify certificate", description = "Verify certificate authenticity. Public endpoint.")
    public ResponseEntity<ApiResponse<CertificateVerificationDto>> verifyCertificate(
            @PathVariable String certificateId) {

        CertificateVerificationDto result = apiService.verifyCertificate(certificateId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
