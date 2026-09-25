//---------------- milestone 19 ---------------------
// package com.condotrack.backend.service;

// import com.condotrack.backend.dto.DeliveryCreateRequest;
// import com.condotrack.backend.dto.DeliveryPageResponse;
// import com.condotrack.backend.dto.DeliveryResponse;
// import com.condotrack.backend.dto.DeliveryStatusUpdateRequest;
// import com.condotrack.backend.model.Building;
// import com.condotrack.backend.model.Delivery;
// import com.condotrack.backend.model.Enums;
// import com.condotrack.backend.model.Resident;
// import com.condotrack.backend.model.Unit;
// import com.condotrack.backend.model.User;
// import com.condotrack.backend.repository.BuildingRepository;
// import com.condotrack.backend.repository.DeliveryRepository;
// import com.condotrack.backend.repository.ResidentRepository;
// import com.condotrack.backend.repository.UnitRepository;
// import com.condotrack.backend.repository.UserRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.security.core.Authentication;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.OffsetDateTime;
// import java.util.UUID;

// @Service
// @RequiredArgsConstructor
// public class DeliveryService {

//     private static final int MAX_PAGE_SIZE = 100;

//     private final DeliveryRepository deliveryRepository;
//     private final BuildingRepository buildingRepository;
//     private final UnitRepository unitRepository;
//     private final ResidentRepository residentRepository;
//     private final UserRepository userRepository;

//     @Transactional(readOnly = true)
//     public DeliveryPageResponse getDeliveries(Authentication authentication, int page, int size) {
//         Pageable pageable = createPageable(page, size);
//         Page<DeliveryResponse> deliveries;

//         if (hasFullView(authentication)) {
//             deliveries = deliveryRepository
//                     .findAllByOrderByReceivedAtDesc(pageable)
//                     .map(this::toResponse);
//         } else {
//             deliveries = deliveryRepository
//                     .findByResident_User_EmailIgnoreCaseOrderByReceivedAtDesc(
//                             authentication.getName(),
//                             pageable
//                     )
//                     .map(this::toResponse);
//         }

//         return DeliveryPageResponse.from(deliveries);
//     }

//     @Transactional(readOnly = true)
//     public DeliveryResponse getDelivery(UUID deliveryId, Authentication authentication) {
//         Delivery delivery;

//         if (hasFullView(authentication)) {
//             delivery = deliveryRepository.findById(deliveryId)
//                     .orElseThrow(() -> new IllegalArgumentException(
//                             "Delivery not found: " + deliveryId
//                     ));
//         } else {
//             delivery = deliveryRepository
//                     .findByIdAndResident_User_EmailIgnoreCase(
//                             deliveryId,
//                             authentication.getName()
//                     )
//                     .orElseThrow(() -> new IllegalArgumentException(
//                             "Delivery not found: " + deliveryId
//                     ));
//         }

//         return toResponse(delivery);
//     }

//     @Transactional
//     public DeliveryResponse createDelivery(
//             DeliveryCreateRequest request,
//             Authentication authentication
//     ) {
//         Building building = buildingRepository.findByIdAndActiveTrue(request.buildingId())
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Building not found: " + request.buildingId()
//                 ));

//         Unit unit = unitRepository.findById(request.unitId())
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Unit not found: " + request.unitId()
//                 ));

//         if (!unit.isActive()) {
//             throw new IllegalStateException("Unit is inactive");
//         }

//         if (!unit.getBuilding().getId().equals(building.getId())) {
//             throw new IllegalArgumentException("Unit does not belong to the requested building");
//         }

//         Resident resident = residentRepository.findById(request.residentId())
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Resident not found: " + request.residentId()
//                 ));

//         if (!resident.isActive()) {
//             throw new IllegalStateException("Resident is inactive");
//         }

//         if (!resident.getUnit().getId().equals(unit.getId())) {
//             throw new IllegalArgumentException("Resident does not belong to the requested unit");
//         }

//         User currentUser = getAuthenticatedUser(authentication);
//         OffsetDateTime now = OffsetDateTime.now();

//         Delivery delivery = new Delivery();
//         delivery.setBuilding(building);
//         delivery.setUnit(unit);
//         delivery.setResident(resident);
//         delivery.setCarrierName(normalizeOptional(request.carrierName()));
//         delivery.setTrackingNumber(normalizeOptional(request.trackingNumber()));
//         delivery.setDeliveryType(request.deliveryType());
//         delivery.setStatus(Enums.DeliveryStatus.RECEIVED);
//         delivery.setReceivedAt(now);
//         delivery.setNotes(normalizeOptional(request.notes()));
//         delivery.setUpdatedBy(currentUser.getId());

//         return toResponse(deliveryRepository.save(delivery));
//     }

//     @Transactional
//     public DeliveryResponse updateStatus(
//             UUID deliveryId,
//             DeliveryStatusUpdateRequest request,
//             Authentication authentication
//     ) {
//         Delivery delivery = deliveryRepository.findById(deliveryId)
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Delivery not found: " + deliveryId
//                 ));

//         Enums.DeliveryStatus currentStatus = delivery.getStatus();
//         Enums.DeliveryStatus targetStatus = request.status();

//         validateTransition(currentStatus, targetStatus);

//         User currentUser = getAuthenticatedUser(authentication);
//         OffsetDateTime now = OffsetDateTime.now();

//         delivery.setStatus(targetStatus);
//         delivery.setUpdatedBy(currentUser.getId());

//         if (targetStatus == Enums.DeliveryStatus.NOTIFIED) {
//             delivery.setNotifiedAt(now);
//         }

//         if (targetStatus == Enums.DeliveryStatus.COLLECTED) {
//             delivery.setCollectedAt(now);
//             delivery.setCollectedByUser(currentUser);
//         }

//         return toResponse(deliveryRepository.save(delivery));
//     }

//     private boolean hasFullView(Authentication authentication) {
//         return authentication != null
//                 && authentication.isAuthenticated()
//                 && authentication.getAuthorities().stream()
//                 .anyMatch(authority -> authority.getAuthority().equals("PERM_DELIVERIES_VIEW"));
//     }

//     private void validateTransition(
//             Enums.DeliveryStatus currentStatus,
//             Enums.DeliveryStatus targetStatus
//     ) {
//         if (currentStatus == targetStatus) {
//             throw new IllegalStateException("Delivery is already in status " + targetStatus);
//         }

//         boolean valid = switch (currentStatus) {
//             case RECEIVED -> targetStatus == Enums.DeliveryStatus.NOTIFIED
//                     || targetStatus == Enums.DeliveryStatus.RETURNED
//                     || targetStatus == Enums.DeliveryStatus.CANCELLED;
//             case NOTIFIED -> targetStatus == Enums.DeliveryStatus.COLLECTED
//                     || targetStatus == Enums.DeliveryStatus.RETURNED
//                     || targetStatus == Enums.DeliveryStatus.CANCELLED;
//             case COLLECTED, RETURNED, CANCELLED -> false;
//         };

//         if (!valid) {
//             throw new IllegalStateException(
//                     "Invalid delivery status transition from "
//                             + currentStatus + " to " + targetStatus
//             );
//         }
//     }

//     private User getAuthenticatedUser(Authentication authentication) {
//         if (authentication == null || authentication.getName() == null) {
//             throw new IllegalStateException("Authenticated user is required");
//         }

//         return userRepository.findByEmailIgnoreCase(authentication.getName())
//                 .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
//     }

//     private Pageable createPageable(int page, int size) {
//         int safePage = Math.max(page, 0);
//         int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
//         return PageRequest.of(safePage, safeSize);
//     }

//     private DeliveryResponse toResponse(Delivery delivery) {
//         Resident resident = delivery.getResident();
//         String residentName = resident == null || resident.getUser() == null
//                 ? null
//                 : resident.getUser().getFirstName() + " " + resident.getUser().getLastName();

//         return new DeliveryResponse(
//                 delivery.getId(),
//                 delivery.getBuilding().getId(),
//                 delivery.getUnit().getId(),
//                 delivery.getUnit().getUnitNumber(),
//                 resident == null ? null : resident.getId(),
//                 residentName,
//                 delivery.getCarrierName(),
//                 delivery.getTrackingNumber(),
//                 delivery.getDeliveryType().name(),
//                 delivery.getStatus().name(),
//                 delivery.getReceivedAt(),
//                 delivery.getNotifiedAt(),
//                 delivery.getCollectedAt(),
//                 delivery.getCollectedByUser() == null ? null : delivery.getCollectedByUser().getId(),
//                 delivery.getNotes()
//         );
//     }

//     private String normalizeOptional(String value) {
//         if (value == null) {
//             return null;
//         }

//         String normalized = value.trim();
//         return normalized.isEmpty() ? null : normalized;
//     }
// }


//-------------------------- milestone 20 ---------------------
package com.condotrack.backend.service;

import com.condotrack.backend.dto.DeliveryCreateRequest;
import com.condotrack.backend.dto.DeliveryPageResponse;
import com.condotrack.backend.dto.DeliveryResponse;
import com.condotrack.backend.dto.DeliveryStatusUpdateRequest;
import com.condotrack.backend.model.Building;
import com.condotrack.backend.model.Delivery;
import com.condotrack.backend.model.Enums;
import com.condotrack.backend.model.Resident;
import com.condotrack.backend.model.Unit;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.BuildingRepository;
import com.condotrack.backend.repository.DeliveryRepository;
import com.condotrack.backend.repository.ResidentRepository;
import com.condotrack.backend.repository.UnitRepository;
import com.condotrack.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final DeliveryRepository deliveryRepository;
    private final BuildingRepository buildingRepository;
    private final UnitRepository unitRepository;
    private final ResidentRepository residentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public DeliveryPageResponse getDeliveries(Authentication authentication, int page, int size) {
        Pageable pageable = createPageable(page, size);
        Page<DeliveryResponse> deliveries;

        if (hasFullView(authentication)) {
            deliveries = deliveryRepository
                    .findAllByOrderByReceivedAtDesc(pageable)
                    .map(this::toResponse);
        } else {
            deliveries = deliveryRepository
                    .findByResident_User_EmailIgnoreCaseOrderByReceivedAtDesc(
                            authentication.getName(),
                            pageable
                    )
                    .map(this::toResponse);
        }

        return DeliveryPageResponse.from(deliveries);
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getDelivery(UUID deliveryId, Authentication authentication) {
        Delivery delivery;

        if (hasFullView(authentication)) {
            delivery = deliveryRepository.findById(deliveryId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Delivery not found: " + deliveryId
                    ));
        } else {
            delivery = deliveryRepository
                    .findByIdAndResident_User_EmailIgnoreCase(
                            deliveryId,
                            authentication.getName()
                    )
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Delivery not found: " + deliveryId
                    ));
        }

        return toResponse(delivery);
    }

    @Transactional
    public DeliveryResponse createDelivery(
            DeliveryCreateRequest request,
            Authentication authentication
    ) {
        Building building = buildingRepository.findByIdAndActiveTrue(request.buildingId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Building not found: " + request.buildingId()
                ));

        Unit unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unit not found: " + request.unitId()
                ));

        if (!unit.isActive()) {
            throw new IllegalStateException("Unit is inactive");
        }

        if (!unit.getBuilding().getId().equals(building.getId())) {
            throw new IllegalArgumentException("Unit does not belong to the requested building");
        }

        Resident resident = residentRepository.findById(request.residentId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Resident not found: " + request.residentId()
                ));

        if (!resident.isActive()) {
            throw new IllegalStateException("Resident is inactive");
        }

        if (!resident.getUnit().getId().equals(unit.getId())) {
            throw new IllegalArgumentException("Resident does not belong to the requested unit");
        }

        User currentUser = getAuthenticatedUser(authentication);
        OffsetDateTime now = OffsetDateTime.now();

        Delivery delivery = new Delivery();
        delivery.setBuilding(building);
        delivery.setUnit(unit);
        delivery.setResident(resident);
        delivery.setCarrierName(normalizeOptional(request.carrierName()));
        delivery.setTrackingNumber(normalizeOptional(request.trackingNumber()));
        delivery.setDeliveryType(request.deliveryType());
        delivery.setStatus(Enums.DeliveryStatus.RECEIVED);
        delivery.setReceivedAt(now);
        delivery.setNotes(normalizeOptional(request.notes()));
        delivery.setUpdatedBy(currentUser.getId());

        Delivery saved = deliveryRepository.save(delivery);
        auditService.record(authentication.getName(), building, "DELIVERY", saved.getId(), "CREATE", null, saved.getStatus().name(), null, java.util.Map.of("unitId", unit.getId().toString()));
        return toResponse(saved);
    }

    @Transactional
    public DeliveryResponse updateStatus(
            UUID deliveryId,
            DeliveryStatusUpdateRequest request,
            Authentication authentication
    ) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Delivery not found: " + deliveryId
                ));

        Enums.DeliveryStatus currentStatus = delivery.getStatus();
        Enums.DeliveryStatus targetStatus = request.status();

        validateTransition(currentStatus, targetStatus);

        User currentUser = getAuthenticatedUser(authentication);
        OffsetDateTime now = OffsetDateTime.now();

        delivery.setStatus(targetStatus);
        delivery.setUpdatedBy(currentUser.getId());

        if (targetStatus == Enums.DeliveryStatus.NOTIFIED) {
            delivery.setNotifiedAt(now);
        }

        if (targetStatus == Enums.DeliveryStatus.COLLECTED) {
            delivery.setCollectedAt(now);
            delivery.setCollectedByUser(currentUser);
        }

        Delivery saved = deliveryRepository.save(delivery);
        auditService.record(authentication.getName(), delivery.getBuilding(), "DELIVERY", saved.getId(), "STATUS_CHANGED", currentStatus.name(), saved.getStatus().name(), null, java.util.Map.of());
        return toResponse(saved);
    }

    private boolean hasFullView(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("PERM_DELIVERIES_VIEW"));
    }

    private void validateTransition(
            Enums.DeliveryStatus currentStatus,
            Enums.DeliveryStatus targetStatus
    ) {
        if (currentStatus == targetStatus) {
            throw new IllegalStateException("Delivery is already in status " + targetStatus);
        }

        boolean valid = switch (currentStatus) {
            case RECEIVED -> targetStatus == Enums.DeliveryStatus.NOTIFIED
                    || targetStatus == Enums.DeliveryStatus.RETURNED
                    || targetStatus == Enums.DeliveryStatus.CANCELLED;
            case NOTIFIED -> targetStatus == Enums.DeliveryStatus.COLLECTED
                    || targetStatus == Enums.DeliveryStatus.RETURNED
                    || targetStatus == Enums.DeliveryStatus.CANCELLED;
            case COLLECTED, RETURNED, CANCELLED -> false;
        };

        if (!valid) {
            throw new IllegalStateException(
                    "Invalid delivery status transition from "
                            + currentStatus + " to " + targetStatus
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

    private DeliveryResponse toResponse(Delivery delivery) {
        Resident resident = delivery.getResident();
        String residentName = resident == null || resident.getUser() == null
                ? null
                : resident.getUser().getFirstName() + " " + resident.getUser().getLastName();

        return new DeliveryResponse(
                delivery.getId(),
                delivery.getBuilding().getId(),
                delivery.getUnit().getId(),
                delivery.getUnit().getUnitNumber(),
                resident == null ? null : resident.getId(),
                residentName,
                delivery.getCarrierName(),
                delivery.getTrackingNumber(),
                delivery.getDeliveryType().name(),
                delivery.getStatus().name(),
                delivery.getReceivedAt(),
                delivery.getNotifiedAt(),
                delivery.getCollectedAt(),
                delivery.getCollectedByUser() == null ? null : delivery.getCollectedByUser().getId(),
                delivery.getNotes()
        );
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
