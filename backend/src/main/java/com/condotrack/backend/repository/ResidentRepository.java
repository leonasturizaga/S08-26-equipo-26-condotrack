package com.condotrack.backend.repository;

import com.condotrack.backend.model.Resident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResidentRepository extends JpaRepository<Resident, UUID> {
    List<Resident> findByUnitIdAndActiveTrue(UUID unitId);
}
