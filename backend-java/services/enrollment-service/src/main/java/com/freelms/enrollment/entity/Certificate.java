package com.freelms.enrollment.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificates", indexes = {
        @Index(name = "idx_certificates_user", columnList = "user_id"),
        @Index(name = "idx_certificates_course", columnList = "course_id"),
        @Index(name = "idx_certificates_number", columnList = "certificate_number", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Certificate extends BaseEntity {

    @Column(name = "certificate_number", nullable = false, unique = true, length = 50)
    private String certificateNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(name = "user_name", nullable = false, length = 255)
    private String userName;

    @Column(name = "course_title", nullable = false, length = 255)
    private String courseTitle;

    @Column(name = "issued_at", nullable = false)
    @Builder.Default
    private LocalDateTime issuedAt = LocalDateTime.now();

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "verification_url", length = 500)
    private String verificationUrl;

    public static String generateCertificateNumber() {
        return "CERT-" + System.currentTimeMillis() + "-" +
               String.format("%04d", (int) (Math.random() * 10000));
    }
}
