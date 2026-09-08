package com.condotrack.backend.repository;

import com.condotrack.backend.model.Building;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BuildingRepository extends JpaRepository<Building, UUID> {
}
