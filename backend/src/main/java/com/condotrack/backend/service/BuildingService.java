package com.condotrack.backend.service;

import com.condotrack.backend.dto.BuildingResponse;
import com.condotrack.backend.model.Building;
import com.condotrack.backend.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;

    @Transactional(readOnly = true)
    public List<BuildingResponse> getAllBuildings() {
        return buildingRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
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
