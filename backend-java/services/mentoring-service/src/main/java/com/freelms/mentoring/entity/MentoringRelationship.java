package com.freelms.mentoring.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mentoring_relationships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentoringRelationship extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private MentorProfile mentor;

    @Column(name = "mentee_id", nullable = false)
    private Long menteeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RelationshipStatus status = RelationshipStatus.PENDING;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "target_end_date")
    private LocalDate targetEndDate;

    @Column(columnDefinition = "TEXT")
    private String goals;

    @Column(name = "focus_areas", columnDefinition = "TEXT")
    private String focusAreas;

    @Column(name = "meeting_frequency")
    private String meetingFrequency; // "weekly", "biweekly", "monthly"

    @OneToMany(mappedBy = "relationship", cascade = CascadeType.ALL)
    @Builder.Default
    private List<MentoringSession> sessions = new ArrayList<>();

    @Column(name = "mentor_rating")
    private Integer mentorRating;

    @Column(name = "mentee_rating")
    private Integer menteeRating;

    @Column(name = "mentor_feedback", columnDefinition = "TEXT")
    private String mentorFeedback;

    @Column(name = "mentee_feedback", columnDefinition = "TEXT")
    private String menteeFeedback;

    public enum RelationshipStatus {
        PENDING,
        ACTIVE,
        PAUSED,
        COMPLETED,
        CANCELLED
    }
}
