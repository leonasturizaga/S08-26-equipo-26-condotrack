package com.condotrack.backend.repository;

import com.condotrack.backend.model.BuildingRolePermission;
import com.condotrack.backend.model.BuildingRolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface BuildingRolePermissionRepository extends JpaRepository<BuildingRolePermission, BuildingRolePermissionId> {

    @Query("""
            SELECT brp
            FROM BuildingRolePermission brp
            JOIN FETCH brp.role r
            JOIN FETCH brp.permission p
            WHERE brp.building.id = :buildingId
              AND r.code IN :roleCodes
            """)
    List<BuildingRolePermission> findByBuildingIdAndRoleCodes(
            @Param("buildingId") UUID buildingId,
            @Param("roleCodes") Set<String> roleCodes
    );

    @Query("""
            SELECT brp
            FROM BuildingRolePermission brp
            JOIN FETCH brp.role r
            JOIN FETCH brp.permission p
            JOIN FETCH brp.building b
            WHERE b.id = :buildingId
            ORDER BY r.code, p.code
            """)
    List<BuildingRolePermission> findAllForBuilding(@Param("buildingId") UUID buildingId);
}
