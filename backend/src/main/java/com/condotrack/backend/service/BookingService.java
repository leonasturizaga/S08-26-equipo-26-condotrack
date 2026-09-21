package com.condotrack.backend.service;

import com.condotrack.backend.dto.BookingCreateRequest;
import com.condotrack.backend.dto.BookingResponse;
import com.condotrack.backend.dto.BookingStatusUpdateRequest;
import com.condotrack.backend.model.*;
import com.condotrack.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CommonAreaRepository commonAreaRepository;
    private final UnitRepository unitRepository;
    private final ResidentRepository residentRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookings(
            Authentication authentication
    ) {
        List<Booking> bookings;

        if (permissionService.hasPermission(
                authentication,
                "BOOKINGS_VIEW"
        )) {
            bookings = bookingRepository.findAllByOrderByStartAtDesc();
        } else {
            bookings = bookingRepository
                    .findByResident_User_EmailIgnoreCaseOrderByStartAtDesc(
                            authentication.getName()
                    );
        }

        return bookings.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(
            UUID bookingId,
            Authentication authentication
    ) {
        Booking booking;

        if (permissionService.hasPermission(
                authentication,
                "BOOKINGS_VIEW"
        )) {
            booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Booking not found: " + bookingId
                    ));
        } else {
            booking = bookingRepository
                    .findByIdAndResident_User_EmailIgnoreCase(
                            bookingId,
                            authentication.getName()
                    )
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Booking not found: " + bookingId
                    ));
        }

        return toResponse(booking);
    }

    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request,Authentication authentication) {
        validateDates(request.startAt(), request.endAt());

        CommonArea commonArea = commonAreaRepository
                .findByIdAndBuildingIdAndActiveTrue(
                        request.commonAreaId(),
                        request.buildingId()
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Active common area not found"
                ));

        Unit unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unit not found: " + request.unitId()
                ));

        if (!unit.isActive()) {
            throw new IllegalStateException("Unit is inactive");
        }

        if (!unit.getBuilding().getId().equals(request.buildingId())) {
            throw new IllegalArgumentException(
                    "Unit does not belong to the requested building"
            );
        }

        Resident resident = residentRepository.findById(request.residentId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Resident not found: " + request.residentId()
                ));

        if (!resident.isActive()) {
            throw new IllegalStateException("Resident is inactive");
        }

        if (!resident.getUnit().getId().equals(unit.getId())) {
            throw new IllegalArgumentException(
                    "Resident does not belong to the requested unit"
            );
        }

        User currentUser = getAuthenticatedUser(authentication);

        Booking booking = new Booking();
        booking.setBuilding(unit.getBuilding());
        booking.setCommonArea(commonArea);
        booking.setUnit(unit);
        booking.setResident(resident);
        booking.setStartAt(request.startAt());
        booking.setEndAt(request.endAt());
        booking.setPurpose(normalizeOptional(request.purpose()));
        booking.setUpdatedBy(currentUser.getId());

        booking.setStatus(Enums.BookingStatus.APPROVED);

        try {
            return toResponse(bookingRepository.saveAndFlush(booking));
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException(
                    "The common area is already booked for the selected time"
            );
        }
    }

    @Transactional
    public BookingResponse updateStatus(
            UUID bookingId,
            BookingStatusUpdateRequest request,
            Authentication authentication
    ) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Booking not found: " + bookingId
                ));

        boolean fullUpdate = permissionService.hasPermission(
                authentication,
                "BOOKINGS_UPDATE"
        );

        boolean ownCancellation =
                request.status() == Enums.BookingStatus.CANCELLED
                && permissionService.hasPermission(
                        authentication,
                        "BOOKINGS_CANCEL_OWN"
                )
                && booking.getResident() != null
                && booking.getResident()
                        .getUser()
                        .getEmail()
                        .equalsIgnoreCase(authentication.getName());

        if (!fullUpdate && !ownCancellation) {
            throw new SecurityException(
                    "You are not allowed to update this booking"
            );
        }

        if (booking.getStatus() == Enums.BookingStatus.CANCELLED
                || booking.getStatus() == Enums.BookingStatus.COMPLETED
                || booking.getStatus() == Enums.BookingStatus.NO_SHOW) {
            throw new IllegalStateException(
                    "Booking cannot be updated from current status"
            );
        }

        User currentUser = getAuthenticatedUser(authentication);

        booking.setStatus(request.status());
        booking.setCancellationReason(
                normalizeOptional(request.cancellationReason())
        );
        booking.setUpdatedBy(currentUser.getId());

        return toResponse(bookingRepository.save(booking));
    }

    private void validateDates(
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {
        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException(
                    "endAt must be after startAt"
            );
        }

        if (startAt.isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException(
                    "Booking cannot start in the past"
            );
        }
    }

    private User getAuthenticatedUser(
            Authentication authentication
    ) {
        if (authentication == null
                || authentication.getName() == null) {
            throw new IllegalStateException(
                    "Authenticated user is required"
            );
        }

        return userRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found"
                ));
    }

    private BookingResponse toResponse(Booking booking) {
        Resident resident = booking.getResident();

        String residentName = resident == null
                || resident.getUser() == null
                ? null
                : resident.getUser().getFirstName()
                    + " "
                    + resident.getUser().getLastName();

        return new BookingResponse(
                booking.getId(),
                booking.getBuilding().getId(),
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
                booking.getApprovedByStaff() == null
                        ? null
                        : booking.getApprovedByStaff().getId(),
                booking.getApprovedAt(),
                booking.getCancellationReason()
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