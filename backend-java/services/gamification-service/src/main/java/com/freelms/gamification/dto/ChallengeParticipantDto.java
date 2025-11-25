package com.freelms.gamification.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeParticipantDto {
    private Long userId;
    private String userName;
    private String avatarUrl;
    private String teamName;
    private Integer currentProgress;
    private Integer targetProgress;
    private Integer percentComplete;
    private LocalDateTime joinedAt;
    private LocalDateTime completedAt;
    private Integer rank;
    private Boolean isWinner;
}
