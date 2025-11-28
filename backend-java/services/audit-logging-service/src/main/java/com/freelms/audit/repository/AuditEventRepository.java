package com.freelms.audit.repository;

import com.freelms.audit.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    Page<AuditEvent> findByActorId(Long actorId, Pageable pageable);

    Page<AuditEvent> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<AuditEvent> findByEventType(AuditEvent.EventType eventType, Pageable pageable);

    Page<AuditEvent> findByCategory(AuditEvent.EventCategory category, Pageable pageable);

    Page<AuditEvent> findBySeverity(AuditEvent.Severity severity, Pageable pageable);

    @Query("SELECT a FROM AuditEvent a WHERE a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp DESC")
    Page<AuditEvent> findByDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    @Query("SELECT a FROM AuditEvent a WHERE a.resourceType = :type AND a.resourceId = :id ORDER BY a.timestamp DESC")
    List<AuditEvent> findByResource(
            @Param("type") String resourceType,
            @Param("id") String resourceId);

    @Query("SELECT a FROM AuditEvent a WHERE a.actorId = :userId " +
           "AND a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp DESC")
    List<AuditEvent> findUserActivityInRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT a FROM AuditEvent a WHERE a.severity IN ('ERROR', 'CRITICAL') " +
           "AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditEvent> findRecentErrors(@Param("since") LocalDateTime since);

    @Query("SELECT a FROM AuditEvent a WHERE a.category = 'SECURITY' " +
           "AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditEvent> findSecurityEvents(@Param("since") LocalDateTime since);

    @Query("SELECT a FROM AuditEvent a WHERE a.containsPii = true " +
           "AND a.actorId = :userId ORDER BY a.timestamp DESC")
    List<AuditEvent> findPiiAccessByUser(@Param("userId") Long userId);

    @Query("SELECT a FROM AuditEvent a WHERE a.eventType IN ('DATA_EXPORTED', 'DATA_DELETED') " +
           "AND a.timestamp >= :since")
    List<AuditEvent> findDataOperations(@Param("since") LocalDateTime since);

    // Compliance queries
    @Query("SELECT a FROM AuditEvent a WHERE a.eventType = 'LOGIN_FAILED' " +
           "AND a.actorId = :userId AND a.timestamp >= :since")
    List<AuditEvent> findFailedLoginAttempts(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since);

    @Query("SELECT DISTINCT a.ipAddress FROM AuditEvent a WHERE a.actorId = :userId " +
           "AND a.timestamp >= :since")
    List<String> findUserIpAddresses(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since);

    // Statistics
    @Query("SELECT a.eventType, COUNT(a) FROM AuditEvent a " +
           "WHERE a.organizationId = :orgId AND a.timestamp >= :since " +
           "GROUP BY a.eventType")
    List<Object[]> countByEventType(
            @Param("orgId") Long organizationId,
            @Param("since") LocalDateTime since);

    @Query("SELECT a.category, COUNT(a) FROM AuditEvent a " +
           "WHERE a.organizationId = :orgId AND a.timestamp >= :since " +
           "GROUP BY a.category")
    List<Object[]> countByCategory(
            @Param("orgId") Long organizationId,
            @Param("since") LocalDateTime since);

    @Query("SELECT DATE(a.timestamp), COUNT(a) FROM AuditEvent a " +
           "WHERE a.timestamp >= :since GROUP BY DATE(a.timestamp)")
    List<Object[]> countByDay(@Param("since") LocalDateTime since);

    // Retention management
    @Query("SELECT a FROM AuditEvent a WHERE a.retentionUntil < :now AND a.archived = false")
    List<AuditEvent> findExpiredEvents(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE AuditEvent a SET a.archived = true WHERE a.id IN :ids")
    void archiveEvents(@Param("ids") List<UUID> ids);

    @Modifying
    @Query("DELETE FROM AuditEvent a WHERE a.retentionUntil < :now AND a.archived = true")
    int deleteArchivedExpiredEvents(@Param("now") LocalDateTime now);
}
