package com.freelms.lms.course.mapper;

import com.freelms.lms.course.dto.*;
import com.freelms.lms.course.entity.*;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CourseMapper {

    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "studentCount", constant = "0")
    @Mapping(target = "rating", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "ratingCount", constant = "0")
    @Mapping(target = "modules", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "instructorId", ignore = true)
    @BeanMapping(builder = @Builder(disableBuilder = true))
    Course toEntity(CreateCourseRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "tags", expression = "java(mapTags(course.getTags()))")
    @Mapping(target = "moduleCount", expression = "java(course.getModules() != null ? course.getModules().size() : 0)")
    @Mapping(target = "lessonCount", expression = "java(countLessons(course))")
    @Mapping(target = "featured", source = "featured")
    @Mapping(target = "free", source = "free")
    CourseDto toDto(Course course);

    List<CourseDto> toDtoList(List<Course> courses);

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "lessonCount", expression = "java(module.getLessonCount())")
    @Mapping(target = "published", source = "published")
    ModuleDto toModuleDto(CourseModule module);

    List<ModuleDto> toModuleDtoList(List<CourseModule> modules);

    @Mapping(target = "moduleId", source = "module.id")
    @Mapping(target = "published", source = "published")
    @Mapping(target = "preview", source = "preview")
    @Mapping(target = "mandatory", source = "mandatory")
    LessonDto toLessonDto(Lesson lesson);

    List<LessonDto> toLessonDtoList(List<Lesson> lessons);

    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "questionCount", expression = "java(quiz.getQuestionCount())")
    @Mapping(target = "totalPoints", expression = "java(quiz.getTotalPoints())")
    QuizDto toQuizDto(Quiz quiz);

    QuizQuestionDto toQuestionDto(QuizQuestion question);

    List<QuizQuestionDto> toQuestionDtoList(List<QuizQuestion> questions);

    @Mapping(target = "correct", source = "correct")
    QuizAnswerDto toAnswerDto(QuizAnswer answer);

    List<QuizAnswerDto> toAnswerDtoList(List<QuizAnswer> answers);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "active", source = "active")
    CategoryDto toCategoryDto(Category category);

    List<CategoryDto> toCategoryDtoList(List<Category> categories);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "modules", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateCourseFromRequest(UpdateCourseRequest request, @MappingTarget Course course);

    default Set<String> mapTags(Set<Tag> tags) {
        if (tags == null) return null;
        return tags.stream().map(Tag::getName).collect(Collectors.toSet());
    }

    default int countLessons(Course course) {
        if (course.getModules() == null) return 0;
        return course.getModules().stream()
                .mapToInt(m -> m.getLessons() != null ? m.getLessons().size() : 0)
                .sum();
    }
}
