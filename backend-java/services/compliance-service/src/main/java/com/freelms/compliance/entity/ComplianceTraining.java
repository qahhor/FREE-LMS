package com.freelms.compliance.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "compliance_trainings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceTraining extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "is_mandatory")
    @Builder.Default
    private Boolean isMandatory = true;

    @Column(name = "recurrence_months")
    private Integer recurrenceMonths; // null = one-time

    @Column(name = "grace_period_days")
    @Builder.Default
    private Integer gracePeriodDays = 30;

    @Column(name = "target_roles", columnDefinition = "TEXT")
    private String targetRoles; // JSON array

    @Column(name = "target_departments", columnDefinition = "TEXT")
    private String targetDepartments;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "escalation_levels", columnDefinition = "TEXT")
    private String escalationLevels; // JSON: [{days, action, notifyRoles}]
}
