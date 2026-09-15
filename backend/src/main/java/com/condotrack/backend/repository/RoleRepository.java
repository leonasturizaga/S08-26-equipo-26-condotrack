//----------- milestone 3 ---------------------
// package com.condotrack.backend.repository;

// import com.condotrack.backend.model.Role;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.Optional;
// import java.util.UUID;

// public interface RoleRepository extends JpaRepository<Role, UUID> {
//     Optional<Role> findByCode(String code);
// }


//----------- milestone 4 ---------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByCode(String code);

    @Query("""
            select case when count(p) > 0 then true else false end
            from Role r
            join r.permissions p
            where r.code in :roleCodes
              and p.code = :permissionCode
            """)
    boolean existsPermissionForRoles(
            @Param("roleCodes") Set<String> roleCodes,
            @Param("permissionCode") String permissionCode
    );
}
