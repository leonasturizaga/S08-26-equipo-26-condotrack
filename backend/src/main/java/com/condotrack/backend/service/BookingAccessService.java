package com.condotrack.backend.service;

import com.condotrack.backend.model.Booking;
import com.condotrack.backend.model.CommonArea;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.BookingRepository;
import com.condotrack.backend.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import java.util.UUID;

@Service("bookingAccessService")
@RequiredArgsConstructor
public class BookingAccessService {

    private final PermissionService permissionService;
    private final BookingRepository bookingRepository;
    private final ResidentRepository residentRepository;

    @Transactional(readOnly = true)
    public boolean canView(
            Authentication authentication,
            UUID bookingId
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || bookingId == null) {
            return false;
        }

        if (permissionService.hasPermission(authentication, "BOOKINGS_VIEW")) {
            return bookingRepository.existsById(bookingId);
        }

        if (!permissionService.hasPermission(authentication, "BOOKINGS_VIEW_OWN")) {
            return false;
        }

        return bookingRepository.existsByIdAndResident_User_EmailIgnoreCase(
                bookingId,
                authentication.getName()
        );
    }

    public boolean canCreate(
            Authentication authentication,
            UUID unitId,
            UUID residentId
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || unitId == null
                || residentId == null) {
            return false;
        }

        if (permissionService.hasPermission(authentication, "BOOKINGS_CREATE")) {
            return residentRepository
                    .existsByIdAndUnitIdAndActiveTrue(residentId, unitId);
        }

        if (!permissionService.hasPermission(
                authentication,
                "BOOKINGS_CREATE_OWN"
        )) {
            return false;
        }

        return residentRepository
                .existsActiveResidentForUserAndUnit(
                        authentication.getName(),
                        unitId
                )
                && residentRepository
                .existsActiveResidentForUserAndResidentId(
                        authentication.getName(),
                        residentId
                );
    }

    public boolean canUpdateStatus(
            Authentication authentication,
            UUID bookingId,
            String targetStatus
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || bookingId == null
                || targetStatus == null) {
            return false;
        }

        if (permissionService.hasPermission(authentication, "BOOKINGS_UPDATE")) {
            return true;
        }

        return targetStatus.equals("CANCELLED")
                && permissionService.hasPermission(
                authentication,
                "BOOKINGS_CANCEL_OWN"
        )
                && bookingRepository
                .existsByIdAndResident_User_EmailIgnoreCase(
                        bookingId,
                        authentication.getName()
                );
    }
}
