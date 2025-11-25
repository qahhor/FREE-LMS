package com.freelms.learningpath.repository;

import com.freelms.learningpath.entity.CareerTrack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CareerTrackRepository extends JpaRepository<CareerTrack, Long> {

    Page<CareerTrack> findByOrganizationIdAndIsActiveTrue(Long organizationId, Pageable pageable);

    List<CareerTrack> findByDepartmentIdAndIsActiveTrue(Long departmentId);

    List<CareerTrack> findByOrganizationIdAndIsActiveTrue(Long organizationId);
}
