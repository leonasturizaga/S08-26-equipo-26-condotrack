package com.condotrack.backend.repository;

import com.condotrack.backend.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    List<Delivery> findByUnitIdAndStatusInOrderByReceivedAtDesc(UUID unitId, List<com.condotrack.backend.model.Enums.DeliveryStatus> statuses);
}
