package com.condotrack.backend.repository;

import com.condotrack.backend.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {
    List<Incident> findByUnitIdOrderByCreatedAtDesc(UUID unitId);
}
