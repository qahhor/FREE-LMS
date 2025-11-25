package com.freelms.course.mapper;

import com.freelms.course.dto.*;
import com.freelms.course.entity.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CourseMapper {

    @Mapping(target = "instructorName", ignore = true)
    @Mapping(target = "totalLessons", expression = "java(calculateTotalLessons(course))")
    CourseDto toDto(Course course);

    List<CourseDto> toDtoList(List<Course> courses);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "instructorId", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "modules", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "studentCount", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true)
    Course toEntity(CreateCourseRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "instructorId", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "modules", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "studentCount", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true)
    void updateEntity(UpdateCourseRequest request, @MappingTarget Course course);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "courseCount", expression = "java(category.getCourses().size())")
    CategoryDto toCategoryDto(Category category);

    List<CategoryDto> toCategoryDtoList(List<Category> categories);

    ModuleDto toModuleDto(CourseModule module);

    LessonDto toLessonDto(Lesson lesson);

    TagDto toTagDto(Tag tag);

    default int calculateTotalLessons(Course course) {
        if (course.getModules() == null) return 0;
        return course.getModules().stream()
                .mapToInt(module -> module.getLessons() != null ? module.getLessons().size() : 0)
                .sum();
    }
}
