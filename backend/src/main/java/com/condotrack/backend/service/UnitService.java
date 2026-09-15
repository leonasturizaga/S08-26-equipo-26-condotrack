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
package com.condotrack.backend.service;

import com.condotrack.backend.dto.UnitPageResponse;
import com.condotrack.backend.dto.UnitResponse;
import com.condotrack.backend.model.Unit;
import com.condotrack.backend.repository.UnitRepository;
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
            units = unitRepository.findByActiveTrueOrderByBuildingIdAscUnitNumberAsc(pageable);
        } else if (permissionService.hasPermission(authentication, "UNITS_VIEW_OWN")) {
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
}