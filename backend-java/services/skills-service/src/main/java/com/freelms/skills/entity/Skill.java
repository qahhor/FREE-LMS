package com.freelms.skills.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private SkillCategory category;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "is_global")
    @Builder.Default
    private Boolean isGlobal = false;

    @Column(name = "max_level")
    @Builder.Default
    private Integer maxLevel = 5;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("level ASC")
    @Builder.Default
    private List<SkillLevelDefinition> levelDefinitions = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "skill_related_courses",
        joinColumns = @JoinColumn(name = "skill_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @Builder.Default
    private List<RelatedCourse> relatedCourses = new ArrayList<>();
}
