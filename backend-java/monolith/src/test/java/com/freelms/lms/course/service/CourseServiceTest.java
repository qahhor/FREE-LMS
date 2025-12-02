package com.freelms.lms.course.service;

import com.freelms.lms.common.enums.CourseLevel;
import com.freelms.lms.common.enums.CourseStatus;
import com.freelms.lms.common.exception.ForbiddenException;
import com.freelms.lms.common.exception.ResourceNotFoundException;
import com.freelms.lms.course.dto.CourseDto;
import com.freelms.lms.course.dto.CreateCourseRequest;
import com.freelms.lms.course.entity.Category;
import com.freelms.lms.course.entity.Course;
import com.freelms.lms.course.entity.CourseModule;
import com.freelms.lms.course.mapper.CourseMapper;
import com.freelms.lms.course.repository.CategoryRepository;
import com.freelms.lms.course.repository.CourseRepository;
import com.freelms.lms.course.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private CourseService courseService;

    private Course testCourse;
    private CourseDto testCourseDto;
    private CreateCourseRequest createRequest;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Programming")
                .slug("programming")
                .build();

        testCourse = Course.builder()
                .id(1L)
                .title("Java Fundamentals")
                .slug("java-fundamentals")
                .description("Learn Java programming")
                .status(CourseStatus.DRAFT)
                .level(CourseLevel.BEGINNER)
                .instructorId(1L)
                .price(BigDecimal.ZERO)
                .isFree(true)
                .category(testCategory)
                .modules(new ArrayList<>())
                .build();

        testCourseDto = CourseDto.builder()
                .id(1L)
                .title("Java Fundamentals")
                .slug("java-fundamentals")
                .description("Learn Java programming")
                .status(CourseStatus.DRAFT)
                .level(CourseLevel.BEGINNER)
                .instructorId(1L)
                .build();

        createRequest = CreateCourseRequest.builder()
                .title("Java Fundamentals")
                .description("Learn Java programming")
                .level(CourseLevel.BEGINNER)
                .isFree(true)
                .categoryId(1L)
                .build();
    }

    @Test
    @DisplayName("Should create course successfully")
    void shouldCreateCourseSuccessfully() {
        // Given
        when(courseMapper.toEntity(any(CreateCourseRequest.class))).thenReturn(testCourse);
        when(courseRepository.existsBySlug(anyString())).thenReturn(false);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);
        when(courseMapper.toDto(any(Course.class))).thenReturn(testCourseDto);

        // When
        CourseDto result = courseService.createCourse(1L, createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Java Fundamentals");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Should get course by ID successfully")
    void shouldGetCourseByIdSuccessfully() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseMapper.toDto(any(Course.class))).thenReturn(testCourseDto);

        // When
        CourseDto result = courseService.getCourseById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent course")
    void shouldThrowResourceNotFoundExceptionForNonExistentCourse() {
        // Given
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> courseService.getCourseById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get course by slug successfully")
    void shouldGetCourseBySlugSuccessfully() {
        // Given
        when(courseRepository.findBySlug("java-fundamentals")).thenReturn(Optional.of(testCourse));
        when(courseMapper.toDto(any(Course.class))).thenReturn(testCourseDto);

        // When
        CourseDto result = courseService.getCourseBySlug("java-fundamentals");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSlug()).isEqualTo("java-fundamentals");
    }

    @Test
    @DisplayName("Should publish course successfully")
    void shouldPublishCourseSuccessfully() {
        // Given
        testCourse.getModules().add(CourseModule.builder().id(1L).title("Module 1").build());
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);
        when(courseMapper.toDto(any(Course.class))).thenReturn(testCourseDto);

        // When
        CourseDto result = courseService.publishCourse(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Should throw ForbiddenException when unauthorized user tries to update")
    void shouldThrowForbiddenExceptionForUnauthorizedUpdate() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        // When/Then
        assertThatThrownBy(() -> courseService.publishCourse(1L, 999L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Should delete course successfully")
    void shouldDeleteCourseSuccessfully() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        // When
        courseService.deleteCourse(1L, 1L);

        // Then
        verify(courseRepository).delete(testCourse);
    }

    @Test
    @DisplayName("Should get all published courses successfully")
    void shouldGetAllPublishedCoursesSuccessfully() {
        // Given
        when(courseRepository.findByStatus(eq(CourseStatus.PUBLISHED), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());
        when(courseMapper.toDtoList(anyList())).thenReturn(new ArrayList<>());

        // When
        var result = courseService.getAllPublishedCourses(org.springframework.data.domain.Pageable.unpaged());

        // Then
        assertThat(result).isNotNull();
        verify(courseRepository).findByStatus(eq(CourseStatus.PUBLISHED), any());
    }

    @Test
    @DisplayName("Should search courses successfully")
    void shouldSearchCoursesSuccessfully() {
        // Given
        String query = "Java";
        when(courseRepository.searchCourses(eq(query), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());
        when(courseMapper.toDtoList(anyList())).thenReturn(new ArrayList<>());

        // When
        var result = courseService.searchCourses(query, org.springframework.data.domain.Pageable.unpaged());

        // Then
        assertThat(result).isNotNull();
        verify(courseRepository).searchCourses(eq(query), any());
    }

    @Test
    @DisplayName("Should update course successfully")
    void shouldUpdateCourseSuccessfully() {
        // Given
        com.freelms.lms.course.dto.UpdateCourseRequest updateRequest = com.freelms.lms.course.dto.UpdateCourseRequest.builder()
                .title("Updated Java Fundamentals")
                .description("Updated description")
                .build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);
        when(courseMapper.toDto(any(Course.class))).thenReturn(testCourseDto);

        // When
        CourseDto result = courseService.updateCourse(1L, 1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(courseRepository).save(any(Course.class));
        verify(courseMapper).updateCourseFromRequest(eq(updateRequest), eq(testCourse));
    }
}
