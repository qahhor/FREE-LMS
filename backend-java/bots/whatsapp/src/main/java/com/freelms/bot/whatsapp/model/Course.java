package com.freelms.bot.whatsapp.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Course {
    private Long id;
    private String title;
    private String description;
    private String level;
    private BigDecimal price;
    private boolean isFree;
    private String thumbnailUrl;
    private Integer totalLessons;
    private Integer totalDuration;
}
