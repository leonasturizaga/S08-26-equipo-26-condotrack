//------------------ original PR32 ------------------------
// package com.condotrack.backend.repository;

// import com.condotrack.backend.model.CommonArea;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.List;
// import java.util.Optional;
// import java.util.UUID;

// public interface CommonAreaRepository extends JpaRepository<CommonArea, UUID> {

//     List<CommonArea> findByActiveTrueOrderByNameAsc();

//     Optional<CommonArea> findByIdAndActiveTrue(UUID id);

//     Optional<CommonArea> findByIdAndBuildingIdAndActiveTrue(
//             UUID id,
//             UUID buildingId
//     );
// }


//------------------ new PR32 M21 ------------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.CommonArea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommonAreaRepository extends JpaRepository<CommonArea, UUID> {

    Page<CommonArea> findByActiveTrueOrderByBuildingIdAscNameAsc(Pageable pageable);

    Page<CommonArea> findByBuildingIdAndActiveTrueOrderByNameAsc(UUID buildingId, Pageable pageable);
}
