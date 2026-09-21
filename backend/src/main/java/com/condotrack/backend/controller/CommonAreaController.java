package com.condotrack.backend.controller;

import com.condotrack.backend.dto.CommonAreaResponse;
import com.condotrack.backend.service.CommonAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/common-areas")
@RequiredArgsConstructor
public class CommonAreaController {

    private final CommonAreaService commonAreaService;

    @GetMapping
    @PreAuthorize("""
        @permissionService.hasPermission(authentication, 'BOOKINGS_VIEW')
        || @permissionService.hasPermission(authentication, 'BOOKINGS_VIEW_OWN')
    """)
    public List<CommonAreaResponse> getCommonAreas() {
        return commonAreaService.getActiveCommonAreas();
    }

    @GetMapping("/{commonAreaId}")
    @PreAuthorize("""
        @permissionService.hasPermission(authentication, 'BOOKINGS_VIEW')
        || @permissionService.hasPermission(authentication, 'BOOKINGS_VIEW_OWN')
    """)
    public CommonAreaResponse getCommonArea(
            @PathVariable UUID commonAreaId
    ) {
        return commonAreaService.getActiveCommonArea(commonAreaId);
    }
}