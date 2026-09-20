//-------------- milestone 7 ---------------------
// package com.condotrack.backend.repository;

// import com.condotrack.backend.model.Unit;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.List;
// import java.util.UUID;

// public interface UnitRepository extends JpaRepository<Unit, UUID> {
//     List<Unit> findByBuildingIdOrderByUnitNumber(UUID buildingId);
// }


//------------------ milestone 8 ---------------------
// package com.condotrack.backend.repository;

// import com.condotrack.backend.model.Unit;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// import java.util.List;
// import java.util.UUID;

// public interface UnitRepository extends JpaRepository<Unit, UUID> {

//     List<Unit> findByBuildingIdOrderByUnitNumber(UUID buildingId);

//     Page<Unit> findByActiveTrueOrderByBuildingIdAscUnitNumberAsc(Pageable pageable);

//     @Query("""
//             SELECT DISTINCT u
//             FROM Unit u
//             JOIN u.residents r
//             JOIN r.user user
//             WHERE LOWER(user.email) = LOWER(:email)
//               AND r.active = true
//               AND u.active = true
//             ORDER BY u.building.id ASC, u.unitNumber ASC
//             """)
//     Page<Unit> findActiveUnitsForUser(
//             @Param("email") String email,
//             Pageable pageable
//     );
// }


//------------------ milestone 11 ---------------------
// package com.condotrack.backend.repository;

// import com.condotrack.backend.model.Unit;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// import java.util.List;
// import java.util.UUID;

// public interface UnitRepository extends JpaRepository<Unit, UUID> {

//     List<Unit> findByBuildingIdOrderByUnitNumber(UUID buildingId);

//     Page<Unit> findByActiveTrueOrderByBuildingIdAscUnitNumberAsc(Pageable pageable);

//     Page<Unit> findAllByOrderByBuildingIdAscUnitNumberAsc(Pageable pageable);

//     @Query("""
//             SELECT DISTINCT u
//             FROM Unit u
//             JOIN u.residents r
//             JOIN r.user user
//             WHERE LOWER(user.email) = LOWER(:email)
//               AND r.active = true
           
//             ORDER BY u.building.id ASC, u.unitNumber ASC
//             """)
//     Page<Unit> findUnitsForUser(
//             @Param("email") String email,
//             Pageable pageable
//     );

//     boolean existsByBuildingIdAndUnitNumberIgnoreCase(UUID buildingId, String unitNumber);

//     boolean existsByBuildingIdAndUnitNumberIgnoreCaseAndIdNot(
//             UUID buildingId,
//             String unitNumber,
//             UUID id
//     );
// }


//------------------ milestone 16b ---------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.Unit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UnitRepository extends JpaRepository<Unit, UUID> {

    List<Unit> findByBuildingIdOrderByUnitNumber(UUID buildingId);

    List<Unit> findByActiveTrueOrderByBuildingIdAscUnitNumberAsc();

    Page<Unit> findByActiveTrueOrderByBuildingIdAscUnitNumberAsc(Pageable pageable);

    @Query("""
            SELECT DISTINCT u
            FROM Unit u
            JOIN u.residents r
            JOIN r.user user
            WHERE LOWER(user.email) = LOWER(:email)
              AND r.active = true
              AND u.active = true
            ORDER BY u.building.id ASC, u.unitNumber ASC
            """)
    Page<Unit> findActiveUnitsForUser(
            @Param("email") String email,
            Pageable pageable
    );

    Page<Unit> findAllByOrderByBuildingIdAscUnitNumberAsc(Pageable pageable);

    @Query("""
            SELECT DISTINCT u
            FROM Unit u
            JOIN u.residents r
            JOIN r.user user
            WHERE LOWER(user.email) = LOWER(:email)
              AND r.active = true
           
            ORDER BY u.building.id ASC, u.unitNumber ASC
            """)
    Page<Unit> findUnitsForUser(
            @Param("email") String email,
            Pageable pageable
    );

    boolean existsByBuildingIdAndUnitNumberIgnoreCase(UUID buildingId, String unitNumber);

    boolean existsByBuildingIdAndUnitNumberIgnoreCaseAndIdNot(
            UUID buildingId,
            String unitNumber,
            UUID id
    );
}