package com.freelms.learningpath.repository;

import com.freelms.learningpath.entity.LearningPathEnrollment;
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
public interface LearningPathEnrollmentRepository extends JpaRepository<LearningPathEnrollment, Long> {

    Optional<LearningPathEnrollment> findByLearningPathIdAndUserId(Long learningPathId, Long userId);

    Page<LearningPathEnrollment> findByUserId(Long userId, Pageable pageable);

    Page<LearningPathEnrollment> findByUserIdAndStatus(Long userId, LearningPathEnrollment.EnrollmentStatus status, Pageable pageable);

    List<LearningPathEnrollment> findByLearningPathId(Long learningPathId);

    @Query("SELECT lpe FROM LearningPathEnrollment lpe WHERE lpe.deadline < :now AND lpe.status = 'ACTIVE'")
    List<LearningPathEnrollment> findOverdueEnrollments(@Param("now") LocalDateTime now);

    @Query("SELECT lpe FROM LearningPathEnrollment lpe WHERE lpe.deadline BETWEEN :start AND :end AND lpe.status = 'ACTIVE'")
    List<LearningPathEnrollment> findUpcomingDeadlines(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(lpe) FROM LearningPathEnrollment lpe WHERE lpe.userId = :userId AND lpe.status = 'COMPLETED'")
    Long countCompletedByUser(@Param("userId") Long userId);

    @Query("SELECT lpe FROM LearningPathEnrollment lpe WHERE lpe.assignedBy = :managerId")
    Page<LearningPathEnrollment> findByAssignedBy(@Param("managerId") Long managerId, Pageable pageable);

    boolean existsByLearningPathIdAndUserId(Long learningPathId, Long userId);
}
