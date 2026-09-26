//------------------ original PR32 ------------------------
// package com.condotrack.backend.controller;

// import com.condotrack.backend.dto.BookingCreateRequest;
// import com.condotrack.backend.dto.BookingResponse;
// import com.condotrack.backend.dto.BookingStatusUpdateRequest;
// import com.condotrack.backend.service.BookingService;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.HttpStatus;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.security.core.Authentication;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;
// import java.util.UUID;

// @RestController
// @RequestMapping("/api/bookings")
// @RequiredArgsConstructor
// public class BookingController {

//     private final BookingService bookingService;

//     @GetMapping
//     @PreAuthorize("""
//         @permissionService.hasPermission(authentication, 'BOOKINGS_VIEW')
//         || @permissionService.hasPermission(authentication, 'BOOKINGS_VIEW_OWN')
//     """)
//     public List<BookingResponse> getBookings(Authentication authentication) {
//         return bookingService.getBookings(authentication);
//     }

//     @GetMapping("/{bookingId}")
//     @PreAuthorize("@bookingAccessService.canView(authentication, #bookingId)")
//     public BookingResponse getBooking(@PathVariable UUID bookingId,Authentication authentication) {
//         return bookingService.getBooking(bookingId, authentication);
//     }

//     @PostMapping
//     @ResponseStatus(HttpStatus.CREATED)
//     @PreAuthorize("""
//         @bookingAccessService.canCreate(authentication, #request.unitId, #request.residentId)
//     """)
//     public BookingResponse createBooking(
//             @Valid @RequestBody BookingCreateRequest request,
//             Authentication authentication
//     ) {
//         return bookingService.createBooking(request, authentication);
//     }

//     @PutMapping("/{bookingId}/status")
//     @PreAuthorize("""
//         @permissionService.hasPermission(authentication, 'BOOKINGS_UPDATE')
//         || @permissionService.hasPermission(authentication, 'BOOKINGS_CANCEL_OWN')
//     """)
//     public BookingResponse updateStatus(
//             @PathVariable UUID bookingId,
//             @Valid @RequestBody BookingStatusUpdateRequest request,
//             Authentication authentication
//     ) {
//         return bookingService.updateStatus(bookingId,request,authentication);
//     }
// }


//------------------ new PR32 M21 ------------------------
package com.condotrack.backend.controller;

import com.condotrack.backend.dto.BookingCreateRequest;
import com.condotrack.backend.dto.BookingPageResponse;
import com.condotrack.backend.dto.BookingResponse;
import com.condotrack.backend.dto.BookingStatusUpdateRequest;
import com.condotrack.backend.dto.BookingUpdateRequest;
import com.condotrack.backend.service.BookingAccessService;
import com.condotrack.backend.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    @PreAuthorize("@bookingAccessService.canViewCollection(authentication)")
    @Operation(summary = "List bookings")
    public BookingPageResponse getBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        return bookingService.getBookings(authentication, page, size);
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("@bookingAccessService.canView(authentication, #bookingId)")
    @Operation(summary = "Get booking")
    public BookingResponse getBooking(@PathVariable UUID bookingId) {
        return bookingService.getBooking(bookingId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@bookingAccessService.canCreate(authentication, #request.unitId, #request.residentId)")
    @Operation(summary = "Create booking")
    public BookingResponse createBooking(
            @Valid @RequestBody BookingCreateRequest request,
            Authentication authentication
    ) {
        return bookingService.createBooking(request, authentication);
    }

    @PutMapping("/{bookingId}")
    @PreAuthorize("@bookingAccessService.canUpdate(authentication, #bookingId)")
    @Operation(summary = "Update booking schedule")
    public BookingResponse updateBooking(
            @PathVariable UUID bookingId,
            @Valid @RequestBody BookingUpdateRequest request,
            Authentication authentication
    ) {
        return bookingService.updateBooking(bookingId, request, authentication);
    }
    @PutMapping("/{bookingId}/status")
    @PreAuthorize("@bookingAccessService.canUpdateStatus(authentication, #bookingId, #request.status)")
    @Operation(summary = "Update booking status")
    public BookingResponse updateStatus(
            @PathVariable UUID bookingId,
            @Valid @RequestBody BookingStatusUpdateRequest request,
            Authentication authentication
    ) {
        return bookingService.updateStatus(bookingId, request, authentication);
    }
}
