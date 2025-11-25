package com.freelms.skills.service;

import com.freelms.common.dto.PagedResponse;
import com.freelms.common.exception.ResourceNotFoundException;
import com.freelms.skills.dto.*;
import com.freelms.skills.entity.*;
import com.freelms.skills.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SkillService {

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillGapRepository skillGapRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public SkillDto createSkill(SkillDto dto, Long organizationId) {
        if (skillRepository.existsByNameAndOrganizationId(dto.getName(), organizationId)) {
            throw new IllegalStateException("Skill already exists: " + dto.getName());
        }

        Skill skill = Skill.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .organizationId(organizationId)
                .isGlobal(false)
                .maxLevel(dto.getMaxLevel() != null ? dto.getMaxLevel() : 5)
                .iconUrl(dto.getIconUrl())
                .isActive(true)
                .build();

        if (dto.getLevelDefinitions() != null) {
            for (SkillLevelDefinitionDto levelDto : dto.getLevelDefinitions()) {
                SkillLevelDefinition level = SkillLevelDefinition.builder()
                        .skill(skill)
                        .level(levelDto.getLevel())
                        .name(levelDto.getName())
                        .description(levelDto.getDescription())
                        .criteria(levelDto.getCriteria())
                        .minAssessmentsRequired(levelDto.getMinAssessmentsRequired())
                        .minPeerEndorsements(levelDto.getMinPeerEndorsements())
                        .build();
                skill.getLevelDefinitions().add(level);
            }
        }

        Skill saved = skillRepository.save(skill);
        log.info("Created skill: {} for organization: {}", saved.getId(), organizationId);
        return mapToDto(saved);
    }

    public PagedResponse<SkillDto> getSkills(Long organizationId, String search, Pageable pageable) {
        Page<Skill> page;
        if (search != null && !search.isBlank()) {
            page = skillRepository.searchSkills(organizationId, search, pageable);
        } else {
            page = skillRepository.findByOrganizationIdAndIsActiveTrue(organizationId, pageable);
        }

        List<SkillDto> content = page.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    // User Skills
    public UserSkillDto addUserSkill(Long userId, Long skillId, Integer level) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + skillId));

        UserSkill userSkill = userSkillRepository.findByUserIdAndSkillId(userId, skillId)
                .orElse(UserSkill.builder()
                        .userId(userId)
                        .skill(skill)
                        .currentLevel(level != null ? level : 1)
                        .build());

        if (level != null) {
            userSkill.setSelfAssessedLevel(level);
            userSkill.setLastAssessmentDate(LocalDateTime.now());
        }

        UserSkill saved = userSkillRepository.save(userSkill);
        log.info("Added skill {} to user {} at level {}", skillId, userId, level);

        kafkaTemplate.send("skill-events", "skill-added",
                new SkillEvent(userId, skillId, level, "added"));

        return mapUserSkillToDto(saved);
    }

    public List<UserSkillDto> getUserSkills(Long userId) {
        return userSkillRepository.findByUserId(userId).stream()
                .map(this::mapUserSkillToDto)
                .collect(Collectors.toList());
    }

    public UserSkillDto assessSkill(Long userId, Long skillId,
                                     SkillAssessment.AssessmentType type,
                                     Integer level, Long assessorId, String feedback) {
        UserSkill userSkill = userSkillRepository.findByUserIdAndSkillId(userId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill not found"));

        SkillAssessment assessment = SkillAssessment.builder()
                .userSkill(userSkill)
                .assessmentType(type)
                .assessorId(assessorId)
                .assessedLevel(level)
                .previousLevel(userSkill.getCurrentLevel())
                .feedback(feedback)
                .assessedAt(LocalDateTime.now())
                .build();

        userSkill.getAssessments().add(assessment);

        // Update levels based on assessment type
        switch (type) {
            case SELF -> userSkill.setSelfAssessedLevel(level);
            case MANAGER -> {
                userSkill.setManagerAssessedLevel(level);
                userSkill.setCurrentLevel(level);
                userSkill.setIsVerified(true);
                userSkill.setVerifiedBy(assessorId);
                userSkill.setVerifiedAt(LocalDateTime.now());
            }
            case PEER -> updatePeerAverageLevel(userSkill);
            case QUIZ, CERTIFICATION -> {
                userSkill.setCurrentLevel(level);
                assessment.setIsApproved(true);
            }
        }

        userSkill.setLastAssessmentDate(LocalDateTime.now());
        UserSkill saved = userSkillRepository.save(userSkill);

        kafkaTemplate.send("skill-events", "skill-assessed",
                new SkillEvent(userId, skillId, level, type.name()));

        return mapUserSkillToDto(saved);
    }

    public UserSkillDto endorseSkill(Long userId, Long skillId, Long endorserId,
                                      Integer level, String comment, String relationship) {
        UserSkill userSkill = userSkillRepository.findByUserIdAndSkillId(userId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill not found"));

        SkillEndorsement endorsement = SkillEndorsement.builder()
                .userSkill(userSkill)
                .endorserId(endorserId)
                .endorsedLevel(level)
                .comment(comment)
                .relationship(relationship)
                .endorsedAt(LocalDateTime.now())
                .build();

        userSkill.getEndorsements().add(endorsement);
        userSkill.setEndorsementCount(userSkill.getEndorsementCount() + 1);
        updatePeerAverageLevel(userSkill);

        UserSkill saved = userSkillRepository.save(userSkill);
        log.info("User {} endorsed {} for skill {} at level {}", endorserId, userId, skillId, level);

        return mapUserSkillToDto(saved);
    }

    private void updatePeerAverageLevel(UserSkill userSkill) {
        double avg = userSkill.getEndorsements().stream()
                .filter(e -> e.getEndorsedLevel() != null)
                .mapToInt(SkillEndorsement::getEndorsedLevel)
                .average()
                .orElse(0);
        userSkill.setPeerAverageLevel(avg);
    }

    // Skill Gaps
    public List<SkillGapDto> getUserSkillGaps(Long userId) {
        return skillGapRepository.findActiveGapsByUser(userId).stream()
                .map(this::mapGapToDto)
                .collect(Collectors.toList());
    }

    public SkillGapDto identifyGap(Long userId, Long skillId, Integer requiredLevel,
                                    String sourceType, Long sourceId, SkillGap.GapPriority priority) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + skillId));

        UserSkill userSkill = userSkillRepository.findByUserIdAndSkillId(userId, skillId)
                .orElse(null);

        int currentLevel = userSkill != null ? userSkill.getCurrentLevel() : 0;
        int gapSize = requiredLevel - currentLevel;

        if (gapSize <= 0) {
            return null; // No gap
        }

        SkillGap gap = SkillGap.builder()
                .userId(userId)
                .skill(skill)
                .currentLevel(currentLevel)
                .requiredLevel(requiredLevel)
                .gapSize(gapSize)
                .priority(priority != null ? priority : calculatePriority(gapSize))
                .sourceType(sourceType)
                .sourceId(sourceId)
                .identifiedAt(LocalDateTime.now())
                .build();

        SkillGap saved = skillGapRepository.save(gap);
        log.info("Identified skill gap for user {}: skill {} gap size {}", userId, skillId, gapSize);

        return mapGapToDto(saved);
    }

    private SkillGap.GapPriority calculatePriority(int gapSize) {
        if (gapSize >= 4) return SkillGap.GapPriority.CRITICAL;
        if (gapSize >= 3) return SkillGap.GapPriority.HIGH;
        if (gapSize >= 2) return SkillGap.GapPriority.MEDIUM;
        return SkillGap.GapPriority.LOW;
    }

    // Team Skill Matrix
    public SkillMatrixDto getTeamSkillMatrix(Long teamId, List<Long> memberIds, List<Long> skillIds) {
        List<UserSkill> allSkills = new ArrayList<>();
        for (Long memberId : memberIds) {
            allSkills.addAll(userSkillRepository.findByUserId(memberId));
        }

        Map<Long, Map<Long, Integer>> memberSkillLevels = new HashMap<>();
        for (UserSkill us : allSkills) {
            memberSkillLevels
                    .computeIfAbsent(us.getUserId(), k -> new HashMap<>())
                    .put(us.getSkill().getId(), us.getCurrentLevel());
        }

        List<Skill> skills = skillRepository.findAllById(skillIds);

        // Calculate coverage
        Map<Long, SkillCoverageDto> coverage = new HashMap<>();
        for (Skill skill : skills) {
            long membersWithSkill = memberSkillLevels.values().stream()
                    .filter(m -> m.containsKey(skill.getId()))
                    .count();

            double avgLevel = memberSkillLevels.values().stream()
                    .filter(m -> m.containsKey(skill.getId()))
                    .mapToInt(m -> m.get(skill.getId()))
                    .average()
                    .orElse(0);

            coverage.put(skill.getId(), SkillCoverageDto.builder()
                    .skillId(skill.getId())
                    .skillName(skill.getName())
                    .membersWithSkill((int) membersWithSkill)
                    .averageLevel(avgLevel)
                    .hasCriticalGap(membersWithSkill == 0)
                    .build());
        }

        return SkillMatrixDto.builder()
                .teamId(teamId)
                .skills(skills.stream().map(this::mapToDto).collect(Collectors.toList()))
                .skillCoverage(coverage)
                .build();
    }

    // Mappers
    private SkillDto mapToDto(Skill skill) {
        return SkillDto.builder()
                .id(skill.getId())
                .name(skill.getName())
                .description(skill.getDescription())
                .categoryId(skill.getCategory() != null ? skill.getCategory().getId() : null)
                .categoryName(skill.getCategory() != null ? skill.getCategory().getName() : null)
                .organizationId(skill.getOrganizationId())
                .isGlobal(skill.getIsGlobal())
                .maxLevel(skill.getMaxLevel())
                .iconUrl(skill.getIconUrl())
                .isActive(skill.getIsActive())
                .levelDefinitions(skill.getLevelDefinitions().stream()
                        .map(this::mapLevelToDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private SkillLevelDefinitionDto mapLevelToDto(SkillLevelDefinition level) {
        return SkillLevelDefinitionDto.builder()
                .id(level.getId())
                .level(level.getLevel())
                .name(level.getName())
                .description(level.getDescription())
                .criteria(level.getCriteria())
                .minAssessmentsRequired(level.getMinAssessmentsRequired())
                .minPeerEndorsements(level.getMinPeerEndorsements())
                .build();
    }

    private UserSkillDto mapUserSkillToDto(UserSkill userSkill) {
        return UserSkillDto.builder()
                .id(userSkill.getId())
                .userId(userSkill.getUserId())
                .skillId(userSkill.getSkill().getId())
                .skillName(userSkill.getSkill().getName())
                .categoryName(userSkill.getSkill().getCategory() != null ?
                        userSkill.getSkill().getCategory().getName() : null)
                .currentLevel(userSkill.getCurrentLevel())
                .targetLevel(userSkill.getTargetLevel())
                .selfAssessedLevel(userSkill.getSelfAssessedLevel())
                .managerAssessedLevel(userSkill.getManagerAssessedLevel())
                .peerAverageLevel(userSkill.getPeerAverageLevel())
                .lastAssessmentDate(userSkill.getLastAssessmentDate())
                .nextAssessmentDue(userSkill.getNextAssessmentDue())
                .isVerified(userSkill.getIsVerified())
                .endorsementCount(userSkill.getEndorsementCount())
                .build();
    }

    private SkillGapDto mapGapToDto(SkillGap gap) {
        return SkillGapDto.builder()
                .id(gap.getId())
                .userId(gap.getUserId())
                .skillId(gap.getSkill().getId())
                .skillName(gap.getSkill().getName())
                .currentLevel(gap.getCurrentLevel())
                .requiredLevel(gap.getRequiredLevel())
                .gapSize(gap.getGapSize())
                .priority(gap.getPriority())
                .sourceType(gap.getSourceType())
                .sourceId(gap.getSourceId())
                .targetDate(gap.getTargetDate())
                .identifiedAt(gap.getIdentifiedAt())
                .build();
    }

    public record SkillEvent(Long userId, Long skillId, Integer level, String action) {}
}
