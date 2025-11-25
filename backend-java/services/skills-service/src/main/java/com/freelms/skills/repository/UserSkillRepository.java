package com.freelms.skills.repository;

import com.freelms.skills.entity.UserSkill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUserId(Long userId);

    Optional<UserSkill> findByUserIdAndSkillId(Long userId, Long skillId);

    @Query("SELECT us FROM UserSkill us WHERE us.skill.category.id = :categoryId AND us.userId = :userId")
    List<UserSkill> findByUserIdAndCategory(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    @Query("SELECT us FROM UserSkill us WHERE us.userId IN :userIds AND us.skill.id = :skillId")
    List<UserSkill> findByUsersAndSkill(@Param("userIds") List<Long> userIds, @Param("skillId") Long skillId);

    @Query("SELECT us FROM UserSkill us WHERE us.skill.id = :skillId ORDER BY us.currentLevel DESC")
    Page<UserSkill> findTopBySkill(@Param("skillId") Long skillId, Pageable pageable);

    @Query("SELECT us FROM UserSkill us WHERE us.userId = :userId AND us.targetLevel > us.currentLevel")
    List<UserSkill> findSkillsInProgress(@Param("userId") Long userId);

    @Query("SELECT us FROM UserSkill us WHERE us.nextAssessmentDue <= CURRENT_TIMESTAMP AND us.userId = :userId")
    List<UserSkill> findDueForAssessment(@Param("userId") Long userId);

    @Query("SELECT DISTINCT us.userId FROM UserSkill us WHERE us.skill.id = :skillId AND us.currentLevel >= :level")
    List<Long> findUsersWithSkillLevel(@Param("skillId") Long skillId, @Param("level") Integer level);
}
