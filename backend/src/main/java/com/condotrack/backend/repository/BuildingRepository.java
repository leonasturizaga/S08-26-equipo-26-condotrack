//------------------- milestone 8 ---------------------
// package com.condotrack.backend.repository;

// import com.condotrack.backend.model.Building;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.UUID;

// public interface BuildingRepository extends JpaRepository<Building, UUID> {
// }


//------------------- milestone 9 ---------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.Building;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BuildingRepository extends JpaRepository<Building, UUID> {

    Page<Building> findByActiveTrueOrderByNameAsc(Pageable pageable);

    java.util.Optional<Building> findByIdAndActiveTrue(UUID id);
}