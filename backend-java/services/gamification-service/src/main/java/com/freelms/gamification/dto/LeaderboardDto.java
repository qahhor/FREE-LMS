package com.freelms.gamification.dto;

import com.freelms.gamification.entity.Leaderboard;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardDto {
    private Long id;
    private Leaderboard.PeriodType periodType;
    private Leaderboard.LeaderboardType leaderboardType;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private List<LeaderboardEntryDto> entries;
    private Long totalEntries;
    private LeaderboardEntryDto currentUserEntry;
}
