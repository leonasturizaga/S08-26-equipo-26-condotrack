//------------------ new PR32 M21 ------------------------
// package com.condotrack.backend.service;

// import com.condotrack.backend.dto.CommonAreaResponse;
// import com.condotrack.backend.model.CommonArea;
// import com.condotrack.backend.repository.CommonAreaRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.UUID;

// @Service
// @RequiredArgsConstructor
// public class CommonAreaService {

//     private static final int MAX_PAGE_SIZE = 100;

//     private final CommonAreaRepository commonAreaRepository;

//     @Transactional(readOnly = true)
//     public Page<CommonAreaResponse> getCommonAreas(UUID buildingId, int page, int size) {
//         Pageable pageable = PageRequest.of(
//                 Math.max(page, 0),
//                 Math.min(Math.max(size, 1), MAX_PAGE_SIZE)
//         );

//         Page<CommonArea> areas = buildingId == null
//                 ? commonAreaRepository.findByActiveTrueOrderByBuildingIdAscNameAsc(pageable)
//                 : commonAreaRepository.findByBuildingIdAndActiveTrueOrderByNameAsc(buildingId, pageable);

//         return areas.map(this::toResponse);
//     }

//     @Transactional(readOnly = true)
//     public CommonAreaResponse getCommonArea(UUID commonAreaId) {
//         CommonArea area = commonAreaRepository.findById(commonAreaId)
//                 .filter(CommonArea::isActive)
//                 .orElseThrow(() -> new IllegalArgumentException("Common area not found: " + commonAreaId));
//         return toResponse(area);
//     }

//     private CommonAreaResponse toResponse(CommonArea area) {
//         return new CommonAreaResponse(
//                 area.getId(),
//                 area.getBuilding().getId(),
//                 area.getBuilding().getCode(),
//                 area.getName(),
//                 area.getAreaType(),
//                 area.getCapacity(),
//                 area.isBookingRequired(),
//                 area.isActive(),
//                 area.getBookingDurationMinutes()
//         );
//     }
// }


//---------------------- M24.1 ------------------------------
package com.condotrack.backend.service;

import com.condotrack.backend.dto.CommonAreaCreateRequest;
import com.condotrack.backend.dto.CommonAreaResponse;
import com.condotrack.backend.dto.CommonAreaUpdateRequest;
import com.condotrack.backend.model.Building;
import com.condotrack.backend.model.CommonArea;
import com.condotrack.backend.repository.BuildingRepository;
import com.condotrack.backend.repository.CommonAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommonAreaService {

    private static final int MAX_PAGE_SIZE = 100;

    private final CommonAreaRepository commonAreaRepository;
    private final BuildingRepository buildingRepository;

    @Transactional(readOnly = true)
    public Page<CommonAreaResponse> getCommonAreas(
            UUID buildingId,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE)
        );

        Page<CommonArea> areas = buildingId == null
                ? commonAreaRepository.findByActiveTrueOrderByBuildingIdAscNameAsc(pageable)
                : commonAreaRepository.findByBuildingIdAndActiveTrueOrderByNameAsc(
                        buildingId,
                        pageable
                );

        return areas.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CommonAreaResponse getCommonArea(UUID commonAreaId) {
        CommonArea area = commonAreaRepository.findById(commonAreaId)
                .filter(CommonArea::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Common area not found: " + commonAreaId
                ));

        return toResponse(area);
    }

    @Transactional
    public CommonAreaResponse createCommonArea(
            CommonAreaCreateRequest request
    ) {
        Building building = buildingRepository.findById(request.buildingId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Building not found: " + request.buildingId()
                ));

        if (!building.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot create a common area in an inactive building"
            );
        }

        String name = normalizeRequired(request.name(), "name");
        String areaType = normalizeRequired(request.areaType(), "areaType");

        if (commonAreaRepository.existsByBuildingIdAndNameIgnoreCase(
                request.buildingId(),
                name
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A common area with this name already exists in the selected building"
            );
        }

        CommonArea area = new CommonArea();

        area.setBuilding(building);
        area.setName(name);
        area.setAreaType(areaType);
        area.setCapacity(request.capacity());
        area.setBookingRequired(request.bookingRequired());
        area.setBookingDurationMinutes(request.bookingDurationMinutes());
        area.setActive(true);

        CommonArea saved = commonAreaRepository.save(area);

        return toResponse(saved);
    }

    @Transactional
    public CommonAreaResponse updateCommonArea(
            UUID commonAreaId,
            CommonAreaUpdateRequest request
    ) {
        CommonArea area = commonAreaRepository.findById(commonAreaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Common area not found: " + commonAreaId
                ));

        if (!area.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot edit a deactivated common area"
            );
        }

        String name = normalizeRequired(request.name(), "name");
        String areaType = normalizeRequired(request.areaType(), "areaType");

        if (commonAreaRepository.existsByBuildingIdAndNameIgnoreCaseAndIdNot(
                area.getBuilding().getId(),
                name,
                commonAreaId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A common area with this name already exists in the selected building"
            );
        }

        area.setName(name);
        area.setAreaType(areaType);
        area.setCapacity(request.capacity());
        area.setBookingRequired(request.bookingRequired());
        area.setBookingDurationMinutes(request.bookingDurationMinutes());

        CommonArea saved = commonAreaRepository.save(area);

        return toResponse(saved);
    }

    @Transactional
    public void deactivateCommonArea(UUID commonAreaId) {
        CommonArea area = commonAreaRepository.findById(commonAreaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Common area not found: " + commonAreaId
                ));

        if (!area.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Common area is already deactivated"
            );
        }

        area.setActive(false);

        commonAreaRepository.save(area);
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " is required"
            );
        }

        return value.trim();
    }

    private CommonAreaResponse toResponse(CommonArea area) {
        return new CommonAreaResponse(
                area.getId(),
                area.getBuilding().getId(),
                area.getBuilding().getCode(),
                area.getName(),
                area.getAreaType(),
                area.getCapacity(),
                area.isBookingRequired(),
                area.isActive(),
                area.getBookingDurationMinutes()
        );
    }
}