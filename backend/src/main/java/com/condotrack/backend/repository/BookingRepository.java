//------------------ original PR32 M21 ------------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.Booking;
import com.condotrack.backend.model.Enums;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    
   List<Booking> findByUnitIdOrderByStartAtDesc(UUID unitId);
   
    Page<Booking> findAllByOrderByStartAtDesc(Pageable pageable);

    Page<Booking> findAllByResident_User_EmailIgnoreCaseOrderByStartAtDesc(
            String email,
            Pageable pageable
    );

    boolean existsByIdAndResident_User_EmailIgnoreCase(UUID bookingId, String email);

    boolean existsByCommonAreaIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
        UUID commonAreaId,
        java.util.Collection<Enums.BookingStatus> statuses,
        java.time.OffsetDateTime endAt,
        java.time.OffsetDateTime startAt
   );
}
