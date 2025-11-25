package com.freelms.idp.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionDto {
    private Long id;
    private String title;
    private String description;
    private Boolean isCompleted;
    private LocalDate dueDate;
    private LocalDate completedDate;
}
