package com.condotrack.backend.service;

import com.condotrack.backend.dto.UnitLookupResponse;
import com.condotrack.backend.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitLookupService {

    private static final int MAX_RESULTS = 20;

    private final UnitRepository unitRepository;

    @Transactional(readOnly = true)
    public List<UnitLookupResponse> search(String query) {
        String normalized = query == null ? "" : query.trim();

        if (normalized.length() < 2) {
            return List.of();
        }

        return unitRepository.searchActiveUnits(
                        normalized,
                        PageRequest.of(0, MAX_RESULTS)
                )
                .stream()
                .map(unit -> new UnitLookupResponse(
                        unit.getId(),
                        unit.getBuilding().getId(),
                        unit.getBuilding().getName(),
                        unit.getBuilding().getCode(),
                        unit.getUnitNumber(),
                        unit.getFloorNumber(),
                        unit.getUnitType().name()
                ))
                .toList();
    }
}
