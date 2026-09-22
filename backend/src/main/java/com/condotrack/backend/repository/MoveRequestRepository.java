//---------------- milestone 17 --------------
// package com.condotrack.backend.repository;

// import com.condotrack.backend.model.Enums.MoveRequestStatus;
// import com.condotrack.backend.model.MoveRequest;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.List;
// import java.util.UUID;

// public interface MoveRequestRepository extends JpaRepository<MoveRequest, UUID> {
//     List<MoveRequest> findByUnitIdAndStatusNotInOrderByRequestedAtDesc(UUID unitId, List<MoveRequestStatus> excludedStatuses);
// }


//---------------------- milestone 18 ----------------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.Enums.MoveRequestStatus;
import com.condotrack.backend.model.MoveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Collection;
import java.util.UUID;

public interface MoveRequestRepository extends JpaRepository<MoveRequest, UUID> {

    List<MoveRequest> findByUnitIdAndStatusNotInOrderByRequestedAtDesc(UUID unitId, List<MoveRequestStatus> excludedStatuses);

    Page<MoveRequest> findAllByOrderByRequestedAtDesc(Pageable pageable);

    Page<MoveRequest> findByUnit_Building_IdInOrderByRequestedAtDesc(Collection<UUID> buildingIds, Pageable pageable);

    Page<MoveRequest> findByResident_User_EmailIgnoreCaseOrderByRequestedAtDesc(String email, Pageable pageable);

    Page<MoveRequest> findByUnit_IdInOrderByRequestedAtDesc(Collection<UUID> unitIds, Pageable pageable);

    @Query("select m.building.id from MoveRequest m where m.id = :id")
    java.util.Optional<UUID> findBuildingIdById(@Param("id") UUID id);

    @Query("select m.unit.id from MoveRequest m where m.id = :id")
    java.util.Optional<UUID> findUnitIdById(@Param("id") UUID id);

    boolean existsByIdAndResident_User_EmailIgnoreCase(UUID id, String email);

    boolean existsByIdAndUnit_Id(UUID id, UUID unitId);
}