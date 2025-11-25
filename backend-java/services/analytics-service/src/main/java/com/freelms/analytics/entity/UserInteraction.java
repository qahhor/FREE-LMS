package com.freelms.analytics.entity;

import com.freelms.common.entity.BaseEntity;
import com.freelms.common.enums.InteractionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_interactions", indexes = {
    @Index(name = "idx_interactions_user", columnList = "user_id"),
    @Index(name = "idx_interactions_course", columnList = "course_id"),
    @Index(name = "idx_interactions_type", columnList = "type"),
    @Index(name = "idx_interactions_created", columnList = "created_at")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserInteraction extends BaseEntity {
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "course_id", nullable = false) private Long courseId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private InteractionType type;
    @Column(name = "weight") @Builder.Default private Double weight = 1.0;
    @Column(name = "search_query", length = 255) private String searchQuery;
    @Column(name = "rating") private Integer rating;
}
