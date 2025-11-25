package com.freelms.idp.service;

import com.freelms.common.exception.ResourceNotFoundException;
import com.freelms.idp.dto.*;
import com.freelms.idp.entity.*;
import com.freelms.idp.repository.IdpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IdpService {

    private final IdpRepository idpRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public IdpDto createPlan(CreateIdpRequest request, Long userId, Long organizationId) {
        IndividualDevelopmentPlan plan = IndividualDevelopmentPlan.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(IndividualDevelopmentPlan.PlanStatus.DRAFT)
                .startDate(request.getStartDate())
                .targetDate(request.getTargetDate())
                .organizationId(organizationId)
                .createdBy(userId)
                .managerId(request.getManagerId())
                .mentorId(request.getMentorId())
                .careerGoal(request.getCareerGoal())
                .currentRole(request.getCurrentRole())
                .targetRole(request.getTargetRole())
                .progressPercentage(0)
                .build();

        if (request.getGoals() != null) {
            int index = 0;
            for (CreateGoalRequest goalRequest : request.getGoals()) {
                DevelopmentGoal goal = createGoalFromRequest(goalRequest, plan, index++);
                plan.getGoals().add(goal);
            }
        }

        IndividualDevelopmentPlan saved = idpRepository.save(plan);
        log.info("Created IDP {} for user {}", saved.getId(), userId);

        return mapToDto(saved);
    }

    public IdpDto getPlan(Long planId) {
        IndividualDevelopmentPlan plan = idpRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("IDP not found: " + planId));
        return mapToDto(plan);
    }

    public List<IdpDto> getUserPlans(Long userId) {
        return idpRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public IdpDto submitForApproval(Long planId) {
        IndividualDevelopmentPlan plan = idpRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("IDP not found: " + planId));

        plan.setStatus(IndividualDevelopmentPlan.PlanStatus.PENDING_APPROVAL);
        IndividualDevelopmentPlan saved = idpRepository.save(plan);

        kafkaTemplate.send("idp-events", "plan-submitted",
                new IdpEvent(planId, plan.getUserId(), plan.getManagerId()));

        return mapToDto(saved);
    }

    public IdpDto approvePlan(Long planId, Long approverId) {
        IndividualDevelopmentPlan plan = idpRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("IDP not found: " + planId));

        plan.setStatus(IndividualDevelopmentPlan.PlanStatus.APPROVED);
        IndividualDevelopmentPlan saved = idpRepository.save(plan);

        kafkaTemplate.send("idp-events", "plan-approved",
                new IdpEvent(planId, plan.getUserId(), approverId));

        return mapToDto(saved);
    }

    public IdpDto activatePlan(Long planId) {
        IndividualDevelopmentPlan plan = idpRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("IDP not found: " + planId));

        plan.setStatus(IndividualDevelopmentPlan.PlanStatus.ACTIVE);
        if (plan.getStartDate() == null) {
            plan.setStartDate(LocalDate.now());
        }
        IndividualDevelopmentPlan saved = idpRepository.save(plan);

        return mapToDto(saved);
    }

    public IdpDto updateGoalProgress(Long planId, Long goalId, int progress) {
        IndividualDevelopmentPlan plan = idpRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("IDP not found: " + planId));

        plan.getGoals().stream()
                .filter(g -> g.getId().equals(goalId))
                .findFirst()
                .ifPresent(goal -> {
                    goal.setProgressPercentage(progress);
                    if (progress >= 100) {
                        goal.setStatus(DevelopmentGoal.GoalStatus.COMPLETED);
                        goal.setCompletedDate(LocalDate.now());
                    } else if (progress > 0) {
                        goal.setStatus(DevelopmentGoal.GoalStatus.IN_PROGRESS);
                    }
                });

        updatePlanProgress(plan);
        IndividualDevelopmentPlan saved = idpRepository.save(plan);

        return mapToDto(saved);
    }

    public IdpDto addReview(Long planId, Long reviewerId, CreateReviewRequest request) {
        IndividualDevelopmentPlan plan = idpRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("IDP not found: " + planId));

        PlanReview review = PlanReview.builder()
                .plan(plan)
                .reviewerId(reviewerId)
                .reviewDate(LocalDate.now())
                .reviewType(request.getReviewType())
                .overallProgress(request.getOverallProgress())
                .feedback(request.getFeedback())
                .recommendations(request.getRecommendations())
                .nextReviewDate(request.getNextReviewDate())
                .isOnTrack(request.getIsOnTrack())
                .build();

        plan.getReviews().add(review);
        IndividualDevelopmentPlan saved = idpRepository.save(plan);

        return mapToDto(saved);
    }

    private void updatePlanProgress(IndividualDevelopmentPlan plan) {
        if (plan.getGoals().isEmpty()) {
            plan.setProgressPercentage(0);
            return;
        }

        int totalProgress = plan.getGoals().stream()
                .mapToInt(DevelopmentGoal::getProgressPercentage)
                .sum();
        plan.setProgressPercentage(totalProgress / plan.getGoals().size());

        if (plan.getProgressPercentage() >= 100) {
            plan.setStatus(IndividualDevelopmentPlan.PlanStatus.COMPLETED);
        }
    }

    private DevelopmentGoal createGoalFromRequest(CreateGoalRequest request, IndividualDevelopmentPlan plan, int index) {
        return DevelopmentGoal.builder()
                .plan(plan)
                .title(request.getTitle())
                .description(request.getDescription())
                .goalType(request.getGoalType())
                .status(DevelopmentGoal.GoalStatus.NOT_STARTED)
                .orderIndex(index)
                .targetDate(request.getTargetDate())
                .successCriteria(request.getSuccessCriteria())
                .resourcesNeeded(request.getResourcesNeeded())
                .skillId(request.getSkillId())
                .learningPathId(request.getLearningPathId())
                .courseId(request.getCourseId())
                .build();
    }

    private IdpDto mapToDto(IndividualDevelopmentPlan plan) {
        return IdpDto.builder()
                .id(plan.getId())
                .userId(plan.getUserId())
                .title(plan.getTitle())
                .description(plan.getDescription())
                .status(plan.getStatus())
                .startDate(plan.getStartDate())
                .targetDate(plan.getTargetDate())
                .managerId(plan.getManagerId())
                .mentorId(plan.getMentorId())
                .careerGoal(plan.getCareerGoal())
                .currentRole(plan.getCurrentRole())
                .targetRole(plan.getTargetRole())
                .progressPercentage(plan.getProgressPercentage())
                .goals(plan.getGoals().stream().map(this::mapGoalToDto).collect(Collectors.toList()))
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    private GoalDto mapGoalToDto(DevelopmentGoal goal) {
        return GoalDto.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .goalType(goal.getGoalType())
                .status(goal.getStatus())
                .targetDate(goal.getTargetDate())
                .completedDate(goal.getCompletedDate())
                .progressPercentage(goal.getProgressPercentage())
                .successCriteria(goal.getSuccessCriteria())
                .skillId(goal.getSkillId())
                .learningPathId(goal.getLearningPathId())
                .courseId(goal.getCourseId())
                .build();
    }

    public record IdpEvent(Long planId, Long userId, Long managerId) {}
}
