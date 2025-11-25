package com.freelms.gamification.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "challenge_teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeTeam extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(nullable = false)
    private String name;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "captain_user_id")
    private Long captainUserId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "total_progress")
    @Builder.Default
    private Integer totalProgress = 0;

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "is_winner")
    @Builder.Default
    private Boolean isWinner = false;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ChallengeParticipant> members = new ArrayList<>();
}
