package com.freelms.compliance.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "certifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certification extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(name = "issuing_authority")
    private String issuingAuthority;

    @Column(name = "credential_id")
    private String credentialId;

    @Column(name = "credential_url")
    private String credentialUrl;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verification_date")
    private LocalDate verificationDate;

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "skill_ids", columnDefinition = "TEXT")
    private String skillIds; // JSON array

    @Column(name = "reminder_days_before")
    @Builder.Default
    private Integer reminderDaysBefore = 30;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CertificationStatus status = CertificationStatus.ACTIVE;

    public enum CertificationStatus {
        ACTIVE,
        EXPIRING_SOON,
        EXPIRED,
        REVOKED
    }
}
