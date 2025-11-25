package com.freelms.course.repository;

import com.freelms.common.enums.CourseLevel;
import com.freelms.common.enums.CourseStatus;
import com.freelms.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    Page<Course> findByInstructorId(Long instructorId, Pageable pageable);

    Page<Course> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND c.isFree = true")
    Page<Course> findFreeCourses(Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Course> searchCourses(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' " +
           "AND (:categoryId IS NULL OR c.category.id = :categoryId) " +
           "AND (:level IS NULL OR c.level = :level) " +
           "AND (:isFree IS NULL OR c.isFree = :isFree)")
    Page<Course> findWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("level") CourseLevel level,
            @Param("isFree") Boolean isFree,
            Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' ORDER BY c.studentCount DESC")
    List<Course> findPopularCourses(Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' ORDER BY c.publishedAt DESC")
    List<Course> findRecentCourses(Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' ORDER BY c.rating DESC")
    List<Course> findTopRatedCourses(Pageable pageable);

    @Modifying
    @Query("UPDATE Course c SET c.studentCount = c.studentCount + 1 WHERE c.id = :courseId")
    void incrementStudentCount(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.instructorId = :instructorId")
    long countByInstructor(@Param("instructorId") Long instructorId);

    @Query("SELECT c FROM Course c JOIN c.tags t WHERE t.slug = :tagSlug AND c.status = 'PUBLISHED'")
    Page<Course> findByTag(@Param("tagSlug") String tagSlug, Pageable pageable);
}
