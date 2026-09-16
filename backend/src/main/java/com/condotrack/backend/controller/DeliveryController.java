package com.condotrack.backend.controller;

import com.condotrack.backend.dto.DeliveryCreateRequest;
import com.condotrack.backend.dto.DeliveryPageResponse;
import com.condotrack.backend.dto.DeliveryResponse;
import com.condotrack.backend.dto.DeliveryStatusUpdateRequest;
import com.condotrack.backend.service.DeliveryAccessService;
import com.condotrack.backend.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DeliveryAccessService deliveryAccessService;

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, 'DELIVERIES_VIEW') || @permissionService.hasPermission(authentication, 'DELIVERIES_VIEW_OWN')")
    public DeliveryPageResponse getDeliveries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        return deliveryService.getDeliveries(authentication, page, size);
    }

    @GetMapping("/{deliveryId}")
    @PreAuthorize("@deliveryAccessService.canView(authentication, #deliveryId)")
    public DeliveryResponse getDelivery(
            @PathVariable UUID deliveryId,
            Authentication authentication
    ) {
        return deliveryService.getDelivery(deliveryId, authentication);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@deliveryAccessService.canCreate(authentication, #request.unitId, #request.residentId)")
    public DeliveryResponse createDelivery(
            @Valid @RequestBody DeliveryCreateRequest request,
            Authentication authentication
    ) {
        return deliveryService.createDelivery(request, authentication);
    }

    @PutMapping("/{deliveryId}/status")
    @PreAuthorize("@deliveryAccessService.canUpdate(authentication)")
    public DeliveryResponse updateStatus(
            @PathVariable UUID deliveryId,
            @Valid @RequestBody DeliveryStatusUpdateRequest request,
            Authentication authentication
    ) {
        return deliveryService.updateStatus(deliveryId, request, authentication);
    }
}
