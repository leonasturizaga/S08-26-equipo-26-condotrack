//-------------- milestone 8 ---------------------
// package com.condotrack.backend.service;

// import com.condotrack.backend.dto.BuildingResponse;
// import com.condotrack.backend.model.Building;
// import com.condotrack.backend.repository.BuildingRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.List;

// @Service
// @RequiredArgsConstructor
// public class BuildingService {

//     private final BuildingRepository buildingRepository;

//     @Transactional(readOnly = true)
//     public List<BuildingResponse> getAllBuildings() {
//         return buildingRepository.findAll().stream()
//                 .map(this::toResponse)
//                 .toList();
//     }

//     private BuildingResponse toResponse(Building building) {
//         return new BuildingResponse(
//                 building.getId(),
//                 building.getName(),
//                 building.getCode(),
//                 building.getAddressLine1(),
//                 building.getAddressLine2(),
//                 building.getCity(),
//                 building.getStateProvince(),
//                 building.getPostalCode(),
//                 building.getCountry(),
//                 building.getTimezone(),
//                 building.isActive()
//         );
//     }
// }


//---------------- milestone 9 ---------------------
package com.condotrack.backend.service;

import com.condotrack.backend.dto.BuildingPageResponse;
import com.condotrack.backend.dto.BuildingResponse;
import com.condotrack.backend.model.Building;
import com.condotrack.backend.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private static final int MAX_PAGE_SIZE = 100;

    private final BuildingRepository buildingRepository;

    @Transactional(readOnly = true)
    public BuildingPageResponse getBuildings(int page, int size) {
        Pageable pageable = createPageable(page, size);
        Page<BuildingResponse> buildings = buildingRepository
                .findByActiveTrueOrderByNameAsc(pageable)
                .map(this::toResponse);

        return BuildingPageResponse.from(buildings);
    }

    @Transactional(readOnly = true)
    public BuildingResponse getBuilding(UUID buildingId) {
        Building building = buildingRepository.findByIdAndActiveTrue(buildingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Building not found: " + buildingId
                ));

        return toResponse(building);
    }

    private Pageable createPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize);
    }

    private BuildingResponse toResponse(Building building) {
        return new BuildingResponse(
                building.getId(),
                building.getName(),
                building.getCode(),
                building.getAddressLine1(),
                building.getAddressLine2(),
                building.getCity(),
                building.getStateProvince(),
                building.getPostalCode(),
                building.getCountry(),
                building.getTimezone(),
                building.isActive()
        );
    }
}