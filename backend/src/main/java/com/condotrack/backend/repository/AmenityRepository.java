package com.condotrack.backend.repository;

import com.condotrack.backend.model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AmenityRepository extends JpaRepository<Amenity, UUID> {

    List<Amenity> findAllByActiveTrueOrderByNameAsc();

    List<Amenity> findAllByIdInAndActiveTrue(Iterable<UUID> ids);
}
