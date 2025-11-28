package com.freelms.lti.repository;

import com.freelms.lti.entity.LtiLaunch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LtiLaunchRepository extends JpaRepository<LtiLaunch, UUID> {

    Optional<LtiLaunch> findByNonce(String nonce);

    Optional<LtiLaunch> findByState(String state);

    List<LtiLaunch> findByUserId(Long userId);

    List<LtiLaunch> findByPlatformId(UUID platformId);

    List<LtiLaunch> findByToolId(UUID toolId);

    @Query("SELECT l FROM LtiLaunch l WHERE l.status IN ('INITIATED', 'OIDC_INITIATED') " +
           "AND l.expiresAt < :now")
    List<LtiLaunch> findExpiredLaunches(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE LtiLaunch l SET l.status = 'EXPIRED' WHERE l.status IN ('INITIATED', 'OIDC_INITIATED') " +
           "AND l.expiresAt < :now")
    int expireStaleLaunches(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(l) FROM LtiLaunch l WHERE l.platformId = :platformId " +
           "AND l.status = 'COMPLETED' AND l.createdAt >= :since")
    Long countSuccessfulLaunches(
            @Param("platformId") UUID platformId,
            @Param("since") LocalDateTime since);
}
