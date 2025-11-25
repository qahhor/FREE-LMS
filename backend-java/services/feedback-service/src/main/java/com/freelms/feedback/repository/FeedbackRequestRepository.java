package com.freelms.feedback.repository;

import com.freelms.feedback.entity.FeedbackRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRequestRepository extends JpaRepository<FeedbackRequest, Long> {

    List<FeedbackRequest> findByReviewerIdAndStatus(Long reviewerId, FeedbackRequest.RequestStatus status);

    List<FeedbackRequest> findByTargetUserId(Long targetUserId);

    List<FeedbackRequest> findByCycleIdAndTargetUserId(Long cycleId, Long targetUserId);

    @Query("SELECT fr FROM FeedbackRequest fr WHERE fr.reviewerId = :userId AND fr.status = 'PENDING'")
    List<FeedbackRequest> findPendingReviewsForUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(fr) FROM FeedbackRequest fr WHERE fr.cycle.id = :cycleId AND fr.status = 'SUBMITTED'")
    Long countSubmittedByCycle(@Param("cycleId") Long cycleId);
}
