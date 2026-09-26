//------------------ original PR32 ------------------------
// package com.condotrack.backend.service;

// import com.condotrack.backend.dto.CommonAreaResponse;
// import com.condotrack.backend.model.CommonArea;
// import com.condotrack.backend.repository.CommonAreaRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.List;
// import java.util.UUID;

// @Service
// @RequiredArgsConstructor
// public class CommonAreaService {

//     private final CommonAreaRepository commonAreaRepository;

//     @Transactional(readOnly = true)
//     public List<CommonAreaResponse> getActiveCommonAreas() {
//         return commonAreaRepository.findByActiveTrueOrderByNameAsc()
//                 .stream()
//                 .map(this::toResponse)
//                 .toList();
//     }

//     @Transactional(readOnly = true)
//     public CommonAreaResponse getActiveCommonArea(UUID commonAreaId) {
//         CommonArea commonArea = commonAreaRepository
//                 .findByIdAndActiveTrue(commonAreaId)
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Common area not found: " + commonAreaId
//                 ));

//         return toResponse(commonArea);
//     }

//     private CommonAreaResponse toResponse(CommonArea commonArea) {
//         return new CommonAreaResponse(
//                 commonArea.getId(),
//                 commonArea.getBuilding().getId(),
//                 commonArea.getName(),
//                 commonArea.getAreaType(),
//                 commonArea.getCapacity(),
//                 commonArea.isBookingRequired(),
//                 commonArea.isActive(),
//                 commonArea.getBookingDurationMinutes()
//         );
//     }
// }


//------------------ new PR32 M21 ------------------------
package com.condotrack.backend.service;

import com.condotrack.backend.dto.CommonAreaResponse;
import com.condotrack.backend.model.CommonArea;
import com.condotrack.backend.repository.CommonAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommonAreaService {

    private static final int MAX_PAGE_SIZE = 100;

    private final CommonAreaRepository commonAreaRepository;

    @Transactional(readOnly = true)
    public Page<CommonAreaResponse> getCommonAreas(UUID buildingId, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE)
        );

        Page<CommonArea> areas = buildingId == null
                ? commonAreaRepository.findByActiveTrueOrderByBuildingIdAscNameAsc(pageable)
                : commonAreaRepository.findByBuildingIdAndActiveTrueOrderByNameAsc(buildingId, pageable);

        return areas.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CommonAreaResponse getCommonArea(UUID commonAreaId) {
        CommonArea area = commonAreaRepository.findById(commonAreaId)
                .filter(CommonArea::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Common area not found: " + commonAreaId));
        return toResponse(area);
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
