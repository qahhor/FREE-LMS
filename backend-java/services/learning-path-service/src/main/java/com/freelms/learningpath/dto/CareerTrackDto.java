package com.freelms.learningpath.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareerTrackDto {
    private Long id;
    private String title;
    private String description;
    private Long organizationId;
    private Long departmentId;
    private String iconUrl;
    private Boolean isActive;
    private List<CareerLevelDto> levels;
    private Long createdBy;
}
