package com.freelms.course.repository;

import com.freelms.course.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByModuleIdOrderBySortOrder(Long moduleId);

    @Query("SELECT l FROM Lesson l WHERE l.module.course.id = :courseId ORDER BY l.module.sortOrder, l.sortOrder")
    List<Lesson> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT l FROM Lesson l WHERE l.module.course.id = :courseId AND l.isFreePreview = true")
    List<Lesson> findFreePreviewsByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.module.course.id = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT SUM(l.durationMinutes) FROM Lesson l WHERE l.module.course.id = :courseId")
    Integer getTotalDurationByCourseId(@Param("courseId") Long courseId);
}
