package com.condotrack.backend.service;

import com.condotrack.backend.dto.ResidentResponse;
import com.condotrack.backend.dto.ResidentUpdateRequest;
import com.condotrack.backend.model.Resident;
import com.condotrack.backend.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResidentService {

    private final ResidentRepository residentRepository;

    @Transactional(readOnly = true)
    public ResidentResponse getResident(UUID residentId) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException("Resident not found: " + residentId));

        return toResponse(resident);
    }

    @Transactional
    public ResidentResponse updateResident(UUID residentId, ResidentUpdateRequest request) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException("Resident not found: " + residentId));

        if (request.firstName() != null) {
            resident.getUser().setFirstName(request.firstName().trim());
        }

        if (request.lastName() != null) {
            resident.getUser().setLastName(request.lastName().trim());
        }

        if (request.phone() != null) {
            resident.getUser().setPhone(request.phone().trim());
        }

        return toResponse(resident);
    }

    private ResidentResponse toResponse(Resident resident) {
        return new ResidentResponse(
                resident.getId(),
                resident.getUser().getId(),
                resident.getUser().getEmail(),
                resident.getUser().getFirstName(),
                resident.getUser().getLastName(),
                resident.getUser().getPhone(),
                resident.getUnit().getId(),
                resident.getUnit().getBuilding().getId(),
                resident.getUnit().getUnitNumber(),
                resident.getResidentType().name(),
                resident.getMoveInDate() == null ? null : resident.getMoveInDate().toString(),
                resident.getMoveOutDate() == null ? null : resident.getMoveOutDate().toString(),
                resident.isPrimaryContact(),
                resident.isActive()
        );
    }
}
