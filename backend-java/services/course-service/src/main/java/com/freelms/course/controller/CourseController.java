package com.freelms.course.controller;

import com.freelms.common.dto.ApiResponse;
import com.freelms.common.dto.PagedResponse;
import com.freelms.common.enums.CourseLevel;
import com.freelms.common.security.CurrentUser;
import com.freelms.common.security.UserPrincipal;
import com.freelms.course.dto.*;
import com.freelms.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Course management endpoints")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Get all published courses")
    public ResponseEntity<ApiResponse<PagedResponse<CourseDto>>> getAllCourses(
            @PageableDefault(size = 20, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<CourseDto> courses = courseService.getAllCourses(pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID")
    public ResponseEntity<ApiResponse<CourseDto>> getCourseById(@PathVariable Long id) {
        CourseDto course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get course by slug")
    public ResponseEntity<ApiResponse<CourseDto>> getCourseBySlug(@PathVariable String slug) {
        CourseDto course = courseService.getCourseBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    @GetMapping("/search")
    @Operation(summary = "Search courses")
    public ResponseEntity<ApiResponse<PagedResponse<CourseDto>>> searchCourses(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<CourseDto> courses = courseService.searchCourses(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter courses")
    public ResponseEntity<ApiResponse<PagedResponse<CourseDto>>> filterCourses(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) CourseLevel level,
            @RequestParam(required = false) Boolean isFree,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<CourseDto> courses = courseService.filterCourses(categoryId, level, isFree, pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular courses")
    public ResponseEntity<ApiResponse<List<CourseDto>>> getPopularCourses(
            @RequestParam(defaultValue = "10") int limit) {
        List<CourseDto> courses = courseService.getPopularCourses(limit);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent courses")
    public ResponseEntity<ApiResponse<List<CourseDto>>> getRecentCourses(
            @RequestParam(defaultValue = "10") int limit) {
        List<CourseDto> courses = courseService.getRecentCourses(limit);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/instructor/{instructorId}")
    @Operation(summary = "Get courses by instructor")
    public ResponseEntity<ApiResponse<PagedResponse<CourseDto>>> getCoursesByInstructor(
            @PathVariable Long instructorId,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<CourseDto> courses = courseService.getCoursesByInstructor(instructorId, pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get courses by category")
    public ResponseEntity<ApiResponse<PagedResponse<CourseDto>>> getCoursesByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<CourseDto> courses = courseService.getCoursesByCategory(categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Create a new course")
    public ResponseEntity<ApiResponse<CourseDto>> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            @CurrentUser UserPrincipal userPrincipal) {
        CourseDto course = courseService.createCourse(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(course, "Course created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Update a course")
    public ResponseEntity<ApiResponse<CourseDto>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request,
            @CurrentUser UserPrincipal userPrincipal) {
        CourseDto course = courseService.updateCourse(id, request, userPrincipal.getId(), userPrincipal.getRole());
        return ResponseEntity.ok(ApiResponse.success(course, "Course updated successfully"));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Publish a course")
    public ResponseEntity<ApiResponse<CourseDto>> publishCourse(
            @PathVariable Long id,
            @CurrentUser UserPrincipal userPrincipal) {
        CourseDto course = courseService.publishCourse(id, userPrincipal.getId(), userPrincipal.getRole());
        return ResponseEntity.ok(ApiResponse.success(course, "Course published successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Delete a course")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @PathVariable Long id,
            @CurrentUser UserPrincipal userPrincipal) {
        courseService.deleteCourse(id, userPrincipal.getId(), userPrincipal.getRole());
        return ResponseEntity.ok(ApiResponse.success(null, "Course deleted successfully"));
    }
}
