//------------------ milestone 4 ---------------------
// package com.condotrack.backend.repository;

// import com.condotrack.backend.model.Resident;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.List;
// import java.util.UUID;

// public interface ResidentRepository extends JpaRepository<Resident, UUID> {
//     List<Resident> findByUnitIdAndActiveTrue(UUID unitId);
// }


// //------------------ milestone 5 ---------------------
// package com.condotrack.backend.repository;

// import com.condotrack.backend.model.Resident;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// import java.util.List;
// import java.util.UUID;

// public interface ResidentRepository extends JpaRepository<Resident, UUID> {

//     List<Resident> findByUnitIdAndActiveTrue(UUID unitId);

//     @Query("""
//             SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
//             FROM Resident r
//             WHERE LOWER(r.user.email) = LOWER(:email)
//               AND r.unit.id = :unitId
//               AND r.active = true
//             """)
//     boolean existsActiveResidentForUserAndUnit(
//             @Param("email") String email,
//             @Param("unitId") UUID unitId
//     );
// }

//------------------ milestone 6 ---------------------
// package com.condotrack.backend.repository;

// import com.condotrack.backend.model.Resident;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// import java.util.List;
// import java.util.UUID;

// public interface ResidentRepository extends JpaRepository<Resident, UUID> {

//     List<Resident> findByUnitIdAndActiveTrue(UUID unitId);

//     @Query("""
//             SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
//             FROM Resident r
//             WHERE LOWER(r.user.email) = LOWER(:email)
//               AND r.unit.id = :unitId
//               AND r.active = true
//             """)
//     boolean existsActiveResidentForUserAndUnit(
//             @Param("email") String email,
//             @Param("unitId") UUID unitId
//     );

//     @Query("""
//             SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
//             FROM Resident r
//             WHERE LOWER(r.user.email) = LOWER(:email)
//               AND r.id = :residentId
//               AND r.active = true
//             """)
//     boolean existsActiveResidentForUserAndResidentId(
//             @Param("email") String email,
//             @Param("residentId") UUID residentId
//     );
// }

//----------------- milestone 7 ---------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.Resident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ResidentRepository extends JpaRepository<Resident, UUID> {

    List<Resident> findByUnitIdAndActiveTrue(UUID unitId);
   //---- milestone 14.1 ------
    List<Resident> findAllByUser_EmailIgnoreCaseAndActiveTrue(String email);

    Page<Resident> findByActiveTrue(Pageable pageable);

    Page<Resident> findByUser_EmailIgnoreCaseAndActiveTrue(String email, Pageable pageable);

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Resident r
            WHERE LOWER(r.user.email) = LOWER(:email)
              AND r.unit.id = :unitId
              AND r.active = true
            """)
    boolean existsActiveResidentForUserAndUnit(
            @Param("email") String email,
            @Param("unitId") UUID unitId
    );

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Resident r
            WHERE LOWER(r.user.email) = LOWER(:email)
              AND r.id = :residentId
              AND r.active = true
            """)
    boolean existsActiveResidentForUserAndResidentId(
            @Param("email") String email,
            @Param("residentId") UUID residentId
    );

	// milestone 18
    @Query("""
            SELECT DISTINCT r.unit.id
            FROM Resident r
            WHERE LOWER(r.user.email) = LOWER(:email)
              AND r.active = true
              AND r.unit.active = true
            """)
    List<UUID> findActiveUnitIdsForUser(@Param("email") String email);

	// milestone 18
    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Resident r
            WHERE LOWER(r.user.email) = LOWER(:email)
              AND r.unit.id = :unitId
              AND r.active = true
              AND r.residentType = com.condotrack.backend.model.Enums.ResidentType.OWNER
            """)
    boolean existsActiveOwnerForUserAndUnit(
            @Param("email") String email,
            @Param("unitId") UUID unitId
    );

    // milestone 15 Additional method to check if a resident with a specific ID belongs to a specific unit and is active
        boolean existsByIdAndUnitIdAndActiveTrue(UUID residentId, UUID unitId);
}
