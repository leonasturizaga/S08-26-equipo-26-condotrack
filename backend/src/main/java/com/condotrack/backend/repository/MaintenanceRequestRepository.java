package com.condotrack.backend.repository;

import com.condotrack.backend.model.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, UUID> {
    List<MaintenanceRequest> findByUnitIdOrderByCreatedAtDesc(UUID unitId);
}
