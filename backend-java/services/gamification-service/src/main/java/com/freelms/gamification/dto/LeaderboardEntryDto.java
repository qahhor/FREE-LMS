package com.freelms.gamification.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntryDto {
    private Long userId;
    private String userName;
    private String avatarUrl;
    private String department;
    private Long score;
    private Integer rank;
    private Integer previousRank;
    private Integer rankChange;
    private Boolean isTopPerformer;
}
