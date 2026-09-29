//------------------- milestone 11 ---------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.Building;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BuildingRepository extends JpaRepository<Building, UUID> {

    Page<Building> findByActiveTrueOrderByNameAsc(Pageable pageable);

    Page<Building> findAllByOrderByNameAsc(Pageable pageable);

    java.util.Optional<Building> findByIdAndActiveTrue(UUID id);

    boolean existsByIdAndActiveTrue(UUID id);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);
}