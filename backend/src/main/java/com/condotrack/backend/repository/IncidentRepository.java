//----------------- milestone 17.1 ---------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.UUID;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {
    List<Incident> findByUnitIdOrderByCreatedAtDesc(UUID unitId);

    Page<Incident> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Incident> findByReportedByUser_EmailIgnoreCaseOrderByCreatedAtDesc(
            String email,
            Pageable pageable
    );

    Page<Incident> findByAssignedToStaff_User_EmailIgnoreCaseOrderByCreatedAtDesc(
            String email,
            Pageable pageable
    );
    @Query("""
            SELECT DISTINCT i
            FROM Incident i
            JOIN i.unit u
            JOIN u.residents r
            JOIN r.user user
            WHERE LOWER(user.email) = LOWER(:email)
              AND r.active = true
            ORDER BY i.createdAt DESC
            """)
    Page<Incident> findForOwnedUnits(
            @Param("email") String email,
            Pageable pageable
    );

    boolean existsByIdAndReportedByUser_EmailIgnoreCase(UUID id, String email);

    boolean existsByIdAndAssignedToStaff_User_EmailIgnoreCase(UUID id, String email);

    @Query("select i.building.id from Incident i where i.id = :id")
    java.util.Optional<UUID> findBuildingIdById(@Param("id") UUID id);

    @Query("select i.building.id from Incident i where i.unit.id = :unitId")
    java.util.Optional<UUID> findBuildingIdByUnitId(@Param("unitId") UUID unitId);

    Page<Incident> findByBuilding_IdInOrderByCreatedAtDesc(java.util.Collection<UUID> buildingIds, Pageable pageable);
    @Query("""
            SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END
            FROM Incident i
            JOIN i.unit u
            JOIN u.residents r
            JOIN r.user user
            WHERE i.id = :incidentId
              AND LOWER(user.email) = LOWER(:email)
              AND r.active = true
            """)
    boolean existsForOwnedUnit(
            @Param("incidentId") UUID incidentId,
            @Param("email") String email
    );

}
