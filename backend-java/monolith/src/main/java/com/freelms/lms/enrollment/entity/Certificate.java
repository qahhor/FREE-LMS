package com.freelms.lms.enrollment.entity;

import com.freelms.lms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificates", indexes = {
        @Index(name = "idx_certificates_enrollment", columnList = "enrollment_id", unique = true),
        @Index(name = "idx_certificates_number", columnList = "certificate_number", unique = true),
        @Index(name = "idx_certificates_user", columnList = "user_id")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Certificate extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "certificate_number", nullable = false, unique = true, length = 50)
    private String certificateNumber;

    @Column(name = "course_title", nullable = false)
    private String courseTitle;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "issued_at", nullable = false)
    @Builder.Default
    private LocalDateTime issuedAt = LocalDateTime.now();

    @Column(name = "expiry_at")
    private LocalDateTime expiryAt;

    @Column(name = "verification_url", length = 500)
    private String verificationUrl;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @PrePersist
    public void generateCertificateNumber() {
        if (this.certificateNumber == null) {
            this.certificateNumber = "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    public boolean isExpired() {
        return expiryAt != null && expiryAt.isBefore(LocalDateTime.now());
    }
}
