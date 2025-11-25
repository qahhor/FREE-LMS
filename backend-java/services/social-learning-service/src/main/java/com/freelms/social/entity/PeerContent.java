package com.freelms.social.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "peer_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeerContent extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "skill_id")
    private Long skillId;

    @Column(name = "attachment_urls", columnDefinition = "TEXT")
    private String attachmentUrls; // JSON array

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    public enum ContentType {
        TUTORIAL,
        TIP,
        RESOURCE,
        CHEAT_SHEET,
        CASE_STUDY
    }

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
