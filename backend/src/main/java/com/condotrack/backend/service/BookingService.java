//------------------ original PR32 ------------------------
// package com.condotrack.backend.service;

// import com.condotrack.backend.dto.BookingCreateRequest;
// import com.condotrack.backend.dto.BookingResponse;
// import com.condotrack.backend.dto.BookingStatusUpdateRequest;
// import com.condotrack.backend.model.*;
// import com.condotrack.backend.repository.*;
// import lombok.RequiredArgsConstructor;
// import org.springframework.dao.DataIntegrityViolationException;
// import org.springframework.security.core.Authentication;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.OffsetDateTime;
// import java.util.List;
// import java.util.UUID;

// @Service
// @RequiredArgsConstructor
// public class BookingService {

//     private final BookingRepository bookingRepository;
//     private final CommonAreaRepository commonAreaRepository;
//     private final UnitRepository unitRepository;
//     private final ResidentRepository residentRepository;
//     private final UserRepository userRepository;
//     private final PermissionService permissionService;

//     @Transactional(readOnly = true)
//     public List<BookingResponse> getBookings(
//             Authentication authentication
//     ) {
//         List<Booking> bookings;

//         if (permissionService.hasPermission(
//                 authentication,
//                 "BOOKINGS_VIEW"
//         )) {
//             bookings = bookingRepository.findAllByOrderByStartAtDesc();
//         } else {
//             bookings = bookingRepository
//                     .findByResident_User_EmailIgnoreCaseOrderByStartAtDesc(
//                             authentication.getName()
//                     );
//         }

//         return bookings.stream()
//                 .map(this::toResponse)
//                 .toList();
//     }

//     @Transactional(readOnly = true)
//     public BookingResponse getBooking(
//             UUID bookingId,
//             Authentication authentication
//     ) {
//         Booking booking;

//         if (permissionService.hasPermission(
//                 authentication,
//                 "BOOKINGS_VIEW"
//         )) {
//             booking = bookingRepository.findById(bookingId)
//                     .orElseThrow(() -> new IllegalArgumentException(
//                             "Booking not found: " + bookingId
//                     ));
//         } else {
//             booking = bookingRepository
//                     .findByIdAndResident_User_EmailIgnoreCase(
//                             bookingId,
//                             authentication.getName()
//                     )
//                     .orElseThrow(() -> new IllegalArgumentException(
//                             "Booking not found: " + bookingId
//                     ));
//         }

//         return toResponse(booking);
//     }

//     @Transactional
//     public BookingResponse createBooking(BookingCreateRequest request,Authentication authentication) {
//         validateDates(request.startAt(), request.endAt());

//         CommonArea commonArea = commonAreaRepository
//                 .findByIdAndBuildingIdAndActiveTrue(
//                         request.commonAreaId(),
//                         request.buildingId()
//                 )
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Active common area not found"
//                 ));

//         Unit unit = unitRepository.findById(request.unitId())
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Unit not found: " + request.unitId()
//                 ));

//         if (!unit.isActive()) {
//             throw new IllegalStateException("Unit is inactive");
//         }

//         if (!unit.getBuilding().getId().equals(request.buildingId())) {
//             throw new IllegalArgumentException(
//                     "Unit does not belong to the requested building"
//             );
//         }

//         Resident resident = residentRepository.findById(request.residentId())
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Resident not found: " + request.residentId()
//                 ));

//         if (!resident.isActive()) {
//             throw new IllegalStateException("Resident is inactive");
//         }

//         if (!resident.getUnit().getId().equals(unit.getId())) {
//             throw new IllegalArgumentException(
//                     "Resident does not belong to the requested unit"
//             );
//         }

//         User currentUser = getAuthenticatedUser(authentication);

//         Booking booking = new Booking();
//         booking.setBuilding(unit.getBuilding());
//         booking.setCommonArea(commonArea);
//         booking.setUnit(unit);
//         booking.setResident(resident);
//         booking.setStartAt(request.startAt());
//         booking.setEndAt(request.endAt());
//         booking.setPurpose(normalizeOptional(request.purpose()));
//         booking.setUpdatedBy(currentUser.getId());

//         booking.setStatus(Enums.BookingStatus.APPROVED);

//         try {
//             return toResponse(bookingRepository.saveAndFlush(booking));
//         } catch (DataIntegrityViolationException exception) {
//             throw new IllegalStateException(
//                     "The common area is already booked for the selected time"
//             );
//         }
//     }

//     @Transactional
//     public BookingResponse updateStatus(
//             UUID bookingId,
//             BookingStatusUpdateRequest request,
//             Authentication authentication
//     ) {
//         Booking booking = bookingRepository.findById(bookingId)
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Booking not found: " + bookingId
//                 ));

//         boolean fullUpdate = permissionService.hasPermission(
//                 authentication,
//                 "BOOKINGS_UPDATE"
//         );

//         boolean ownCancellation =
//                 request.status() == Enums.BookingStatus.CANCELLED
//                 && permissionService.hasPermission(
//                         authentication,
//                         "BOOKINGS_CANCEL_OWN"
//                 )
//                 && booking.getResident() != null
//                 && booking.getResident()
//                         .getUser()
//                         .getEmail()
//                         .equalsIgnoreCase(authentication.getName());

//         if (!fullUpdate && !ownCancellation) {
//             throw new SecurityException(
//                     "You are not allowed to update this booking"
//             );
//         }

//         if (booking.getStatus() == Enums.BookingStatus.CANCELLED
//                 || booking.getStatus() == Enums.BookingStatus.COMPLETED
//                 || booking.getStatus() == Enums.BookingStatus.NO_SHOW) {
//             throw new IllegalStateException(
//                     "Booking cannot be updated from current status"
//             );
//         }

//         User currentUser = getAuthenticatedUser(authentication);

//         booking.setStatus(request.status());
//         booking.setCancellationReason(
//                 normalizeOptional(request.cancellationReason())
//         );
//         booking.setUpdatedBy(currentUser.getId());

//         return toResponse(bookingRepository.save(booking));
//     }

//     private void validateDates(
//             OffsetDateTime startAt,
//             OffsetDateTime endAt
//     ) {
//         if (!endAt.isAfter(startAt)) {
//             throw new IllegalArgumentException(
//                     "endAt must be after startAt"
//             );
//         }

//         if (startAt.isBefore(OffsetDateTime.now())) {
//             throw new IllegalArgumentException(
//                     "Booking cannot start in the past"
//             );
//         }
//     }

//     private User getAuthenticatedUser(
//             Authentication authentication
//     ) {
//         if (authentication == null
//                 || authentication.getName() == null) {
//             throw new IllegalStateException(
//                     "Authenticated user is required"
//             );
//         }

//         return userRepository
//                 .findByEmailIgnoreCase(authentication.getName())
//                 .orElseThrow(() -> new IllegalStateException(
//                         "Authenticated user not found"
//                 ));
//     }

//     private BookingResponse toResponse(Booking booking) {
//         Resident resident = booking.getResident();

//         String residentName = resident == null
//                 || resident.getUser() == null
//                 ? null
//                 : resident.getUser().getFirstName()
//                     + " "
//                     + resident.getUser().getLastName();

//         return new BookingResponse(
//                 booking.getId(),
//                 booking.getBuilding().getId(),
//                 booking.getCommonArea().getId(),
//                 booking.getCommonArea().getName(),
//                 booking.getUnit().getId(),
//                 booking.getUnit().getUnitNumber(),
//                 resident == null ? null : resident.getId(),
//                 residentName,
//                 booking.getStatus().name(),
//                 booking.getStartAt(),
//                 booking.getEndAt(),
//                 booking.getPurpose(),
//                 booking.getApprovedByStaff() == null
//                         ? null
//                         : booking.getApprovedByStaff().getId(),
//                 booking.getApprovedAt(),
//                 booking.getCancellationReason()
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


//------------------ original PR32 M21 ------------------------
package com.condotrack.backend.service;

import com.condotrack.backend.dto.BookingCreateRequest;
import com.condotrack.backend.dto.BookingPageResponse;
import com.condotrack.backend.dto.BookingResponse;
import com.condotrack.backend.dto.BookingStatusUpdateRequest;
import com.condotrack.backend.dto.BookingUpdateRequest;
import com.condotrack.backend.model.Booking;
import com.condotrack.backend.model.Building;
import com.condotrack.backend.model.CommonArea;
import com.condotrack.backend.model.Enums;
import com.condotrack.backend.model.Resident;
import com.condotrack.backend.model.Staff;
import com.condotrack.backend.model.Unit;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.BookingRepository;
import com.condotrack.backend.repository.CommonAreaRepository;
import com.condotrack.backend.repository.ResidentRepository;
import com.condotrack.backend.repository.StaffRepository;
import com.condotrack.backend.repository.UnitRepository;
import com.condotrack.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final int MAX_PAGE_SIZE = 100;

    private final BookingRepository bookingRepository;
    private final UnitRepository unitRepository;
    private final ResidentRepository residentRepository;
    private final StaffRepository staffRepository;
    private final CommonAreaRepository commonAreaRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public BookingPageResponse getBookings(Authentication authentication, int page, int size) {
        Pageable pageable = createPageable(page, size);
        boolean canSeeAll = authentication != null
                && authentication.isAuthenticated()
                && permissionService.hasPermission(authentication, "BOOKINGS_VIEW");

        Page<Booking> bookings = canSeeAll
                ? bookingRepository.findAllByOrderByStartAtDesc(pageable)
                : bookingRepository.findAllByResident_User_EmailIgnoreCaseOrderByStartAtDesc(
                authentication.getName(), pageable);

        return BookingPageResponse.from(bookings.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request, Authentication authentication) {
        validateDates(request.startAt(), request.endAt());

        Unit unit = getActiveUnit(request.unitId());
        Building building = unit.getBuilding();
        if (building == null || !building.isActive()) {
            throw new IllegalArgumentException("The unit belongs to an inactive or missing building");
        }
        if (request.buildingId() == null || !request.buildingId().equals(building.getId())) {
            throw new IllegalArgumentException("buildingId does not match the selected unit's building");
        }

        CommonArea commonArea = getActiveCommonArea(request.commonAreaId());
        if (!commonArea.getBuilding().getId().equals(building.getId())) {
            throw new IllegalArgumentException("Common area does not belong to the unit's building");
        }

        Resident resident = getActiveResident(request.residentId());
        if (!resident.getUnit().getId().equals(unit.getId())) {
            throw new IllegalArgumentException("Resident does not belong to the requested unit");
        }

        User currentUser = getAuthenticatedUser(authentication);
        Booking booking = new Booking();
        booking.setBuilding(building);
        booking.setCommonArea(commonArea);
        booking.setUnit(unit);
        booking.setResident(resident);
        booking.setStatus(Enums.BookingStatus.PENDING);
        booking.setStartAt(request.startAt());
        booking.setEndAt(request.endAt());
        booking.setPurpose(normalizeOptional(request.purpose()));
        booking.setUpdatedBy(currentUser.getId());

        try {
            Booking saved = bookingRepository.saveAndFlush(booking);
            auditService.record(
                    authentication.getName(),
                    building,
                    "BOOKING",
                    saved.getId(),
                    "CREATE",
                    null,
                    saved.getStatus().name(),
                    null,
                    Map.of(
                            "commonAreaId", commonArea.getId().toString(),
                            "unitId", unit.getId().toString(),
                            "residentId", resident.getId().toString()
                    )
            );
            return toResponse(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException(
                    "The requested common-area time slot is already booked or overlaps an active booking"
            );
        }
    }

    @Transactional
    public BookingResponse updateBooking(UUID bookingId, BookingUpdateRequest request, Authentication authentication) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (booking.getStatus() != Enums.BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be rescheduled. Approved bookings must be cancelled and submitted again.");
        }

        validateDates(request.startAt(), request.endAt());

        User currentUser = getAuthenticatedUser(authentication);
        OffsetDateTime previousStart = booking.getStartAt();
        OffsetDateTime previousEnd = booking.getEndAt();

        booking.setStartAt(request.startAt());
        booking.setEndAt(request.endAt());
        booking.setPurpose(normalizeOptional(request.purpose()));
        booking.setUpdatedBy(currentUser.getId());

        try {
            Booking saved = bookingRepository.saveAndFlush(booking);
            auditService.record(
                    authentication.getName(),
                    saved.getBuilding(),
                    "BOOKING",
                    saved.getId(),
                    "UPDATE",
                    null,
                    saved.getStatus().name(),
                    null,
                    Map.of(
                            "previousStartAt", previousStart.toString(),
                            "previousEndAt", previousEnd.toString(),
                            "startAt", saved.getStartAt().toString(),
                            "endAt", saved.getEndAt().toString()
                    )
            );
            return toResponse(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException(
                    "The requested common-area time slot is already booked or overlaps an active booking"
            );
        }
    }

    @Transactional
    public BookingResponse updateStatus(UUID bookingId, BookingStatusUpdateRequest request, Authentication authentication) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        Enums.BookingStatus currentStatus = booking.getStatus();
        Enums.BookingStatus targetStatus = request.status();
        validateStatusTransition(currentStatus, targetStatus, authentication);

        User currentUser = getAuthenticatedUser(authentication);
        booking.setStatus(targetStatus);
        booking.setUpdatedBy(currentUser.getId());

        if (targetStatus == Enums.BookingStatus.APPROVED) {
            booking.setApprovedAt(OffsetDateTime.now());
            staffRepository.findActiveByUserEmailIgnoreCase(authentication.getName()).stream()
                    .filter(staff -> staff.getBuilding() != null
                            && staff.getBuilding().getId().equals(booking.getBuilding().getId()))
                    .findFirst()
                    .ifPresent(booking::setApprovedByStaff);
        }

        if (targetStatus == Enums.BookingStatus.CANCELLED || targetStatus == Enums.BookingStatus.REJECTED) {
            booking.setCancellationReason(normalizeOptional(request.cancellationReason()));
        } else {
            booking.setCancellationReason(null);
        }

        Booking saved = bookingRepository.save(booking);
        auditService.record(
                authentication.getName(),
                booking.getBuilding(),
                "BOOKING",
                saved.getId(),
                "STATUS_CHANGED",
                currentStatus.name(),
                targetStatus.name(),
                saved.getCancellationReason(),
                Map.of("commonAreaId", booking.getCommonArea().getId().toString())
        );
        return toResponse(saved);
    }

    private void validateDates(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("endAt must be after startAt");
        }
    }

    private void validateStatusTransition(Enums.BookingStatus current, Enums.BookingStatus target, Authentication authentication) {
        if (current == target) {
            throw new IllegalStateException("Booking is already in status " + target);
        }

        boolean adminUpdate = userHasPermission(authentication, "BOOKINGS_UPDATE");
        if (!adminUpdate && target != Enums.BookingStatus.CANCELLED) {
            throw new IllegalStateException("Only cancellation is allowed for own bookings");
        }

        if (!adminUpdate) {
            if (current != Enums.BookingStatus.PENDING && current != Enums.BookingStatus.APPROVED) {
                throw new IllegalStateException("Only pending or approved bookings can be cancelled");
            }
            return;
        }

        boolean valid = switch (current) {
            case PENDING -> target == Enums.BookingStatus.APPROVED
                    || target == Enums.BookingStatus.REJECTED
                    || target == Enums.BookingStatus.CANCELLED;
            case APPROVED -> target == Enums.BookingStatus.COMPLETED
                    || target == Enums.BookingStatus.CANCELLED
                    || target == Enums.BookingStatus.NO_SHOW;
            case REJECTED, CANCELLED, COMPLETED, NO_SHOW -> false;
        };

        if (!valid) {
            throw new IllegalStateException("Invalid booking status transition from " + current + " to " + target);
        }
    }

    private Unit getActiveUnit(UUID unitId) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + unitId));
        if (!unit.isActive()) {
            throw new IllegalArgumentException("Unit is inactive: " + unitId);
        }
        return unit;
    }

    private CommonArea getActiveCommonArea(UUID commonAreaId) {
        return commonAreaRepository.findById(commonAreaId)
                .filter(CommonArea::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Common area not found: " + commonAreaId));
    }

    private Resident getActiveResident(UUID residentId) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException("Resident not found: " + residentId));
        if (!resident.isActive()) {
            throw new IllegalArgumentException("Resident is inactive: " + residentId);
        }
        return resident;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authenticated user is required");
        }
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private BookingResponse toResponse(Booking booking) {
        Resident resident = booking.getResident();
        String residentName = resident == null || resident.getUser() == null
                ? null
                : resident.getUser().getFirstName() + " " + resident.getUser().getLastName();

        return new BookingResponse(
                booking.getId(),
                booking.getBuilding().getId(),
                booking.getBuilding().getCode(),
                booking.getCommonArea().getId(),
                booking.getCommonArea().getName(),
                booking.getUnit().getId(),
                booking.getUnit().getUnitNumber(),
                resident == null ? null : resident.getId(),
                residentName,
                booking.getStatus().name(),
                booking.getStartAt(),
                booking.getEndAt(),
                booking.getPurpose(),
                booking.getApprovedAt(),
                booking.getApprovedByStaff() == null
                        ? null
                        : booking.getApprovedByStaff().getId(),
                booking.getCancellationReason()
        );
    }

    private Pageable createPageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
    }

    private boolean userHasPermission(Authentication authentication, String permissionCode) {
        return authentication != null
                && authentication.isAuthenticated()
                && permissionService.hasPermission(authentication, permissionCode);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
