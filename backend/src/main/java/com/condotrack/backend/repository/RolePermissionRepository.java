package com.condotrack.backend.repository;

import com.condotrack.backend.model.RolePermission;
import com.condotrack.backend.model.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    @Query("""
            SELECT rp
            FROM RolePermission rp
            JOIN FETCH rp.role r
            JOIN FETCH rp.permission p
            WHERE r.code IN :roleCodes
            """)
    List<RolePermission> findByRoleCodes(@Param("roleCodes") Set<String> roleCodes);

    @Query("""
            SELECT CASE WHEN COUNT(rp) > 0 THEN TRUE ELSE FALSE END
            FROM RolePermission rp
            JOIN rp.role r
            JOIN rp.permission p
            WHERE r.code = :roleCode
              AND p.code = :permissionCode
              AND rp.active = TRUE
            """)
    boolean existsActiveForRoleAndPermission(
            @Param("roleCode") String roleCode,
            @Param("permissionCode") String permissionCode
    );
}
