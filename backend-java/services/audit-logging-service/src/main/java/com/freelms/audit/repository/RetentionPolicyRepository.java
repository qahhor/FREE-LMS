package com.freelms.audit.repository;

import com.freelms.audit.entity.AuditEvent;
import com.freelms.audit.entity.RetentionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RetentionPolicyRepository extends JpaRepository<RetentionPolicy, UUID> {

    Optional<RetentionPolicy> findByName(String name);

    List<RetentionPolicy> findByOrganizationId(Long organizationId);

    List<RetentionPolicy> findByFramework(RetentionPolicy.ComplianceFramework framework);

    @Query("SELECT p FROM RetentionPolicy p WHERE p.active = true AND " +
           "(p.nextExecution IS NULL OR p.nextExecution <= :now)")
    List<RetentionPolicy> findPoliciesDueForExecution(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM RetentionPolicy p WHERE p.active = true AND " +
           "(p.category = :category OR p.category IS NULL) AND " +
           "(p.organizationId = :orgId OR p.organizationId IS NULL)")
    List<RetentionPolicy> findApplicablePolicies(
            @Param("category") AuditEvent.EventCategory category,
            @Param("orgId") Long organizationId);
}
