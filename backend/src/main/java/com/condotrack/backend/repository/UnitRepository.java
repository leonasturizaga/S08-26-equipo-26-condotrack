package com.condotrack.backend.repository;

import com.condotrack.backend.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UnitRepository extends JpaRepository<Unit, UUID> {
    List<Unit> findByBuildingIdOrderByUnitNumber(UUID buildingId);
}
