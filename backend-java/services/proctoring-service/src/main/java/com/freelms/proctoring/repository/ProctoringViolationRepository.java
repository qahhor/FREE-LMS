package com.freelms.proctoring.repository;

import com.freelms.proctoring.entity.ProctoringViolation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProctoringViolationRepository extends JpaRepository<ProctoringViolation, UUID> {
    List<ProctoringViolation> findBySessionIdOrderByDetectedAtAsc(UUID sessionId);
}
