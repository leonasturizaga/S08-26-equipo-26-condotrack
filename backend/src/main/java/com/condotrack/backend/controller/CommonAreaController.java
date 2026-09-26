//------------------ original PR32 ------------------------
// package com.condotrack.backend.controller;

// import com.condotrack.backend.dto.CommonAreaResponse;
// import com.condotrack.backend.service.CommonAreaService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import java.util.List;
// import java.util.UUID;

// @RestController
// @RequestMapping("/api/common-areas")
// @RequiredArgsConstructor
// public class CommonAreaController {

//     private final CommonAreaService commonAreaService;

//     @GetMapping
//     @PreAuthorize("""
//         @permissionService.hasPermission(authentication, 'BOOKINGS_VIEW')
//         || @permissionService.hasPermission(authentication, 'BOOKINGS_VIEW_OWN')
//     """)
//     public List<CommonAreaResponse> getCommonAreas() {
//         return commonAreaService.getActiveCommonAreas();
//     }

//     @GetMapping("/{commonAreaId}")
//     @PreAuthorize("""
//         @permissionService.hasPermission(authentication, 'BOOKINGS_VIEW')
//         || @permissionService.hasPermission(authentication, 'BOOKINGS_VIEW_OWN')
//     """)
//     public CommonAreaResponse getCommonArea(
//             @PathVariable UUID commonAreaId
//     ) {
//         return commonAreaService.getActiveCommonArea(commonAreaId);
//     }
// }


//------------------ new PR32 M21 ------------------------
package com.condotrack.backend.controller;

import com.condotrack.backend.dto.CommonAreaResponse;
import com.condotrack.backend.service.CommonAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/common-areas")
@RequiredArgsConstructor
public class CommonAreaController {

    private final CommonAreaService commonAreaService;

    @GetMapping
    @PreAuthorize("@bookingAccessService.canViewCollection(authentication)")
    public Page<CommonAreaResponse> getCommonAreas(
            @RequestParam(required = false) UUID buildingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return commonAreaService.getCommonAreas(buildingId, page, size);
    }

    @GetMapping("/{commonAreaId}")
    @PreAuthorize("@bookingAccessService.canViewCollection(authentication)")
    public CommonAreaResponse getCommonArea(@PathVariable UUID commonAreaId) {
        return commonAreaService.getCommonArea(commonAreaId);
    }
}
