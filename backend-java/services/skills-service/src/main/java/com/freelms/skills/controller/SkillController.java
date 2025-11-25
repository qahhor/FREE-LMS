package com.freelms.skills.controller;

import com.freelms.common.dto.ApiResponse;
import com.freelms.common.dto.PagedResponse;
import com.freelms.common.security.UserPrincipal;
import com.freelms.skills.dto.*;
import com.freelms.skills.entity.SkillAssessment;
import com.freelms.skills.entity.SkillGap;
import com.freelms.skills.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<SkillDto>> createSkill(
            @Valid @RequestBody SkillDto request,
            @AuthenticationPrincipal UserPrincipal principal) {

        SkillDto result = skillService.createSkill(request, principal.getOrganizationId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Skill created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<SkillDto>>> getSkills(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PagedResponse<SkillDto> result = skillService.getSkills(
                principal.getOrganizationId(), search, PageRequest.of(page, size, Sort.by("name")));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // User Skills
    @PostMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserSkillDto>> addUserSkill(
            @PathVariable Long userId,
            @RequestParam Long skillId,
            @RequestParam(required = false) Integer level) {

        UserSkillDto result = skillService.addUserSkill(userId, skillId, level);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Skill added to user"));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<UserSkillDto>>> getUserSkills(@PathVariable Long userId) {
        List<UserSkillDto> result = skillService.getUserSkills(userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<UserSkillDto>>> getMySkills(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<UserSkillDto> result = skillService.getUserSkills(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/users/{userId}/assess")
    public ResponseEntity<ApiResponse<UserSkillDto>> assessSkill(
            @PathVariable Long userId,
            @RequestParam Long skillId,
            @RequestParam SkillAssessment.AssessmentType type,
            @RequestParam Integer level,
            @RequestParam(required = false) String feedback,
            @AuthenticationPrincipal UserPrincipal principal) {

        UserSkillDto result = skillService.assessSkill(userId, skillId, type, level, principal.getId(), feedback);
        return ResponseEntity.ok(ApiResponse.success(result, "Skill assessed successfully"));
    }

    @PostMapping("/users/{userId}/endorse")
    public ResponseEntity<ApiResponse<UserSkillDto>> endorseSkill(
            @PathVariable Long userId,
            @RequestParam Long skillId,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false, defaultValue = "peer") String relationship,
            @AuthenticationPrincipal UserPrincipal principal) {

        UserSkillDto result = skillService.endorseSkill(userId, skillId, principal.getId(), level, comment, relationship);
        return ResponseEntity.ok(ApiResponse.success(result, "Skill endorsed successfully"));
    }

    // Skill Gaps
    @GetMapping("/users/{userId}/gaps")
    public ResponseEntity<ApiResponse<List<SkillGapDto>>> getUserSkillGaps(@PathVariable Long userId) {
        List<SkillGapDto> result = skillService.getUserSkillGaps(userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/my/gaps")
    public ResponseEntity<ApiResponse<List<SkillGapDto>>> getMySkillGaps(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<SkillGapDto> result = skillService.getUserSkillGaps(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/users/{userId}/gaps")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<ApiResponse<SkillGapDto>> identifyGap(
            @PathVariable Long userId,
            @RequestParam Long skillId,
            @RequestParam Integer requiredLevel,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) Long sourceId,
            @RequestParam(required = false) SkillGap.GapPriority priority) {

        SkillGapDto result = skillService.identifyGap(userId, skillId, requiredLevel, sourceType, sourceId, priority);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Skill gap identified"));
    }

    // Team Matrix
    @GetMapping("/matrix/team/{teamId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<ApiResponse<SkillMatrixDto>> getTeamSkillMatrix(
            @PathVariable Long teamId,
            @RequestParam List<Long> memberIds,
            @RequestParam List<Long> skillIds) {

        SkillMatrixDto result = skillService.getTeamSkillMatrix(teamId, memberIds, skillIds);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
