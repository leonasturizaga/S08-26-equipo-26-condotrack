// //------------------- M24.1 --------------------
// //CommonAreaRepository.java
// package com.condotrack.backend.repository;

// import com.condotrack.backend.model.CommonArea;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.UUID;

// public interface CommonAreaRepository extends JpaRepository<CommonArea, UUID> {

//     Page<CommonArea> findByActiveTrueOrderByBuildingIdAscNameAsc(Pageable pageable);

//     Page<CommonArea> findByBuildingIdAndActiveTrueOrderByNameAsc(
//             UUID buildingId,
//             Pageable pageable
//     );

//     boolean existsByBuildingIdAndNameIgnoreCase(
//             UUID buildingId,
//             String name
//     );

//     boolean existsByBuildingIdAndNameIgnoreCaseAndIdNot(
//             UUID buildingId,
//             String name,
//             UUID id
//     );
// }


//------------------------- M24.6 -------------------------
//------------------- M24.1 --------------------
//CommonAreaRepository.java
package com.condotrack.backend.repository;

import com.condotrack.backend.model.CommonArea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface CommonAreaRepository extends JpaRepository<CommonArea, UUID> {

    Page<CommonArea> findByActiveTrueOrderByBuildingIdAscNameAsc(Pageable pageable);

    Page<CommonArea> findByBuildingIdAndActiveTrueOrderByNameAsc(
            UUID buildingId,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CommonArea c where c.id = :id")
    Optional<CommonArea> findByIdForUpdate(@Param("id") UUID id);

    boolean existsByBuildingIdAndNameIgnoreCase(
            UUID buildingId,
            String name
    );

    boolean existsByBuildingIdAndNameIgnoreCaseAndIdNot(
            UUID buildingId,
            String name,
            UUID id
    );
}
