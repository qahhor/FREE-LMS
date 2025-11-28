package com.freelms.booking.repository;

import com.freelms.booking.entity.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    Page<Resource> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<Resource> findByType(Resource.ResourceType type, Pageable pageable);

    Page<Resource> findByStatus(Resource.ResourceStatus status, Pageable pageable);

    @Query("SELECT r FROM Resource r WHERE r.status = 'AVAILABLE' " +
           "AND r.organizationId = :orgId " +
           "AND (:type IS NULL OR r.type = :type) " +
           "AND (:minCapacity IS NULL OR r.capacity >= :minCapacity)")
    Page<Resource> findAvailableResources(
            @Param("orgId") Long organizationId,
            @Param("type") Resource.ResourceType type,
            @Param("minCapacity") Integer minCapacity,
            Pageable pageable);

    @Query("SELECT r FROM Resource r WHERE r.building = :building AND r.status = 'AVAILABLE'")
    List<Resource> findByBuilding(@Param("building") String building);

    @Query("SELECT r FROM Resource r JOIN r.features f WHERE f IN :features AND r.status = 'AVAILABLE'")
    List<Resource> findByFeatures(@Param("features") List<String> features);

    List<Resource> findByManagerId(Long managerId);

    @Query("SELECT DISTINCT r.building FROM Resource r WHERE r.organizationId = :orgId AND r.building IS NOT NULL")
    List<String> findDistinctBuildings(@Param("orgId") Long organizationId);

    @Query("SELECT r FROM Resource r WHERE r.type IN :types AND r.status = 'AVAILABLE'")
    List<Resource> findByTypes(@Param("types") List<Resource.ResourceType> types);
}
