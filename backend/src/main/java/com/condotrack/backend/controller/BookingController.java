package com.condotrack.backend.controller;

import com.condotrack.backend.dto.BookingCreateRequest;
import com.condotrack.backend.dto.BookingResponse;
import com.condotrack.backend.dto.BookingStatusUpdateRequest;
import com.condotrack.backend.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    @PreAuthorize("""
        @permissionService.hasPermission(authentication, 'BOOKINGS_VIEW')
        || @permissionService.hasPermission(authentication, 'BOOKINGS_VIEW_OWN')
    """)
    public List<BookingResponse> getBookings(Authentication authentication) {
        return bookingService.getBookings(authentication);
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("@bookingAccessService.canView(authentication, #bookingId)")
    public BookingResponse getBooking(@PathVariable UUID bookingId,Authentication authentication) {
        return bookingService.getBooking(bookingId, authentication);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("""
        @bookingAccessService.canCreate(authentication, #request.unitId, #request.residentId)
    """)
    public BookingResponse createBooking(
            @Valid @RequestBody BookingCreateRequest request,
            Authentication authentication
    ) {
        return bookingService.createBooking(request, authentication);
    }

    @PutMapping("/{bookingId}/status")
    @PreAuthorize("""
        @permissionService.hasPermission(authentication, 'BOOKINGS_UPDATE')
        || @permissionService.hasPermission(authentication, 'BOOKINGS_CANCEL_OWN')
    """)
    public BookingResponse updateStatus(
            @PathVariable UUID bookingId,
            @Valid @RequestBody BookingStatusUpdateRequest request,
            Authentication authentication
    ) {
        return bookingService.updateStatus(bookingId,request,authentication);
    }
}