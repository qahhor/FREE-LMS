package com.freelms.skills.repository;

import com.freelms.skills.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    Page<Skill> findByOrganizationIdAndIsActiveTrue(Long organizationId, Pageable pageable);

    List<Skill> findByIsGlobalTrueAndIsActiveTrue();

    Page<Skill> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    @Query("SELECT s FROM Skill s WHERE s.isActive = true AND " +
           "(s.organizationId = :orgId OR s.isGlobal = true) AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Skill> searchSkills(@Param("orgId") Long organizationId,
                              @Param("search") String search,
                              Pageable pageable);

    @Query("SELECT s FROM Skill s WHERE s.isActive = true AND " +
           "(s.organizationId = :orgId OR s.isGlobal = true)")
    List<Skill> findAllAvailableSkills(@Param("orgId") Long organizationId);

    boolean existsByNameAndOrganizationId(String name, Long organizationId);
}
