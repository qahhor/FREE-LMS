package com.freelms.learningpath.repository;

import com.freelms.learningpath.entity.UserCareerProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCareerProgressRepository extends JpaRepository<UserCareerProgress, Long> {

    Optional<UserCareerProgress> findByUserIdAndCareerTrackId(Long userId, Long careerTrackId);

    List<UserCareerProgress> findByUserId(Long userId);

    Page<UserCareerProgress> findByCareerTrackId(Long careerTrackId, Pageable pageable);

    @Query("SELECT ucp FROM UserCareerProgress ucp WHERE ucp.mentorId = :mentorId")
    List<UserCareerProgress> findByMentor(@Param("mentorId") Long mentorId);

    @Query("SELECT ucp FROM UserCareerProgress ucp WHERE ucp.careerTrack.id = :trackId ORDER BY ucp.progressPercentage DESC")
    List<UserCareerProgress> findTopProgressByTrack(@Param("trackId") Long trackId, Pageable pageable);
}
