//----------------- M17.1 ---------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.MaintenanceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, UUID> {

    Page<MaintenanceRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<MaintenanceRequest> findByRequestedByUser_EmailIgnoreCaseOrderByCreatedAtDesc(String email, Pageable pageable);

    Page<MaintenanceRequest> findByAssignedToStaff_User_EmailIgnoreCaseOrderByCreatedAtDesc(String email, Pageable pageable);

    @Query("""
            SELECT DISTINCT m
            FROM MaintenanceRequest m
            JOIN m.unit u
            JOIN u.residents r
            JOIN r.user user
            WHERE LOWER(user.email) = LOWER(:email)
              AND r.active = true
            ORDER BY m.createdAt DESC
            """)
    Page<MaintenanceRequest> findForOwnedUnits(@Param("email") String email, Pageable pageable);

    boolean existsByIdAndRequestedByUser_EmailIgnoreCase(UUID id, String email);

    boolean existsByIdAndAssignedToStaff_User_EmailIgnoreCase(UUID id, String email);

    @Query("select m.building.id from MaintenanceRequest m where m.id = :id")
    java.util.Optional<UUID> findBuildingIdById(@Param("id") UUID id);

    @Query("select m.building.id from MaintenanceRequest m where m.unit.id = :unitId")
    java.util.Optional<UUID> findBuildingIdByUnitId(@Param("unitId") UUID unitId);

    Page<MaintenanceRequest> findByBuilding_IdInOrderByCreatedAtDesc(java.util.Collection<UUID> buildingIds, Pageable pageable);
    @Query("""
            SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
            FROM MaintenanceRequest m
            JOIN m.unit u
            JOIN u.residents r
            JOIN r.user user
            WHERE m.id = :maintenanceId
              AND LOWER(user.email) = LOWER(:email)
              AND r.active = true
            """)
    boolean existsForOwnedUnit(@Param("maintenanceId") UUID maintenanceId, @Param("email") String email);

    List<MaintenanceRequest> findByUnitIdOrderByCreatedAtDesc(UUID unitId);
}