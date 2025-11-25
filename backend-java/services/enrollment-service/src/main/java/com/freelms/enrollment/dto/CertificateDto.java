package com.freelms.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDto {

    private Long id;
    private String certificateNumber;
    private Long userId;
    private Long courseId;
    private String userName;
    private String courseTitle;
    private LocalDateTime issuedAt;
    private String pdfUrl;
    private String verificationUrl;
}
