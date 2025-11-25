package com.freelms.skills.repository;

import com.freelms.skills.entity.SkillGap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillGapRepository extends JpaRepository<SkillGap, Long> {

    List<SkillGap> findByUserIdAndResolvedAtIsNullOrderByPriorityAsc(Long userId);

    List<SkillGap> findByUserIdAndSkillIdAndResolvedAtIsNull(Long userId, Long skillId);

    @Query("SELECT sg FROM SkillGap sg WHERE sg.userId = :userId AND sg.resolvedAt IS NULL " +
           "ORDER BY sg.priority ASC, sg.gapSize DESC")
    List<SkillGap> findActiveGapsByUser(@Param("userId") Long userId);

    @Query("SELECT sg FROM SkillGap sg WHERE sg.userId IN :userIds AND sg.resolvedAt IS NULL")
    List<SkillGap> findActiveGapsByUsers(@Param("userIds") List<Long> userIds);

    @Query("SELECT COUNT(sg) FROM SkillGap sg WHERE sg.userId = :userId AND sg.resolvedAt IS NULL " +
           "AND sg.priority IN ('CRITICAL', 'HIGH')")
    Long countCriticalGaps(@Param("userId") Long userId);
}
