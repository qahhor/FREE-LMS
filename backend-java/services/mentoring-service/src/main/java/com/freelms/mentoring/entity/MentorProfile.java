package com.freelms.mentoring.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mentor_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorProfile extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "expertise_areas", columnDefinition = "TEXT")
    private String expertiseAreas; // JSON array

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills; // JSON array of skill IDs

    @Column(name = "availability_hours_per_week")
    @Builder.Default
    private Integer availabilityHoursPerWeek = 2;

    @Column(name = "max_mentees")
    @Builder.Default
    private Integer maxMentees = 3;

    @Column(name = "current_mentees_count")
    @Builder.Default
    private Integer currentMenteesCount = 0;

    @Column(name = "is_accepting_mentees")
    @Builder.Default
    private Boolean isAcceptingMentees = true;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "total_sessions")
    @Builder.Default
    private Integer totalSessions = 0;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "total_ratings")
    @Builder.Default
    private Integer totalRatings = 0;

    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL)
    @Builder.Default
    private List<MentoringRelationship> relationships = new ArrayList<>();
}
