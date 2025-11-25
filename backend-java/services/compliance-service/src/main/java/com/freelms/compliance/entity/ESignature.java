package com.freelms.compliance.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "e_signatures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ESignature extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "document_name", nullable = false)
    private String documentName;

    @Column(name = "document_version")
    private String documentVersion;

    @Column(name = "signature_hash", nullable = false)
    private String signatureHash;

    @Column(name = "signed_at", nullable = false)
    @Builder.Default
    private LocalDateTime signedAt = LocalDateTime.now();

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String statement; // "I have read and understood..."

    @Column(name = "is_valid")
    @Builder.Default
    private Boolean isValid = true;

    @Column(name = "invalidated_at")
    private LocalDateTime invalidatedAt;

    @Column(name = "invalidation_reason")
    private String invalidationReason;
}
