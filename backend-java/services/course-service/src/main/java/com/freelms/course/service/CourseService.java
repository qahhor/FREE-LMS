package com.freelms.course.service;

import com.freelms.common.dto.PagedResponse;
import com.freelms.common.enums.CourseLevel;
import com.freelms.common.enums.CourseStatus;
import com.freelms.common.enums.UserRole;
import com.freelms.common.exception.ForbiddenException;
import com.freelms.common.exception.ResourceNotFoundException;
import com.freelms.common.util.SlugUtils;
import com.freelms.course.dto.*;
import com.freelms.course.entity.Category;
import com.freelms.course.entity.Course;
import com.freelms.course.entity.Tag;
import com.freelms.course.mapper.CourseMapper;
import com.freelms.course.repository.CategoryRepository;
import com.freelms.course.repository.CourseRepository;
import com.freelms.course.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CourseMapper courseMapper;

    @Cacheable(value = "courses", key = "#id")
    @Transactional(readOnly = true)
    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return courseMapper.toDto(course);
    }

    @Transactional(readOnly = true)
    public CourseDto getCourseBySlug(String slug) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "slug", slug));
        return courseMapper.toDto(course);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CourseDto> getAllCourses(Pageable pageable) {
        Page<Course> coursePage = courseRepository.findByStatus(CourseStatus.PUBLISHED, pageable);
        List<CourseDto> courseDtos = courseMapper.toDtoList(coursePage.getContent());
        return PagedResponse.of(coursePage, courseDtos);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CourseDto> getCoursesByInstructor(Long instructorId, Pageable pageable) {
        Page<Course> coursePage = courseRepository.findByInstructorId(instructorId, pageable);
        List<CourseDto> courseDtos = courseMapper.toDtoList(coursePage.getContent());
        return PagedResponse.of(coursePage, courseDtos);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CourseDto> getCoursesByCategory(Long categoryId, Pageable pageable) {
        Page<Course> coursePage = courseRepository.findByCategoryId(categoryId, pageable);
        List<CourseDto> courseDtos = courseMapper.toDtoList(coursePage.getContent());
        return PagedResponse.of(coursePage, courseDtos);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CourseDto> searchCourses(String query, Pageable pageable) {
        Page<Course> coursePage = courseRepository.searchCourses(query, pageable);
        List<CourseDto> courseDtos = courseMapper.toDtoList(coursePage.getContent());
        return PagedResponse.of(coursePage, courseDtos);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CourseDto> filterCourses(Long categoryId, CourseLevel level, Boolean isFree, Pageable pageable) {
        Page<Course> coursePage = courseRepository.findWithFilters(categoryId, level, isFree, pageable);
        List<CourseDto> courseDtos = courseMapper.toDtoList(coursePage.getContent());
        return PagedResponse.of(coursePage, courseDtos);
    }

    @Transactional(readOnly = true)
    public List<CourseDto> getPopularCourses(int limit) {
        List<Course> courses = courseRepository.findPopularCourses(Pageable.ofSize(limit));
        return courseMapper.toDtoList(courses);
    }

    @Transactional(readOnly = true)
    public List<CourseDto> getRecentCourses(int limit) {
        List<Course> courses = courseRepository.findRecentCourses(Pageable.ofSize(limit));
        return courseMapper.toDtoList(courses);
    }

    @Transactional
    public CourseDto createCourse(CreateCourseRequest request, Long instructorId) {
        log.info("Creating course: {} by instructor: {}", request.getTitle(), instructorId);

        String slug = SlugUtils.generateUniqueSlug(request.getTitle(), courseRepository::existsBySlug);

        Course course = courseMapper.toEntity(request);
        course.setSlug(slug);
        course.setInstructorId(instructorId);
        course.setStatus(CourseStatus.DRAFT);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            course.setCategory(category);
        }

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = getOrCreateTags(request.getTags());
            course.setTags(tags);
        }

        course = courseRepository.save(course);
        log.info("Course created with ID: {}", course.getId());

        return courseMapper.toDto(course);
    }

    @CacheEvict(value = "courses", key = "#id")
    @Transactional
    public CourseDto updateCourse(Long id, UpdateCourseRequest request, Long userId, UserRole userRole) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        // Check permissions
        if (!course.getInstructorId().equals(userId) && userRole != UserRole.ADMIN) {
            throw new ForbiddenException("You can only update your own courses");
        }

        courseMapper.updateEntity(request, course);

        if (request.getTitle() != null) {
            String newSlug = SlugUtils.generateUniqueSlug(request.getTitle(),
                    slug -> !slug.equals(course.getSlug()) && courseRepository.existsBySlug(slug));
            course.setSlug(newSlug);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            course.setCategory(category);
        }

        if (request.getTags() != null) {
            Set<Tag> tags = getOrCreateTags(request.getTags());
            course.setTags(tags);
        }

        course = courseRepository.save(course);
        log.info("Course updated: {}", id);

        return courseMapper.toDto(course);
    }

    @CacheEvict(value = "courses", key = "#id")
    @Transactional
    public CourseDto publishCourse(Long id, Long userId, UserRole userRole) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        if (!course.getInstructorId().equals(userId) && userRole != UserRole.ADMIN) {
            throw new ForbiddenException("You can only publish your own courses");
        }

        course.publish();
        course = courseRepository.save(course);
        log.info("Course published: {}", id);

        return courseMapper.toDto(course);
    }

    @CacheEvict(value = "courses", key = "#id")
    @Transactional
    public void deleteCourse(Long id, Long userId, UserRole userRole) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        if (!course.getInstructorId().equals(userId) && userRole != UserRole.ADMIN) {
            throw new ForbiddenException("You can only delete your own courses");
        }

        // Soft delete by archiving
        course.archive();
        courseRepository.save(course);
        log.info("Course archived: {}", id);
    }

    private Set<Tag> getOrCreateTags(Set<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            String slug = SlugUtils.toSlug(tagName);
            Tag tag = tagRepository.findBySlug(slug)
                    .orElseGet(() -> tagRepository.save(Tag.builder()
                            .name(tagName)
                            .slug(slug)
                            .build()));
            tags.add(tag);
        }
        return tags;
    }
}
