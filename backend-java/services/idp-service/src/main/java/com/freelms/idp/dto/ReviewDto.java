package com.freelms.idp.dto;

import com.freelms.idp.entity.PlanReview;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {
    private Long id;
    private Long reviewerId;
    private String reviewerName;
    private LocalDate reviewDate;
    private PlanReview.ReviewType reviewType;
    private Integer overallProgress;
    private String feedback;
    private String recommendations;
    private LocalDate nextReviewDate;
    private Boolean isOnTrack;
}
