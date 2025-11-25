package com.freelms.enrollment.service;

import com.freelms.common.dto.PagedResponse;
import com.freelms.common.enums.EnrollmentStatus;
import com.freelms.common.exception.BadRequestException;
import com.freelms.common.exception.ConflictException;
import com.freelms.common.exception.ResourceNotFoundException;
import com.freelms.enrollment.dto.EnrollmentDto;
import com.freelms.enrollment.entity.Certificate;
import com.freelms.enrollment.entity.Enrollment;
import com.freelms.enrollment.mapper.EnrollmentMapper;
import com.freelms.enrollment.repository.CertificateRepository;
import com.freelms.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CertificateRepository certificateRepository;
    private final EnrollmentMapper enrollmentMapper;

    @Transactional
    public EnrollmentDto enroll(Long userId, Long courseId) {
        log.info("User {} enrolling in course {}", userId, courseId);

        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new ConflictException("User is already enrolled in this course");
        }

        Enrollment enrollment = Enrollment.builder()
                .userId(userId)
                .courseId(courseId)
                .status(EnrollmentStatus.ACTIVE)
                .progress(0)
                .enrolledAt(LocalDateTime.now())
                .build();

        enrollment = enrollmentRepository.save(enrollment);
        log.info("Enrollment created with ID: {}", enrollment.getId());

        return enrollmentMapper.toDto(enrollment);
    }

    @Transactional(readOnly = true)
    public EnrollmentDto getEnrollment(Long userId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        return enrollmentMapper.toDto(enrollment);
    }

    @Transactional(readOnly = true)
    public PagedResponse<EnrollmentDto> getUserEnrollments(Long userId, Pageable pageable) {
        Page<Enrollment> page = enrollmentRepository.findByUserId(userId, pageable);
        List<EnrollmentDto> dtos = enrollmentMapper.toDtoList(page.getContent());
        return PagedResponse.of(page, dtos);
    }

    @Transactional(readOnly = true)
    public PagedResponse<EnrollmentDto> getUserEnrollmentsByStatus(Long userId, EnrollmentStatus status, Pageable pageable) {
        Page<Enrollment> page = enrollmentRepository.findByUserIdAndStatus(userId, status, pageable);
        List<EnrollmentDto> dtos = enrollmentMapper.toDtoList(page.getContent());
        return PagedResponse.of(page, dtos);
    }

    @Transactional
    public EnrollmentDto updateProgress(Long enrollmentId, int progress) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));

        enrollment.updateProgress(progress);
        enrollment = enrollmentRepository.save(enrollment);

        // Generate certificate if completed
        if (enrollment.getStatus() == EnrollmentStatus.COMPLETED && enrollment.getCertificate() == null) {
            generateCertificate(enrollment);
        }

        return enrollmentMapper.toDto(enrollment);
    }

    @Transactional
    public EnrollmentDto completeCourse(Long userId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            throw new BadRequestException("Course is already completed");
        }

        enrollment.complete();
        enrollment = enrollmentRepository.save(enrollment);

        // Generate certificate
        if (enrollment.getCertificate() == null) {
            generateCertificate(enrollment);
        }

        log.info("Course {} completed by user {}", courseId, userId);
        return enrollmentMapper.toDto(enrollment);
    }

    @Transactional
    public void dropCourse(Long userId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        enrollment.drop();
        enrollmentRepository.save(enrollment);
        log.info("User {} dropped course {}", userId, courseId);
    }

    private void generateCertificate(Enrollment enrollment) {
        Certificate certificate = Certificate.builder()
                .certificateNumber(Certificate.generateCertificateNumber())
                .userId(enrollment.getUserId())
                .courseId(enrollment.getCourseId())
                .enrollment(enrollment)
                .userName("User " + enrollment.getUserId()) // Should be fetched from auth service
                .courseTitle("Course " + enrollment.getCourseId()) // Should be fetched from course service
                .issuedAt(LocalDateTime.now())
                .build();

        certificate.setVerificationUrl("/api/v1/certificates/verify/" + certificate.getCertificateNumber());
        certificateRepository.save(certificate);
        log.info("Certificate generated: {}", certificate.getCertificateNumber());
    }

    @Transactional(readOnly = true)
    public boolean isEnrolled(Long userId, Long courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }
}
