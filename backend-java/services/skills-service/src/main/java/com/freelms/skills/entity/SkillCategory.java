package com.freelms.skills.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "skill_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillCategory extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "color")
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private SkillCategory parent;

    @OneToMany(mappedBy = "parent")
    @Builder.Default
    private List<SkillCategory> children = new ArrayList<>();

    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<Skill> skills = new ArrayList<>();

    @Column(name = "order_index")
    @Builder.Default
    private Integer orderIndex = 0;
}
