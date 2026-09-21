package com.condotrack.backend.service;

import com.condotrack.backend.dto.CommonAreaResponse;
import com.condotrack.backend.model.CommonArea;
import com.condotrack.backend.repository.CommonAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommonAreaService {

    private final CommonAreaRepository commonAreaRepository;

    @Transactional(readOnly = true)
    public List<CommonAreaResponse> getActiveCommonAreas() {
        return commonAreaRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CommonAreaResponse getActiveCommonArea(UUID commonAreaId) {
        CommonArea commonArea = commonAreaRepository
                .findByIdAndActiveTrue(commonAreaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Common area not found: " + commonAreaId
                ));

        return toResponse(commonArea);
    }

    private CommonAreaResponse toResponse(CommonArea commonArea) {
        return new CommonAreaResponse(
                commonArea.getId(),
                commonArea.getBuilding().getId(),
                commonArea.getName(),
                commonArea.getAreaType(),
                commonArea.getCapacity(),
                commonArea.isBookingRequired(),
                commonArea.isActive(),
                commonArea.getBookingDurationMinutes()
        );
    }
}