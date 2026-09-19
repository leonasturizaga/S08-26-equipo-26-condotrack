package com.condotrack.backend.repository;

import com.condotrack.backend.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StaffRepository extends JpaRepository<Staff, UUID> {

    Optional<Staff> findFirstByUser_IdAndBuilding_IdAndActiveTrue(
            UUID userId,
            UUID buildingId
    );
}
