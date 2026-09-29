//------------------ new PR32 M21 ------------------------
// package com.condotrack.backend.repository;

// import com.condotrack.backend.model.CommonArea;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.UUID;

// public interface CommonAreaRepository extends JpaRepository<CommonArea, UUID> {

//     Page<CommonArea> findByActiveTrueOrderByBuildingIdAscNameAsc(Pageable pageable);

//     Page<CommonArea> findByBuildingIdAndActiveTrueOrderByNameAsc(UUID buildingId, Pageable pageable);
// }

//------------------- M24.1 --------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.CommonArea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommonAreaRepository extends JpaRepository<CommonArea, UUID> {

    Page<CommonArea> findByActiveTrueOrderByBuildingIdAscNameAsc(Pageable pageable);

    Page<CommonArea> findByBuildingIdAndActiveTrueOrderByNameAsc(
            UUID buildingId,
            Pageable pageable
    );

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
