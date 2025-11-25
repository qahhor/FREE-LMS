package com.freelms.social.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "lesson_id")
    private Long lessonId;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags; // JSON array

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "vote_count")
    @Builder.Default
    private Integer voteCount = 0;

    @Column(name = "answer_count")
    @Builder.Default
    private Integer answerCount = 0;

    @Column(name = "is_answered")
    @Builder.Default
    private Boolean isAnswered = false;

    @Column(name = "accepted_answer_id")
    private Long acceptedAnswerId;

    @Column(name = "is_pinned")
    @Builder.Default
    private Boolean isPinned = false;

    @Column(name = "is_closed")
    @Builder.Default
    private Boolean isClosed = false;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Answer> answers = new ArrayList<>();
}
