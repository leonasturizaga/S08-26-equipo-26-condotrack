//----------------- M17 ---------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.Enums;
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

    List<Staff> findByBuildingIdAndActiveTrueAndStaffTypeOrderByUser_LastNameAscUser_FirstNameAsc(
            UUID buildingId, Enums.StaffType staffType);

    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
            FROM Staff s
            WHERE s.active = true
              AND LOWER(s.user.email) = LOWER(:email)
              AND s.building.id = :buildingId
            """)
    boolean existsActiveByUserEmailAndBuildingId(
            @Param("email") String email,
            @Param("buildingId") UUID buildingId
    );
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
