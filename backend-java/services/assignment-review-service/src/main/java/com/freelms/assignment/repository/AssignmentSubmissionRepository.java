package com.freelms.assignment.repository;

import com.freelms.assignment.entity.AssignmentSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, UUID> {

    Page<AssignmentSubmission> findByAssignmentId(Long assignmentId, Pageable pageable);

    Page<AssignmentSubmission> findByStudentId(Long studentId, Pageable pageable);

    Optional<AssignmentSubmission> findByAssignmentIdAndStudentIdAndAttemptNumber(
            Long assignmentId, Long studentId, Integer attemptNumber);

    @Query("SELECT s FROM AssignmentSubmission s WHERE s.assignmentId = :assignmentId AND s.studentId = :studentId " +
           "ORDER BY s.attemptNumber DESC")
    List<AssignmentSubmission> findAllAttempts(@Param("assignmentId") Long assignmentId, @Param("studentId") Long studentId);

    @Query("SELECT s FROM AssignmentSubmission s WHERE s.status = 'SUBMITTED' ORDER BY s.submittedAt ASC")
    Page<AssignmentSubmission> findPendingReview(Pageable pageable);

    @Query("SELECT s FROM AssignmentSubmission s WHERE s.courseId = :courseId AND s.status = 'SUBMITTED'")
    Page<AssignmentSubmission> findPendingByCourse(@Param("courseId") Long courseId, Pageable pageable);

    @Query("SELECT AVG(s.score) FROM AssignmentSubmission s WHERE s.assignmentId = :assignmentId AND s.score IS NOT NULL")
    Float getAverageScore(@Param("assignmentId") Long assignmentId);

    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.assignmentId = :assignmentId AND s.status = 'GRADED'")
    Long countGraded(@Param("assignmentId") Long assignmentId);
}
