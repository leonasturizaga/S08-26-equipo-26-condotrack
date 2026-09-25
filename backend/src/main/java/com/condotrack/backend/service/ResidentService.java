//------------------- milestone 7 ---------------------
// package com.condotrack.backend.service;

// import com.condotrack.backend.dto.ResidentPageResponse;
// import com.condotrack.backend.dto.ResidentResponse;
// import com.condotrack.backend.dto.ResidentUpdateRequest;
// import com.condotrack.backend.model.Resident;
// import com.condotrack.backend.repository.ResidentRepository;
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
// public class ResidentService {

//     private static final int DEFAULT_PAGE_SIZE = 20;
//     private static final int MAX_PAGE_SIZE = 100;

//     private final ResidentRepository residentRepository;
//     private final PermissionService permissionService;

//     @Transactional(readOnly = true)
//     public ResidentResponse getResident(UUID residentId) {
//         Resident resident = residentRepository.findById(residentId)
//                 .orElseThrow(() -> new IllegalArgumentException("Resident not found: " + residentId));

//         return toResponse(resident);
//     }

//     @Transactional
//     public ResidentResponse updateResident(UUID residentId, ResidentUpdateRequest request) {
//         Resident resident = residentRepository.findById(residentId)
//                 .orElseThrow(() -> new IllegalArgumentException("Resident not found: " + residentId));

//         if (request.firstName() != null) {
//             resident.getUser().setFirstName(request.firstName().trim());
//         }

//         if (request.lastName() != null) {
//             resident.getUser().setLastName(request.lastName().trim());
//         }

//         if (request.phone() != null) {
//             resident.getUser().setPhone(request.phone().trim());
//         }

//         return toResponse(resident);
//     }

//     @Transactional(readOnly = true)
//     public ResidentPageResponse listResidents(
//             Authentication authentication,
//             Pageable pageable
//     ) {
//         Pageable safePageable = normalizePageable(pageable);

//         Page<Resident> residents;

//         if (permissionService.hasPermission(authentication, "RESIDENTS_VIEW")) {
//             residents = residentRepository.findByActiveTrue(safePageable);
//         } else if (permissionService.hasPermission(authentication, "RESIDENTS_VIEW_OWN")) {
//             residents = residentRepository.findByUser_EmailIgnoreCaseAndActiveTrue(
//                     authentication.getName(),
//                     safePageable
//             );
//         } else {
//             throw new org.springframework.security.access.AccessDeniedException(
//                     "You do not have permission to view residents"
//             );
//         }

//         return new ResidentPageResponse(
//                 residents.getContent().stream()
//                         .map(this::toResponse)
//                         .toList(),
//                 residents.getNumber(),
//                 residents.getSize(),
//                 residents.getTotalElements(),
//                 residents.getTotalPages()
//         );
//     }

//     private Pageable normalizePageable(Pageable pageable) {
//         int page = Math.max(pageable.getPageNumber(), 0);
//         int requestedSize = pageable.getPageSize() > 0
//                 ? pageable.getPageSize()
//                 : DEFAULT_PAGE_SIZE;
//         int size = Math.min(requestedSize, MAX_PAGE_SIZE);

//         return PageRequest.of(page, size, pageable.getSort());
//     }

//     private ResidentResponse toResponse(Resident resident) {
//         return new ResidentResponse(
//                 resident.getId(),
//                 resident.getUser().getId(),
//                 resident.getUser().getEmail(),
//                 resident.getUser().getFirstName(),
//                 resident.getUser().getLastName(),
//                 resident.getUser().getPhone(),
//                 resident.getUnit().getId(),
//                 resident.getUnit().getBuilding().getId(),
//                 resident.getUnit().getUnitNumber(),
//                 resident.getResidentType().name(),
//                 resident.getMoveInDate() == null ? null : resident.getMoveInDate().toString(),
//                 resident.getMoveOutDate() == null ? null : resident.getMoveOutDate().toString(),
//                 resident.isPrimaryContact(),
//                 resident.isActive()
//         );
//     }
// }


//------------------- milestone 20 ---------------------
package com.condotrack.backend.service;

import com.condotrack.backend.dto.ResidentPageResponse;
import com.condotrack.backend.dto.ResidentResponse;
import com.condotrack.backend.dto.ResidentUpdateRequest;
import com.condotrack.backend.model.Resident;
import com.condotrack.backend.repository.ResidentRepository;
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
public class ResidentService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final ResidentRepository residentRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public ResidentResponse getResident(UUID residentId) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException("Resident not found: " + residentId));

        return toResponse(resident);
    }

    @Transactional
    public ResidentResponse updateResident(UUID residentId, ResidentUpdateRequest request, Authentication authentication) {
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

        auditService.record(authentication.getName(), resident.getUnit().getBuilding(), "RESIDENT", resident.getId(), "UPDATE", java.util.Map.of("userId", resident.getUser().getId().toString()));
        return toResponse(resident);
    }

    @Transactional(readOnly = true)
    public ResidentPageResponse listResidents(
            Authentication authentication,
            Pageable pageable
    ) {
        Pageable safePageable = normalizePageable(pageable);

        Page<Resident> residents;

        if (permissionService.hasPermission(authentication, "RESIDENTS_VIEW")) {
            residents = residentRepository.findByActiveTrue(safePageable);
        } else if (permissionService.hasPermission(authentication, "RESIDENTS_VIEW_OWN")) {
            residents = residentRepository.findByUser_EmailIgnoreCaseAndActiveTrue(
                    authentication.getName(),
                    safePageable
            );
        } else {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to view residents"
            );
        }

        return new ResidentPageResponse(
                residents.getContent().stream()
                        .map(this::toResponse)
                        .toList(),
                residents.getNumber(),
                residents.getSize(),
                residents.getTotalElements(),
                residents.getTotalPages()
        );
    }

    private Pageable normalizePageable(Pageable pageable) {
        int page = Math.max(pageable.getPageNumber(), 0);
        int requestedSize = pageable.getPageSize() > 0
                ? pageable.getPageSize()
                : DEFAULT_PAGE_SIZE;
        int size = Math.min(requestedSize, MAX_PAGE_SIZE);

        return PageRequest.of(page, size, pageable.getSort());
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
