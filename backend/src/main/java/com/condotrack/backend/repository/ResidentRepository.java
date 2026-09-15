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
}
