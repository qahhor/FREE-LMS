package com.freelms.learningpath.repository;

import com.freelms.common.enums.CourseStatus;
import com.freelms.learningpath.entity.LearningPath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {

    Page<LearningPath> findByOrganizationIdAndStatus(Long organizationId, CourseStatus status, Pageable pageable);

    Page<LearningPath> findByOrganizationId(Long organizationId, Pageable pageable);

    List<LearningPath> findByIsMandatoryTrueAndStatus(CourseStatus status);

    @Query("SELECT lp FROM LearningPath lp WHERE lp.organizationId = :orgId " +
           "AND lp.status = :status " +
           "AND (LOWER(lp.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(lp.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<LearningPath> searchByOrganization(@Param("orgId") Long orgId,
                                             @Param("status") CourseStatus status,
                                             @Param("search") String search,
                                             Pageable pageable);

    @Query("SELECT lp FROM LearningPath lp WHERE lp.targetRoles LIKE %:roleId% AND lp.status = 'PUBLISHED'")
    List<LearningPath> findByTargetRole(@Param("roleId") String roleId);

    @Query("SELECT lp FROM LearningPath lp WHERE lp.targetDepartments LIKE %:deptId% AND lp.status = 'PUBLISHED'")
    List<LearningPath> findByTargetDepartment(@Param("deptId") String deptId);

    @Query("SELECT COUNT(lpe) FROM LearningPathEnrollment lpe WHERE lpe.learningPath.id = :pathId AND lpe.status = 'COMPLETED'")
    Long countCompletions(@Param("pathId") Long pathId);

    @Query("SELECT AVG(lpe.progressPercentage) FROM LearningPathEnrollment lpe WHERE lpe.learningPath.id = :pathId")
    Double getAverageProgress(@Param("pathId") Long pathId);
}
