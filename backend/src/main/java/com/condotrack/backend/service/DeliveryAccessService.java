package com.condotrack.backend.service;

import com.condotrack.backend.repository.DeliveryRepository;
import com.condotrack.backend.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("deliveryAccessService")
@RequiredArgsConstructor
public class DeliveryAccessService {

    private final PermissionService permissionService;
    private final DeliveryRepository deliveryRepository;
    private final ResidentRepository residentRepository;

    @Transactional(readOnly = true)
    public boolean canView(Authentication authentication, UUID deliveryId) {
        if (authentication == null || !authentication.isAuthenticated() || deliveryId == null) {
            return false;
        }

        if (permissionService.hasPermission(authentication, "DELIVERIES_VIEW")) {
            return deliveryRepository.existsById(deliveryId);
        }

        if (!permissionService.hasPermission(authentication, "DELIVERIES_VIEW_OWN")) {
            return false;
        }

        return deliveryRepository.existsByIdAndResident_User_EmailIgnoreCase(
                deliveryId,
                authentication.getName()
        );
    }

    @Transactional(readOnly = true)
    public boolean canCreate(
            Authentication authentication,
            UUID unitId,
            UUID residentId
    ) {
        if (authentication == null || !authentication.isAuthenticated()
                || unitId == null || residentId == null) {
            return false;
        }

        if (!permissionService.hasPermission(authentication, "DELIVERIES_CREATE")) {
            return false;
        }

        return residentRepository.existsByIdAndUnitIdAndActiveTrue(residentId, unitId);
    }

    public boolean canUpdate(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && permissionService.hasPermission(authentication, "DELIVERIES_UPDATE");
    }
}
