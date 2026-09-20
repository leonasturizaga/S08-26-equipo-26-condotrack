//----------------- milestone 15 ---------------------
// package com.condotrack.backend.repository;

// import com.condotrack.backend.model.Staff;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.Optional;
// import java.util.UUID;

// public interface StaffRepository extends JpaRepository<Staff, UUID> {

//     Optional<Staff> findFirstByUser_IdAndBuilding_IdAndActiveTrue(
//             UUID userId,
//             UUID buildingId
//     );
// }

//----------------- milestone 16b ---------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffRepository extends JpaRepository<Staff, UUID> {

    Optional<Staff> findFirstByUser_IdAndBuilding_IdAndActiveTrue(
            UUID userId,
            UUID buildingId
    );

    Optional<Staff> findByIdAndActiveTrue(UUID id);

    List<Staff> findByBuildingIdAndActiveTrueOrderByUser_LastNameAscUser_FirstNameAsc(UUID buildingId);

    @Query("""
            SELECT s
            FROM Staff s
            WHERE s.active = true
              AND LOWER(s.user.email) = LOWER(:email)
            """)
    List<Staff> findActiveByUserEmailIgnoreCase(@Param("email") String email);

    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
            FROM Staff s
            WHERE s.id = :staffId
              AND s.active = true
              AND LOWER(s.user.email) = LOWER(:email)
            """)
    boolean existsActiveForUserAndId(
            @Param("email") String email,
            @Param("staffId") UUID staffId
    );
}
