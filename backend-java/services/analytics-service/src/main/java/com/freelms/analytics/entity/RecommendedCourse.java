package com.freelms.analytics.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommended_courses", indexes = {
    @Index(name = "idx_recommendations_user", columnList = "user_id"),
    @Index(name = "idx_recommendations_course", columnList = "course_id")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RecommendedCourse extends BaseEntity {
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "course_id", nullable = false) private Long courseId;
    @Column(nullable = false) private Double score;
    @Column(name = "algorithm_type", length = 50) private String algorithmType;
    @Column(name = "valid_until") private LocalDateTime validUntil;
    @Column(name = "is_dismissed") @Builder.Default private boolean isDismissed = false;
}
