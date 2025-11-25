package com.freelms.gamification.repository;

import com.freelms.gamification.entity.Challenge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    Page<Challenge> findByOrganizationIdAndStatus(Long organizationId, Challenge.ChallengeStatus status, Pageable pageable);

    @Query("SELECT c FROM Challenge c WHERE c.organizationId = :orgId AND c.status = 'ACTIVE' AND c.endDate > :now")
    List<Challenge> findActiveChallenges(@Param("orgId") Long organizationId, @Param("now") LocalDateTime now);

    @Query("SELECT c FROM Challenge c WHERE c.status = 'ACTIVE' AND c.endDate <= :now")
    List<Challenge> findExpiredChallenges(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM Challenge c WHERE c.status = 'UPCOMING' AND c.startDate <= :now")
    List<Challenge> findChallengesToStart(@Param("now") LocalDateTime now);

    List<Challenge> findByIsTeamChallengeTrueAndStatus(Challenge.ChallengeStatus status);
}
