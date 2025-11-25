package com.freelms.gamification.dto;

import com.freelms.gamification.entity.Challenge;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeDto {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private Challenge.ChallengeType challengeType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer targetValue;
    private String targetType;
    private Integer pointsReward;
    private Integer coinsReward;
    private Challenge.ChallengeStatus status;
    private Boolean isTeamChallenge;
    private Integer participantCount;
    private Integer currentProgress;
    private Boolean isJoined;
    private List<ChallengeParticipantDto> topParticipants;
}
