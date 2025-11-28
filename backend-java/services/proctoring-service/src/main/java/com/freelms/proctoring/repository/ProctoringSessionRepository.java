package com.freelms.proctoring.repository;

import com.freelms.proctoring.entity.ProctoringSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ProctoringSession entities.
 */
@Repository
public interface ProctoringSessionRepository extends JpaRepository<ProctoringSession, UUID> {

    Optional<ProctoringSession> findByExamIdAndUserId(Long examId, Long userId);

    Page<ProctoringSession> findByExamId(Long examId, Pageable pageable);

    Page<ProctoringSession> findByUserId(Long userId, Pageable pageable);

    Page<ProctoringSession> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<ProctoringSession> findByStatus(ProctoringSession.SessionStatus status, Pageable pageable);

    @Query("SELECT s FROM ProctoringSession s WHERE s.requiresManualReview = true AND s.reviewDecision IS NULL")
    Page<ProctoringSession> findPendingReviews(Pageable pageable);

    @Query("SELECT s FROM ProctoringSession s WHERE s.riskLevel IN :levels")
    Page<ProctoringSession> findByRiskLevels(@Param("levels") List<ProctoringSession.RiskLevel> levels, Pageable pageable);

    @Query("SELECT s FROM ProctoringSession s WHERE s.aiAnalysisStatus = 'PENDING' AND s.status = 'COMPLETED'")
    List<ProctoringSession> findSessionsNeedingAiAnalysis();

    @Query("SELECT COUNT(s) FROM ProctoringSession s WHERE s.examId = :examId AND s.status = 'COMPLETED'")
    Long countCompletedByExamId(@Param("examId") Long examId);

    @Query("SELECT AVG(s.riskScore) FROM ProctoringSession s WHERE s.examId = :examId")
    Float getAverageRiskScoreByExamId(@Param("examId") Long examId);
}
