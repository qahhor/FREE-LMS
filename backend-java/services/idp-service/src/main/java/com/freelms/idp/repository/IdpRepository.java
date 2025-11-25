package com.freelms.idp.repository;

import com.freelms.idp.entity.IndividualDevelopmentPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdpRepository extends JpaRepository<IndividualDevelopmentPlan, Long> {

    List<IndividualDevelopmentPlan> findByUserId(Long userId);

    Page<IndividualDevelopmentPlan> findByUserIdAndStatus(Long userId, IndividualDevelopmentPlan.PlanStatus status, Pageable pageable);

    @Query("SELECT p FROM IndividualDevelopmentPlan p WHERE p.managerId = :managerId AND p.status = 'ACTIVE'")
    List<IndividualDevelopmentPlan> findActiveByManager(@Param("managerId") Long managerId);

    @Query("SELECT p FROM IndividualDevelopmentPlan p WHERE p.mentorId = :mentorId")
    List<IndividualDevelopmentPlan> findByMentor(@Param("mentorId") Long mentorId);

    @Query("SELECT p FROM IndividualDevelopmentPlan p WHERE p.status = 'PENDING_APPROVAL' AND p.managerId = :managerId")
    List<IndividualDevelopmentPlan> findPendingApproval(@Param("managerId") Long managerId);

    Page<IndividualDevelopmentPlan> findByOrganizationId(Long organizationId, Pageable pageable);
}
