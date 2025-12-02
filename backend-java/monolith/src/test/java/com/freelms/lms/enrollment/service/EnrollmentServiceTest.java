package com.freelms.lms.enrollment.service;

import com.freelms.lms.common.enums.EnrollmentStatus;
import com.freelms.lms.common.exception.ConflictException;
import com.freelms.lms.common.exception.ResourceNotFoundException;
import com.freelms.lms.course.repository.CourseRepository;
import com.freelms.lms.course.repository.LessonRepository;
import com.freelms.lms.enrollment.dto.EnrollRequest;
import com.freelms.lms.enrollment.dto.EnrollmentDto;
import com.freelms.lms.enrollment.entity.Enrollment;
import com.freelms.lms.enrollment.repository.EnrollmentRepository;
import com.freelms.lms.enrollment.repository.LessonProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private LessonProgressRepository lessonProgressRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Enrollment testEnrollment;
    private EnrollRequest enrollRequest;

    @BeforeEach
    void setUp() {
        testEnrollment = Enrollment.builder()
                .id(1L)
                .userId(1L)
                .courseId(1L)
                .status(EnrollmentStatus.ACTIVE)
                .progress(0)
                .enrolledAt(LocalDateTime.now())
                .build();

        enrollRequest = EnrollRequest.builder()
                .courseId(1L)
                .build();
    }

    @Test
    @DisplayName("Should enroll user successfully")
    void shouldEnrollUserSuccessfully() {
        // Given
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 1L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);
        when(lessonProgressRepository.countCompletedByEnrollmentId(anyLong())).thenReturn(0L);
        when(lessonRepository.countByCourseId(anyLong())).thenReturn(10L);

        // When
        EnrollmentDto result = enrollmentService.enroll(1L, enrollRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getCourseId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        verify(courseRepository).incrementStudentCount(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent course")
    void shouldThrowResourceNotFoundExceptionForNonExistentCourse() {
        // Given
        when(courseRepository.existsById(anyLong())).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> enrollmentService.enroll(1L, enrollRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw ConflictException when already enrolled")
    void shouldThrowConflictExceptionWhenAlreadyEnrolled() {
        // Given
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 1L)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> enrollmentService.enroll(1L, enrollRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already enrolled");
    }

    @Test
    @DisplayName("Should get enrollment successfully")
    void shouldGetEnrollmentSuccessfully() {
        // Given
        when(enrollmentRepository.findByUserIdAndCourseId(1L, 1L)).thenReturn(Optional.of(testEnrollment));
        when(lessonProgressRepository.countCompletedByEnrollmentId(anyLong())).thenReturn(5L);
        when(lessonRepository.countByCourseId(anyLong())).thenReturn(10L);

        // When
        EnrollmentDto result = enrollmentService.getEnrollment(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalLessons()).isEqualTo(10);
        assertThat(result.getCompletedLessons()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should check enrollment status")
    void shouldCheckEnrollmentStatus() {
        // Given
        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 1L)).thenReturn(true);
        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 999L)).thenReturn(false);

        // When/Then
        assertThat(enrollmentService.isEnrolled(1L, 1L)).isTrue();
        assertThat(enrollmentService.isEnrolled(1L, 999L)).isFalse();
    }

    @Test
    @DisplayName("Should drop enrollment successfully")
    void shouldDropEnrollmentSuccessfully() {
        // Given
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(testEnrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        // When
        enrollmentService.dropEnrollment(1L, 1L);

        // Then
        verify(enrollmentRepository).save(argThat(e -> e.getStatus() == EnrollmentStatus.DROPPED));
    }

    @Test
    @DisplayName("Should get user enrollments successfully")
    void shouldGetUserEnrollmentsSuccessfully() {
        // Given
        when(enrollmentRepository.findByUserId(eq(1L), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        // When
        var result = enrollmentService.getUserEnrollments(1L, org.springframework.data.domain.Pageable.unpaged());

        // Then
        assertThat(result).isNotNull();
        verify(enrollmentRepository).findByUserId(eq(1L), any());
    }

    @Test
    @DisplayName("Should update progress successfully")
    void shouldUpdateProgressSuccessfully() {
        // Given
        com.freelms.lms.enrollment.dto.UpdateProgressRequest progressRequest = 
                com.freelms.lms.enrollment.dto.UpdateProgressRequest.builder()
                        .lessonId(1L)
                        .completed(true)
                        .timeSpentSeconds(300)
                        .videoPositionSeconds(280)
                        .build();

        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(testEnrollment));
        when(lessonProgressRepository.findByEnrollmentIdAndLessonId(1L, 1L)).thenReturn(Optional.empty());
        when(lessonProgressRepository.save(any())).thenReturn(null);
        when(lessonRepository.countByCourseId(1L)).thenReturn(10L);
        when(lessonProgressRepository.countCompletedByEnrollmentId(1L)).thenReturn(1L);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        // When
        EnrollmentDto result = enrollmentService.updateProgress(1L, 1L, progressRequest);

        // Then
        assertThat(result).isNotNull();
        verify(lessonProgressRepository).save(any());
        verify(enrollmentRepository).save(argThat(e -> e.getCurrentLessonId().equals(1L)));
    }

    @Test
    @DisplayName("Should throw ForbiddenException when updating progress for another user's enrollment")
    void shouldThrowForbiddenExceptionForUpdateProgressNotOwner() {
        // Given
        com.freelms.lms.enrollment.dto.UpdateProgressRequest progressRequest = 
                com.freelms.lms.enrollment.dto.UpdateProgressRequest.builder()
                        .lessonId(1L)
                        .completed(true)
                        .build();

        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(testEnrollment));

        // When/Then
        assertThatThrownBy(() -> enrollmentService.updateProgress(999L, 1L, progressRequest))
                .isInstanceOf(com.freelms.lms.common.exception.ForbiddenException.class)
                .hasMessageContaining("not authorized");
    }
}
