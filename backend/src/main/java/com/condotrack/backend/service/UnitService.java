//-------------- milestone 7 ---------------------
// package com.condotrack.backend.service;

// import com.condotrack.backend.dto.UnitResponse;
// import com.condotrack.backend.model.Unit;
// import com.condotrack.backend.repository.UnitRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.UUID;

// @Service
// @RequiredArgsConstructor
// public class UnitService {

//     private final UnitRepository unitRepository;

//     @Transactional(readOnly = true)
//     public UnitResponse getUnit(UUID unitId) {
//         Unit unit = unitRepository.findById(unitId)
//                 .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + unitId));

//         return new UnitResponse(
//                 unit.getId(),
//                 unit.getBuilding().getId(),
//                 unit.getUnitNumber(),
//                 unit.getFloorNumber(),
//                 unit.getUnitType().name(),
//                 unit.isActive()
//         );
//     }
// }


//---------------- milestone 8 ---------------------
// package com.condotrack.backend.service;

// import com.condotrack.backend.dto.UnitPageResponse;
// import com.condotrack.backend.dto.UnitResponse;
// import com.condotrack.backend.model.Unit;
// import com.condotrack.backend.repository.UnitRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.security.core.Authentication;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.UUID;

// @Service
// @RequiredArgsConstructor
// public class UnitService {

//     private static final int MAX_PAGE_SIZE = 100;

//     private final UnitRepository unitRepository;
//     private final PermissionService permissionService;

//     @Transactional(readOnly = true)
//     public UnitResponse getUnit(UUID unitId) {
//         Unit unit = unitRepository.findById(unitId)
//                 .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + unitId));

//         return toResponse(unit);
//     }

//     @Transactional(readOnly = true)
//     public UnitPageResponse getUnits(Authentication authentication, int page, int size) {
//         Pageable pageable = createPageable(page, size);
//         Page<Unit> units;

//         if (permissionService.hasPermission(authentication, "UNITS_VIEW")) {
//             units = unitRepository.findByActiveTrueOrderByBuildingIdAscUnitNumberAsc(pageable);
//         } else if (permissionService.hasPermission(authentication, "UNITS_VIEW_OWN")) {
//             units = unitRepository.findActiveUnitsForUser(
//                     authentication.getName(),
//                     pageable
//             );
//         } else {
//             throw new org.springframework.security.access.AccessDeniedException(
//                     "Insufficient permissions"
//             );
//         }

//         return UnitPageResponse.from(units.map(this::toResponse));
//     }

//     private Pageable createPageable(int page, int size) {
//         int safePage = Math.max(page, 0);
//         int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

//         return PageRequest.of(safePage, safeSize);
//     }

//     private UnitResponse toResponse(Unit unit) {
//         return new UnitResponse(
//                 unit.getId(),
//                 unit.getBuilding().getId(),
//                 unit.getUnitNumber(),
//                 unit.getFloorNumber(),
//                 unit.getUnitType().name(),
//                 unit.isActive()
//         );
//     }
// }


//---------------- milestone 11 ---------------------
package com.condotrack.backend.service;

import com.condotrack.backend.dto.UnitCreateRequest;
import com.condotrack.backend.dto.UnitPageResponse;
import com.condotrack.backend.dto.UnitResponse;
import com.condotrack.backend.dto.UnitUpdateRequest;
import com.condotrack.backend.model.Building;
import com.condotrack.backend.model.Enums;
import com.condotrack.backend.model.Unit;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.BuildingRepository;
import com.condotrack.backend.repository.UnitRepository;
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
public class UnitService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UnitRepository unitRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Transactional(readOnly = true)
    public UnitResponse getUnit(UUID unitId) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + unitId));

        return toResponse(unit);
    }

    @Transactional(readOnly = true)
    public UnitPageResponse getUnits(Authentication authentication, int page, int size) {
        Pageable pageable = createPageable(page, size);
        Page<Unit> units;

        if (permissionService.hasPermission(authentication, "UNITS_VIEW")) {
//            units = unitRepository.findAllByOrderByBuildingIdAscUnitNumberAsc(pageable);
            units = unitRepository.findByActiveTrueOrderByBuildingIdAscUnitNumberAsc(pageable);
        } else if (permissionService.hasPermission(authentication, "UNITS_VIEW_OWN")) {
//            units = unitRepository.findUnitsForUser(
            units = unitRepository.findActiveUnitsForUser(
                    authentication.getName(),
                    pageable
            );
        } else {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Insufficient permissions"
            );
        }

        return UnitPageResponse.from(units.map(this::toResponse));
    }

    @Transactional
    public UnitResponse createUnit(
            UnitCreateRequest request,
            Authentication authentication
    ) {
        Building building = getActiveBuilding(request.buildingId());
        String normalizedUnitNumber = normalizeRequired(request.unitNumber());
        Enums.UnitType unitType = parseUnitType(request.unitType());

        if (unitRepository.existsByBuildingIdAndUnitNumberIgnoreCase(
                building.getId(), normalizedUnitNumber)) {
            throw new IllegalStateException(
                    "A unit with this number already exists in the selected building"
            );
        }

        User currentUser = getAuthenticatedUser(authentication);

        Unit unit = new Unit();
        unit.setBuilding(building);
        unit.setUnitNumber(normalizedUnitNumber);
        unit.setFloorNumber(request.floorNumber());
        unit.setUnitType(unitType);
        unit.setActive(true);
        unit.setUpdatedBy(currentUser.getId());

        return toResponse(unitRepository.save(unit));
    }

    @Transactional
    public UnitResponse updateUnit(
            UUID unitId,
            UnitUpdateRequest request,
            Authentication authentication
    ) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + unitId));

        Building building = getActiveBuilding(request.buildingId());
        String normalizedUnitNumber = normalizeRequired(request.unitNumber());
        Enums.UnitType unitType = parseUnitType(request.unitType());

        if (unitRepository.existsByBuildingIdAndUnitNumberIgnoreCaseAndIdNot(
                building.getId(),
                normalizedUnitNumber,
                unitId)) {
            throw new IllegalStateException(
                    "A unit with this number already exists in the selected building"
            );
        }

        User currentUser = getAuthenticatedUser(authentication);

        unit.setBuilding(building);
        unit.setUnitNumber(normalizedUnitNumber);
        unit.setFloorNumber(request.floorNumber());
        unit.setUnitType(unitType);
        unit.setActive(request.active());
        unit.setUpdatedBy(currentUser.getId());

        return toResponse(unitRepository.save(unit));
    }

    private Building getActiveBuilding(UUID buildingId) {
        return buildingRepository.findByIdAndActiveTrue(buildingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Active building not found: " + buildingId
                ));
    }

    private Enums.UnitType parseUnitType(String value) {
        if (value == null || value.isBlank()) {
            return Enums.UnitType.RESIDENTIAL;
        }

        try {
            return Enums.UnitType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid unitType. Allowed values: RESIDENTIAL, COMMERCIAL, PARKING, STORAGE, OTHER"
            );
        }
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

    private UnitResponse toResponse(Unit unit) {
        return new UnitResponse(
                unit.getId(),
                unit.getBuilding().getId(),
                unit.getUnitNumber(),
                unit.getFloorNumber(),
                unit.getUnitType().name(),
                unit.isActive()
        );
    }

    private String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }
}