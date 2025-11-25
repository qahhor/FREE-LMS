package com.freelms.skills.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndorsementDto {
    private Long id;
    private Long endorserId;
    private String endorserName;
    private String endorserAvatar;
    private Integer endorsedLevel;
    private String comment;
    private LocalDateTime endorsedAt;
    private String relationship;
}
