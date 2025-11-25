package com.freelms.learningpath.service;

import com.freelms.common.dto.PagedResponse;
import com.freelms.common.enums.CourseStatus;
import com.freelms.common.exception.ResourceNotFoundException;
import com.freelms.learningpath.dto.*;
import com.freelms.learningpath.entity.*;
import com.freelms.learningpath.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LearningPathService {

    private final LearningPathRepository learningPathRepository;
    private final LearningPathEnrollmentRepository enrollmentRepository;
    private final CoursePrerequisiteRepository prerequisiteRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public LearningPathDto createLearningPath(CreateLearningPathRequest request, Long organizationId, Long userId) {
        LearningPath learningPath = LearningPath.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .organizationId(organizationId)
                .createdBy(userId)
                .status(CourseStatus.DRAFT)
                .estimatedDurationHours(request.getEstimatedDurationHours())
                .difficultyLevel(request.getDifficultyLevel())
                .isMandatory(request.getIsMandatory() != null ? request.getIsMandatory() : false)
                .targetRoles(request.getTargetRoles() != null ? String.join(",", request.getTargetRoles()) : null)
                .targetDepartments(request.getTargetDepartments() != null ? String.join(",", request.getTargetDepartments()) : null)
                .pointsReward(request.getPointsReward() != null ? request.getPointsReward() : 0)
                .badgeId(request.getBadgeId())
                .completionCertificateTemplateId(request.getCompletionCertificateTemplateId())
                .build();

        if (request.getItems() != null) {
            for (CreateLearningPathItemRequest itemRequest : request.getItems()) {
                LearningPathItem item = createItemFromRequest(itemRequest, learningPath);
                learningPath.getItems().add(item);
            }
        }

        LearningPath saved = learningPathRepository.save(learningPath);
        log.info("Created learning path: {} by user: {}", saved.getId(), userId);

        return mapToDto(saved);
    }

    public LearningPathDto getLearningPath(Long id) {
        LearningPath learningPath = learningPathRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found: " + id));
        return mapToDto(learningPath);
    }

    public PagedResponse<LearningPathDto> getLearningPaths(Long organizationId, CourseStatus status, String search, Pageable pageable) {
        Page<LearningPath> page;
        if (search != null && !search.isBlank()) {
            page = learningPathRepository.searchByOrganization(organizationId, status, search, pageable);
        } else if (status != null) {
            page = learningPathRepository.findByOrganizationIdAndStatus(organizationId, status, pageable);
        } else {
            page = learningPathRepository.findByOrganizationId(organizationId, pageable);
        }

        List<LearningPathDto> content = page.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    public LearningPathDto publishLearningPath(Long id, Long userId) {
        LearningPath learningPath = learningPathRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found: " + id));

        learningPath.setStatus(CourseStatus.PUBLISHED);
        LearningPath saved = learningPathRepository.save(learningPath);

        kafkaTemplate.send("learning-path-events", "path-published", saved.getId());
        log.info("Published learning path: {} by user: {}", id, userId);

        return mapToDto(saved);
    }

    public EnrollmentDto enrollUser(Long pathId, Long userId, Long assignedBy, String note, LocalDateTime deadline) {
        if (enrollmentRepository.existsByLearningPathIdAndUserId(pathId, userId)) {
            throw new IllegalStateException("User already enrolled in this learning path");
        }

        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found: " + pathId));

        LearningPathEnrollment enrollment = LearningPathEnrollment.builder()
                .learningPath(learningPath)
                .userId(userId)
                .status(LearningPathEnrollment.EnrollmentStatus.ACTIVE)
                .enrolledAt(LocalDateTime.now())
                .deadline(deadline)
                .assignedBy(assignedBy)
                .assignmentNote(note)
                .progressPercentage(0)
                .currentItemIndex(0)
                .build();

        // Initialize item progress
        for (LearningPathItem item : learningPath.getItems()) {
            LearningPathItemProgress progress = LearningPathItemProgress.builder()
                    .enrollment(enrollment)
                    .item(item)
                    .status(item.getOrderIndex() == 0 ?
                            LearningPathItemProgress.ProgressStatus.AVAILABLE :
                            LearningPathItemProgress.ProgressStatus.LOCKED)
                    .build();
            enrollment.getItemProgress().add(progress);
        }

        LearningPathEnrollment saved = enrollmentRepository.save(enrollment);

        kafkaTemplate.send("learning-path-events", "user-enrolled",
                new EnrollmentEvent(pathId, userId, assignedBy));
        log.info("User {} enrolled in learning path {} by {}", userId, pathId, assignedBy);

        return mapEnrollmentToDto(saved);
    }

    public EnrollmentDto updateProgress(Long enrollmentId, Long itemId, LearningPathItemProgress.ProgressStatus status, Integer score) {
        LearningPathEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + enrollmentId));

        enrollment.getItemProgress().stream()
                .filter(p -> p.getItem().getId().equals(itemId))
                .findFirst()
                .ifPresent(progress -> {
                    progress.setStatus(status);
                    if (score != null) progress.setScore(score);
                    if (status == LearningPathItemProgress.ProgressStatus.IN_PROGRESS && progress.getStartedAt() == null) {
                        progress.setStartedAt(LocalDateTime.now());
                    }
                    if (status == LearningPathItemProgress.ProgressStatus.COMPLETED) {
                        progress.setCompletedAt(LocalDateTime.now());
                        unlockNextItems(enrollment, progress.getItem());
                    }
                });

        updateEnrollmentProgress(enrollment);
        LearningPathEnrollment saved = enrollmentRepository.save(enrollment);

        return mapEnrollmentToDto(saved);
    }

    private void unlockNextItems(LearningPathEnrollment enrollment, LearningPathItem completedItem) {
        enrollment.getItemProgress().stream()
                .filter(p -> p.getStatus() == LearningPathItemProgress.ProgressStatus.LOCKED)
                .filter(p -> p.getItem().getPrerequisites().contains(completedItem))
                .forEach(p -> {
                    boolean allPrereqsMet = p.getItem().getPrerequisites().stream()
                            .allMatch(prereq -> enrollment.getItemProgress().stream()
                                    .anyMatch(ip -> ip.getItem().equals(prereq) &&
                                            ip.getStatus() == LearningPathItemProgress.ProgressStatus.COMPLETED));
                    if (allPrereqsMet) {
                        p.setStatus(LearningPathItemProgress.ProgressStatus.AVAILABLE);
                        p.setUnlockDate(LocalDateTime.now());
                    }
                });
    }

    private void updateEnrollmentProgress(LearningPathEnrollment enrollment) {
        long completed = enrollment.getItemProgress().stream()
                .filter(p -> p.getStatus() == LearningPathItemProgress.ProgressStatus.COMPLETED)
                .count();
        long total = enrollment.getItemProgress().size();

        int percentage = total > 0 ? (int) ((completed * 100) / total) : 0;
        enrollment.setProgressPercentage(percentage);

        if (percentage == 100) {
            enrollment.setStatus(LearningPathEnrollment.EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(LocalDateTime.now());

            kafkaTemplate.send("learning-path-events", "path-completed",
                    new CompletionEvent(enrollment.getLearningPath().getId(), enrollment.getUserId(),
                            enrollment.getLearningPath().getPointsReward()));
        }
    }

    // Course Prerequisites
    public void addCoursePrerequisite(Long courseId, Long prerequisiteCourseId, boolean mandatory, int minCompletion) {
        if (prerequisiteRepository.existsByCourseIdAndPrerequisiteCourseId(courseId, prerequisiteCourseId)) {
            throw new IllegalStateException("Prerequisite already exists");
        }

        CoursePrerequisite prerequisite = CoursePrerequisite.builder()
                .courseId(courseId)
                .prerequisiteCourseId(prerequisiteCourseId)
                .isMandatory(mandatory)
                .minCompletionPercentage(minCompletion)
                .build();

        prerequisiteRepository.save(prerequisite);
        log.info("Added prerequisite: course {} requires {}", courseId, prerequisiteCourseId);
    }

    public List<CoursePrerequisite> getCoursePrerequisites(Long courseId) {
        return prerequisiteRepository.findByCourseId(courseId);
    }

    private LearningPathItem createItemFromRequest(CreateLearningPathItemRequest request, LearningPath learningPath) {
        return LearningPathItem.builder()
                .learningPath(learningPath)
                .itemType(request.getItemType())
                .courseId(request.getCourseId())
                .quizId(request.getQuizId())
                .externalResourceUrl(request.getExternalResourceUrl())
                .externalResourceTitle(request.getExternalResourceTitle())
                .orderIndex(request.getOrderIndex())
                .isOptional(request.getIsOptional() != null ? request.getIsOptional() : false)
                .unlockAfterDays(request.getUnlockAfterDays())
                .deadlineDays(request.getDeadlineDays())
                .minScoreToPass(request.getMinScoreToPass())
                .pointsReward(request.getPointsReward() != null ? request.getPointsReward() : 0)
                .build();
    }

    private LearningPathDto mapToDto(LearningPath entity) {
        return LearningPathDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .thumbnailUrl(entity.getThumbnailUrl())
                .organizationId(entity.getOrganizationId())
                .createdBy(entity.getCreatedBy())
                .status(entity.getStatus())
                .estimatedDurationHours(entity.getEstimatedDurationHours())
                .difficultyLevel(entity.getDifficultyLevel())
                .isMandatory(entity.getIsMandatory())
                .pointsReward(entity.getPointsReward())
                .badgeId(entity.getBadgeId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .items(entity.getItems().stream().map(this::mapItemToDto).collect(Collectors.toList()))
                .enrollmentCount((long) entity.getEnrollments().size())
                .completionCount(learningPathRepository.countCompletions(entity.getId()))
                .averageProgress(learningPathRepository.getAverageProgress(entity.getId()))
                .build();
    }

    private LearningPathItemDto mapItemToDto(LearningPathItem item) {
        return LearningPathItemDto.builder()
                .id(item.getId())
                .itemType(item.getItemType())
                .courseId(item.getCourseId())
                .quizId(item.getQuizId())
                .externalResourceUrl(item.getExternalResourceUrl())
                .externalResourceTitle(item.getExternalResourceTitle())
                .orderIndex(item.getOrderIndex())
                .isOptional(item.getIsOptional())
                .unlockAfterDays(item.getUnlockAfterDays())
                .deadlineDays(item.getDeadlineDays())
                .prerequisiteIds(item.getPrerequisites().stream().map(LearningPathItem::getId).collect(Collectors.toList()))
                .minScoreToPass(item.getMinScoreToPass())
                .pointsReward(item.getPointsReward())
                .build();
    }

    private EnrollmentDto mapEnrollmentToDto(LearningPathEnrollment enrollment) {
        return EnrollmentDto.builder()
                .id(enrollment.getId())
                .learningPathId(enrollment.getLearningPath().getId())
                .learningPathTitle(enrollment.getLearningPath().getTitle())
                .userId(enrollment.getUserId())
                .status(enrollment.getStatus())
                .enrolledAt(enrollment.getEnrolledAt())
                .startedAt(enrollment.getStartedAt())
                .completedAt(enrollment.getCompletedAt())
                .deadline(enrollment.getDeadline())
                .progressPercentage(enrollment.getProgressPercentage())
                .currentItemIndex(enrollment.getCurrentItemIndex())
                .assignedBy(enrollment.getAssignedBy())
                .assignmentNote(enrollment.getAssignmentNote())
                .itemProgress(enrollment.getItemProgress().stream()
                        .map(this::mapProgressToDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private ItemProgressDto mapProgressToDto(LearningPathItemProgress progress) {
        return ItemProgressDto.builder()
                .id(progress.getId())
                .itemId(progress.getItem().getId())
                .status(progress.getStatus())
                .startedAt(progress.getStartedAt())
                .completedAt(progress.getCompletedAt())
                .unlockDate(progress.getUnlockDate())
                .deadline(progress.getDeadline())
                .score(progress.getScore())
                .attempts(progress.getAttempts())
                .timeSpentMinutes(progress.getTimeSpentMinutes())
                .build();
    }

    // Event classes for Kafka
    public record EnrollmentEvent(Long pathId, Long userId, Long assignedBy) {}
    public record CompletionEvent(Long pathId, Long userId, Integer pointsEarned) {}
}
