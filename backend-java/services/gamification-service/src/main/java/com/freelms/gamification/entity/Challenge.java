package com.freelms.gamification.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "challenges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Challenge extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "challenge_type", nullable = false)
    private ChallengeType challengeType;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "target_value")
    private Integer targetValue; // e.g., complete 10 courses

    @Column(name = "target_type")
    private String targetType; // "courses", "lessons", "quizzes", "points", "time"

    @Column(name = "points_reward")
    @Builder.Default
    private Integer pointsReward = 0;

    @Column(name = "coins_reward")
    @Builder.Default
    private Integer coinsReward = 0;

    @Column(name = "badge_id")
    private Long badgeId;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "is_team_challenge")
    @Builder.Default
    private Boolean isTeamChallenge = false;

    @Column(name = "min_team_size")
    private Integer minTeamSize;

    @Column(name = "max_team_size")
    private Integer maxTeamSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ChallengeStatus status = ChallengeStatus.DRAFT;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ChallengeParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ChallengeTeam> teams = new ArrayList<>();

    @Column(name = "created_by")
    private Long createdBy;

    public enum ChallengeType {
        INDIVIDUAL,
        TEAM,
        DEPARTMENT,
        ORGANIZATION
    }

    public enum ChallengeStatus {
        DRAFT,
        UPCOMING,
        ACTIVE,
        COMPLETED,
        CANCELLED
    }
}
