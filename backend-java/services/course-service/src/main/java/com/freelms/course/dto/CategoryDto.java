package com.freelms.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String iconUrl;
    private Integer sortOrder;
    private Long parentId;
    private List<CategoryDto> children;
    private Integer courseCount;
}
