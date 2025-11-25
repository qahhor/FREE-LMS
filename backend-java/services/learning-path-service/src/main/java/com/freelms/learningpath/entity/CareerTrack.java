package com.freelms.learningpath.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "career_tracks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareerTrack extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "careerTrack", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("levelOrder ASC")
    @Builder.Default
    private List<CareerLevel> levels = new ArrayList<>();

    @Column(name = "created_by")
    private Long createdBy;
}
