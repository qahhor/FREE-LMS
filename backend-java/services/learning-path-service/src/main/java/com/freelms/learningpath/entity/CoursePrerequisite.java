package com.freelms.learningpath.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_prerequisites",
       uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "prerequisite_course_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursePrerequisite extends BaseEntity {

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "prerequisite_course_id", nullable = false)
    private Long prerequisiteCourseId;

    @Column(name = "is_mandatory")
    @Builder.Default
    private Boolean isMandatory = true;

    @Column(name = "min_completion_percentage")
    @Builder.Default
    private Integer minCompletionPercentage = 100;

    @Column(name = "min_quiz_score")
    private Integer minQuizScore;

    @Column(name = "description")
    private String description;
}
