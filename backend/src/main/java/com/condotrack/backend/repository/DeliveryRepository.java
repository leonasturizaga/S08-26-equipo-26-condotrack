//------------- milestone 14 ---------------------
// package com.condotrack.backend.repository;

// import com.condotrack.backend.model.Delivery;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.List;
// import java.util.UUID;

// public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
//     List<Delivery> findByUnitIdAndStatusInOrderByReceivedAtDesc(UUID unitId, List<com.condotrack.backend.model.Enums.DeliveryStatus> statuses);
// }


//------------- milestone 15 ---------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    Page<Delivery> findAllByOrderByReceivedAtDesc(Pageable pageable);

    Page<Delivery> findByResident_User_EmailIgnoreCaseOrderByReceivedAtDesc(
            String email,
            Pageable pageable
    );

    Optional<Delivery> findByIdAndResident_User_EmailIgnoreCase(
            UUID id,
            String email
    );

    boolean existsByIdAndResident_User_EmailIgnoreCase(UUID id, String email);

    List<Delivery> findByUnitIdAndStatusInOrderByReceivedAtDesc(
            UUID unitId,
            List<com.condotrack.backend.model.Enums.DeliveryStatus> statuses
    );
}