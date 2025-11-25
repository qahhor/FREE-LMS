package com.freelms.enrollment.repository;

import com.freelms.common.enums.EnrollmentStatus;
import com.freelms.enrollment.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    Page<Enrollment> findByUserId(Long userId, Pageable pageable);

    Page<Enrollment> findByUserIdAndStatus(Long userId, EnrollmentStatus status, Pageable pageable);

    List<Enrollment> findByCourseId(Long courseId);

    @Query("SELECT e FROM Enrollment e WHERE e.userId = :userId ORDER BY e.lastAccessedAt DESC")
    List<Enrollment> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.courseId = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.courseId = :courseId AND e.status = 'COMPLETED'")
    long countCompletedByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT AVG(e.progress) FROM Enrollment e WHERE e.courseId = :courseId")
    Double getAverageProgressByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT e FROM Enrollment e WHERE e.userId = :userId AND e.status = 'ACTIVE' " +
           "AND e.lastAccessedAt < :threshold")
    List<Enrollment> findInactiveEnrollments(@Param("userId") Long userId, @Param("threshold") LocalDateTime threshold);

    @Query("SELECT e.courseId, COUNT(e) FROM Enrollment e GROUP BY e.courseId ORDER BY COUNT(e) DESC")
    List<Object[]> findPopularCourses(Pageable pageable);
}
