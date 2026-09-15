package com.condotrack.backend.service;

import com.condotrack.backend.dto.UnitResponse;
import com.condotrack.backend.model.Unit;
import com.condotrack.backend.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;

    @Transactional(readOnly = true)
    public UnitResponse getUnit(UUID unitId) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + unitId));

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
