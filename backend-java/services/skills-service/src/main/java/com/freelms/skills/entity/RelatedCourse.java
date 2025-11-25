package com.freelms.skills.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "related_courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatedCourse extends BaseEntity {

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "skill_level_gained")
    private Integer skillLevelGained;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;
}
