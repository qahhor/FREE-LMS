package com.freelms.feedback.repository;

import com.freelms.feedback.entity.FeedbackCycle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackCycleRepository extends JpaRepository<FeedbackCycle, Long> {

    Page<FeedbackCycle> findByOrganizationId(Long organizationId, Pageable pageable);

    List<FeedbackCycle> findByOrganizationIdAndStatus(Long organizationId, FeedbackCycle.CycleStatus status);

    List<FeedbackCycle> findByStatusIn(List<FeedbackCycle.CycleStatus> statuses);
}
