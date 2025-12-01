package com.freelms.lms.enrollment.service;

import com.freelms.lms.common.dto.PagedResponse;
import com.freelms.lms.common.enums.EnrollmentStatus;
import com.freelms.lms.common.exception.BadRequestException;
import com.freelms.lms.common.exception.ConflictException;
import com.freelms.lms.common.exception.ForbiddenException;
import com.freelms.lms.common.exception.ResourceNotFoundException;
import com.freelms.lms.course.entity.Course;
import com.freelms.lms.course.repository.CourseRepository;
import com.freelms.lms.course.repository.LessonRepository;
import com.freelms.lms.enrollment.dto.CertificateDto;
import com.freelms.lms.enrollment.dto.EnrollRequest;
import com.freelms.lms.enrollment.dto.EnrollmentDto;
import com.freelms.lms.enrollment.dto.UpdateProgressRequest;
import com.freelms.lms.enrollment.entity.Certificate;
import com.freelms.lms.enrollment.entity.Enrollment;
import com.freelms.lms.enrollment.entity.LessonProgress;
import com.freelms.lms.enrollment.repository.EnrollmentRepository;
import com.freelms.lms.enrollment.repository.LessonProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public EnrollmentDto enroll(Long userId, EnrollRequest request) {
        Long courseId = request.getCourseId();
        log.info("User {} enrolling in course {}", userId, courseId);

        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }

        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new ConflictException("User is already enrolled in this course");
        }

        Enrollment enrollment = Enrollment.builder()
                .userId(userId)
                .courseId(courseId)
                .status(EnrollmentStatus.ACTIVE)
                .enrolledAt(LocalDateTime.now())
                .build();

        enrollment = enrollmentRepository.save(enrollment);
        courseRepository.incrementStudentCount(courseId);

        log.info("User {} enrolled in course {} with enrollment ID {}", userId, courseId, enrollment.getId());
        return toDto(enrollment);
    }

    @Transactional(readOnly = true)
    public EnrollmentDto getEnrollment(Long userId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        return toDto(enrollment);
    }

    @Transactional(readOnly = true)
    public EnrollmentDto getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));
        return toDto(enrollment);
    }

    @Transactional(readOnly = true)
    public PagedResponse<EnrollmentDto> getUserEnrollments(Long userId, Pageable pageable) {
        Page<Enrollment> enrollments = enrollmentRepository.findByUserId(userId, pageable);
        return toPagedEnrollmentResponse(enrollments);
    }

    @Transactional(readOnly = true)
    public PagedResponse<EnrollmentDto> getUserActiveEnrollments(Long userId, Pageable pageable) {
        Page<Enrollment> enrollments = enrollmentRepository.findByUserIdAndStatus(
                userId, EnrollmentStatus.ACTIVE, pageable);
        return toPagedEnrollmentResponse(enrollments);
    }

    @Transactional(readOnly = true)
    public PagedResponse<EnrollmentDto> getUserCompletedEnrollments(Long userId, Pageable pageable) {
        Page<Enrollment> enrollments = enrollmentRepository.findByUserIdAndStatus(
                userId, EnrollmentStatus.COMPLETED, pageable);
        return toPagedEnrollmentResponse(enrollments);
    }

    @Transactional(readOnly = true)
    public PagedResponse<EnrollmentDto> getRecentEnrollments(Long userId, Pageable pageable) {
        Page<Enrollment> enrollments = enrollmentRepository.findRecentByUserId(userId, pageable);
        return toPagedEnrollmentResponse(enrollments);
    }

    @Transactional
    public EnrollmentDto updateProgress(Long userId, Long enrollmentId, UpdateProgressRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));

        if (!enrollment.getUserId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to update this enrollment");
        }

        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new BadRequestException("Cannot update progress for non-active enrollment");
        }

        // Update or create lesson progress
        final Enrollment enrollmentRef = enrollment;
        LessonProgress lessonProgress = lessonProgressRepository
                .findByEnrollmentIdAndLessonId(enrollmentId, request.getLessonId())
                .orElseGet(() -> LessonProgress.builder()
                        .enrollment(enrollmentRef)
                        .lessonId(request.getLessonId())
                        .build());

        if (request.isCompleted()) {
            lessonProgress.markCompleted();
        }
        if (request.getTimeSpentSeconds() != null) {
            lessonProgress.updateTimeSpent(request.getTimeSpentSeconds());
        }
        if (request.getVideoPositionSeconds() != null) {
            lessonProgress.updateVideoPosition(request.getVideoPositionSeconds());
        }

        lessonProgressRepository.save(lessonProgress);

        // Update enrollment progress
        enrollment.setCurrentLessonId(request.getLessonId());
        enrollment.setLastAccessedAt(LocalDateTime.now());

        // Calculate overall progress
        long totalLessons = lessonRepository.countByCourseId(enrollment.getCourseId());
        long completedLessons = lessonProgressRepository.countCompletedByEnrollmentId(enrollmentId);
        int progress = totalLessons > 0 ? (int) ((completedLessons * 100) / totalLessons) : 0;
        enrollment.updateProgress(progress);

        enrollment = enrollmentRepository.save(enrollment);
        log.info("Progress updated for enrollment {}: {}%", enrollmentId, progress);

        return toDto(enrollment);
    }

    @Transactional
    public void dropEnrollment(Long userId, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));

        if (!enrollment.getUserId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to drop this enrollment");
        }

        enrollment.drop();
        enrollmentRepository.save(enrollment);
        log.info("User {} dropped enrollment {}", userId, enrollmentId);
    }

    @Transactional(readOnly = true)
    public boolean isEnrolled(Long userId, Long courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Transactional(readOnly = true)
    public List<Long> getEnrolledCourseIds(Long userId) {
        return enrollmentRepository.findCourseIdsByUserId(userId);
    }

    private PagedResponse<EnrollmentDto> toPagedEnrollmentResponse(Page<Enrollment> enrollments) {
        if (enrollments.getContent().isEmpty()) {
            return PagedResponse.of(enrollments, List.of());
        }
        
        // Load all courses in one query to avoid N+1 problem
        Map<Long, Course> coursesMap = loadCoursesMap(enrollments.getContent());
        
        List<EnrollmentDto> dtos = enrollments.getContent().stream()
                .map(e -> toDto(e, coursesMap.get(e.getCourseId())))
                .toList();
        return PagedResponse.of(enrollments, dtos);
    }

    private Map<Long, Course> loadCoursesMap(List<Enrollment> enrollments) {
        List<Long> courseIds = enrollments.stream()
                .map(Enrollment::getCourseId)
                .distinct()
                .toList();
        return courseRepository.findByIdIn(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
    }

    private EnrollmentDto toDto(Enrollment enrollment) {
        // Fetch course data for this single enrollment
        Course course = courseRepository.findById(enrollment.getCourseId()).orElse(null);
        return toDto(enrollment, course);
    }

    private EnrollmentDto toDto(Enrollment enrollment, Course course) {
        long completedLessons = lessonProgressRepository.countCompletedByEnrollmentId(enrollment.getId());
        long totalLessons = lessonRepository.countByCourseId(enrollment.getCourseId());

        return EnrollmentDto.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUserId())
                .courseId(enrollment.getCourseId())
                .courseTitle(course != null ? course.getTitle() : null)
                .courseThumbnail(course != null ? course.getThumbnailUrl() : null)
                .status(enrollment.getStatus())
                .progress(enrollment.getProgress())
                .enrolledAt(enrollment.getEnrolledAt())
                .completedAt(enrollment.getCompletedAt())
                .lastAccessedAt(enrollment.getLastAccessedAt())
                .currentLessonId(enrollment.getCurrentLessonId())
                .totalLessons((int) totalLessons)
                .completedLessons((int) completedLessons)
                .certificate(toCertificateDto(enrollment.getCertificate()))
                .build();
    }

    private CertificateDto toCertificateDto(Certificate certificate) {
        if (certificate == null) {
            return null;
        }
        return CertificateDto.builder()
                .id(certificate.getId())
                .certificateNumber(certificate.getCertificateNumber())
                .userId(certificate.getUserId())
                .courseId(certificate.getCourseId())
                .courseTitle(certificate.getCourseTitle())
                .userName(certificate.getUserName())
                .issuedAt(certificate.getIssuedAt())
                .expiryAt(certificate.getExpiryAt())
                .verificationUrl(certificate.getVerificationUrl())
                .pdfUrl(certificate.getPdfUrl())
                .expired(certificate.isExpired())
                .build();
    }
}
