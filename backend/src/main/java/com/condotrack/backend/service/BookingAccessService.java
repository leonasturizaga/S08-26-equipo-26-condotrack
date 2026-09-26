//------------------ original PR32 ------------------------
// package com.condotrack.backend.service;

// import com.condotrack.backend.repository.BookingRepository;
// import com.condotrack.backend.repository.ResidentRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.security.core.Authentication;

// import java.util.UUID;

// @Service("bookingAccessService")
// @RequiredArgsConstructor
// public class BookingAccessService {

//     private final PermissionService permissionService;
//     private final BookingRepository bookingRepository;
//     private final ResidentRepository residentRepository;

//     @Transactional(readOnly = true)
//     public boolean canView(
//             Authentication authentication,
//             UUID bookingId
//     ) {
//         if (authentication == null
//                 || !authentication.isAuthenticated()
//                 || bookingId == null) {
//             return false;
//         }

//         if (permissionService.hasPermission(authentication, "BOOKINGS_VIEW")) {
//             return bookingRepository.existsById(bookingId);
//         }

//         if (!permissionService.hasPermission(authentication, "BOOKINGS_VIEW_OWN")) {
//             return false;
//         }

//         return bookingRepository.existsByIdAndResident_User_EmailIgnoreCase(
//                 bookingId,
//                 authentication.getName()
//         );
//     }

//     public boolean canCreate(
//             Authentication authentication,
//             UUID unitId,
//             UUID residentId
//     ) {
//         if (authentication == null
//                 || !authentication.isAuthenticated()
//                 || unitId == null
//                 || residentId == null) {
//             return false;
//         }

//         if (permissionService.hasPermission(authentication, "BOOKINGS_CREATE")) {
//             return residentRepository
//                     .existsByIdAndUnitIdAndActiveTrue(residentId, unitId);
//         }

//         if (!permissionService.hasPermission(
//                 authentication,
//                 "BOOKINGS_CREATE_OWN"
//         )) {
//             return false;
//         }

//         return residentRepository
//                 .existsActiveResidentForUserAndUnit(
//                         authentication.getName(),
//                         unitId
//                 )
//                 && residentRepository
//                 .existsActiveResidentForUserAndResidentId(
//                         authentication.getName(),
//                         residentId
//                 );
//     }

//     public boolean canUpdateStatus(
//             Authentication authentication,
//             UUID bookingId,
//             String targetStatus
//     ) {
//         if (authentication == null
//                 || !authentication.isAuthenticated()
//                 || bookingId == null
//                 || targetStatus == null) {
//             return false;
//         }

//         if (permissionService.hasPermission(authentication, "BOOKINGS_UPDATE")) {
//             return true;
//         }

//         return targetStatus.equals("CANCELLED")
//                 && permissionService.hasPermission(
//                 authentication,
//                 "BOOKINGS_CANCEL_OWN"
//         )
//                 && bookingRepository
//                 .existsByIdAndResident_User_EmailIgnoreCase(
//                         bookingId,
//                         authentication.getName()
//                 );
//     }
// }


//------------------ new PR32 M21 ------------------------
package com.condotrack.backend.service;

import com.condotrack.backend.model.Enums;
import com.condotrack.backend.model.Unit;
import com.condotrack.backend.repository.BookingRepository;
import com.condotrack.backend.repository.ResidentRepository;
import com.condotrack.backend.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("bookingAccessService")
@RequiredArgsConstructor
public class BookingAccessService {

    private final PermissionService permissionService;
    private final BookingRepository bookingRepository;
    private final ResidentRepository residentRepository;
    private final UnitRepository unitRepository;

    public boolean canViewCollection(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && (permissionService.hasPermission(authentication, "BOOKINGS_VIEW")
                || permissionService.hasPermission(authentication, "BOOKINGS_VIEW_OWN"));
    }

    @Transactional(readOnly = true)
    public boolean canView(Authentication authentication, UUID bookingId) {
        if (authentication == null || !authentication.isAuthenticated() || bookingId == null) {
            return false;
        }

        if (permissionService.hasPermission(authentication, "BOOKINGS_VIEW")) {
            return bookingRepository.existsById(bookingId);
        }

        return permissionService.hasPermission(authentication, "BOOKINGS_VIEW_OWN")
                && bookingRepository.existsByIdAndResident_User_EmailIgnoreCase(
                bookingId,
                authentication.getName()
        );
    }

    @Transactional(readOnly = true)
    public boolean canCreate(Authentication authentication, UUID unitId, UUID residentId) {
        if (authentication == null || !authentication.isAuthenticated()
                || unitId == null || residentId == null) {
            return false;
        }

        Unit unit = unitRepository.findById(unitId).orElse(null);
        if (unit == null || unit.getBuilding() == null || !unit.isActive() || !unit.getBuilding().isActive()) {
            return false;
        }

        boolean broadCreate = permissionService.hasPermission(
                authentication,
                "BOOKINGS_CREATE",
                unit.getBuilding().getId()
        );

        if (broadCreate) {
            return residentRepository.existsByIdAndUnitIdAndActiveTrue(residentId, unitId);
        }

        if (!permissionService.hasPermission(authentication, "BOOKINGS_CREATE_OWN")) {
            return false;
        }

        return residentRepository.existsByIdAndUnitIdAndActiveTrue(residentId, unitId)
                && residentRepository.existsActiveResidentForUserAndResidentId(
                authentication.getName(),
                residentId
        );
    }

    @Transactional(readOnly = true)
    public boolean canUpdateStatus(
            Authentication authentication,
            UUID bookingId,
            Enums.BookingStatus requestedStatus
    ) {
        if (authentication == null || !authentication.isAuthenticated()
                || bookingId == null || requestedStatus == null) {
            return false;
        }

        if (permissionService.hasPermission(authentication, "BOOKINGS_UPDATE")) {
            return bookingRepository.existsById(bookingId);
        }

        return requestedStatus == Enums.BookingStatus.CANCELLED
                && permissionService.hasPermission(authentication, "BOOKINGS_CANCEL_OWN")
                && bookingRepository.existsByIdAndResident_User_EmailIgnoreCase(
                bookingId,
                authentication.getName()
        );
    }
}
