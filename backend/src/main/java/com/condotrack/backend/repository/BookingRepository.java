package com.condotrack.backend.repository;

import com.condotrack.backend.model.Booking;
import com.condotrack.backend.model.Enums;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByUnitIdOrderByStartAtDesc(UUID unitId);

    List<Booking> findAllByOrderByStartAtDesc();

    List<Booking> findByResident_User_EmailIgnoreCaseOrderByStartAtDesc(String email);

    Optional<Booking> findByIdAndResident_User_EmailIgnoreCase(UUID bookingId, String email);

    boolean existsByIdAndResident_User_EmailIgnoreCase(UUID bookingId,String email);

    List<Booking> findByCommonAreaIdAndStartAtLessThanAndEndAtGreaterThanAndStatusIn(
            UUID commonAreaId,
            java.time.OffsetDateTime endAt,
            java.time.OffsetDateTime startAt,
            List<Enums.BookingStatus> statuses
    );
}
