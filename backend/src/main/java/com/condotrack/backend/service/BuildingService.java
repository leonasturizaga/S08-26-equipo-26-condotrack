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
// package com.condotrack.backend.service;

// import com.condotrack.backend.dto.BuildingPageResponse;
// import com.condotrack.backend.dto.BuildingResponse;
// import com.condotrack.backend.model.Building;
// import com.condotrack.backend.repository.BuildingRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.UUID;

// @Service
// @RequiredArgsConstructor
// public class BuildingService {

//     private static final int MAX_PAGE_SIZE = 100;

//     private final BuildingRepository buildingRepository;

//     @Transactional(readOnly = true)
//     public BuildingPageResponse getBuildings(int page, int size) {
//         Pageable pageable = createPageable(page, size);
//         Page<BuildingResponse> buildings = buildingRepository
//                 .findByActiveTrueOrderByNameAsc(pageable)
//                 .map(this::toResponse);

//         return BuildingPageResponse.from(buildings);
//     }

//     @Transactional(readOnly = true)
//     public BuildingResponse getBuilding(UUID buildingId) {
//         Building building = buildingRepository.findByIdAndActiveTrue(buildingId)
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Building not found: " + buildingId
//                 ));

//         return toResponse(building);
//     }

//     private Pageable createPageable(int page, int size) {
//         int safePage = Math.max(page, 0);
//         int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
//         return PageRequest.of(safePage, safeSize);
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


//---------------- milestone 10 ---------------------
package com.condotrack.backend.service;

import com.condotrack.backend.dto.BuildingCreateRequest;
import com.condotrack.backend.dto.BuildingPageResponse;
import com.condotrack.backend.dto.BuildingResponse;
import com.condotrack.backend.dto.BuildingUpdateRequest;
import com.condotrack.backend.model.Building;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.BuildingRepository;
import com.condotrack.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private static final int MAX_PAGE_SIZE = 100;

    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;

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
                .orElseThrow(() -> new IllegalArgumentException("Building not found: " + buildingId));
														   
				   

        return toResponse(building);
    }

    @Transactional
    public BuildingResponse createBuilding(
            BuildingCreateRequest request,
            Authentication authentication
    ) {
        String normalizedCode = normalizeCode(request.code());

        if (buildingRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new IllegalStateException("A building with this code already exists");
        }

        User currentUser = getAuthenticatedUser(authentication);

        Building building = new Building();
        building.setName(normalizeRequired(request.name()));
        building.setCode(normalizedCode);
        building.setAddressLine1(normalizeRequired(request.addressLine1()));
        building.setAddressLine2(normalizeOptional(request.addressLine2()));
        building.setCity(normalizeRequired(request.city()));
        building.setStateProvince(normalizeOptional(request.stateProvince()));
        building.setPostalCode(normalizeOptional(request.postalCode()));
        building.setCountry(normalizeRequired(request.country()));
        building.setTimezone(normalizeOptional(request.timezone()) == null
                ? "UTC"
                : normalizeOptional(request.timezone()));
        building.setActive(true);
        building.setUpdatedBy(currentUser.getId());

        return toResponse(buildingRepository.save(building));
    }

    @Transactional
    public BuildingResponse updateBuilding(
            UUID buildingId,
            BuildingUpdateRequest request,
            Authentication authentication
    ) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new IllegalArgumentException("Building not found: " + buildingId));

        String normalizedCode = normalizeCode(request.code());

        if (buildingRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, buildingId)) {
            throw new IllegalStateException("A building with this code already exists");
        }

        User currentUser = getAuthenticatedUser(authentication);

        building.setName(normalizeRequired(request.name()));
        building.setCode(normalizedCode);
        building.setAddressLine1(normalizeRequired(request.addressLine1()));
        building.setAddressLine2(normalizeOptional(request.addressLine2()));
        building.setCity(normalizeRequired(request.city()));
        building.setStateProvince(normalizeOptional(request.stateProvince()));
        building.setPostalCode(normalizeOptional(request.postalCode()));
        building.setCountry(normalizeRequired(request.country()));
        building.setTimezone(normalizeOptional(request.timezone()) == null
                ? "UTC"
                : normalizeOptional(request.timezone()));
        building.setActive(request.active());
        building.setUpdatedBy(currentUser.getId());

        return toResponse(buildingRepository.save(building));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authenticated user is required");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
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

    private String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeCode(String value) {
        return normalizeRequired(value).toUpperCase();
    }
}