// //---------------------------- M24.1 ----------------------------
// //CommonAreaController.java
// package com.condotrack.backend.controller;

// import com.condotrack.backend.dto.CommonAreaCreateRequest;
// import com.condotrack.backend.dto.CommonAreaResponse;
// import com.condotrack.backend.dto.CommonAreaUpdateRequest;
// import com.condotrack.backend.service.CommonAreaService;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Page;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.ResponseStatus;
// import org.springframework.web.bind.annotation.RestController;

// import java.util.UUID;

// @RestController
// @RequestMapping("/api/common-areas")
// @RequiredArgsConstructor
// public class CommonAreaController {

//     private final CommonAreaService commonAreaService;

//     @GetMapping
//     @PreAuthorize("@bookingAccessService.canViewCollection(authentication)")
//     public Page<CommonAreaResponse> getCommonAreas(
//             @RequestParam(required = false) UUID buildingId,
//             @RequestParam(defaultValue = "0") int page,
//             @RequestParam(defaultValue = "20") int size
//     ) {
//         return commonAreaService.getCommonAreas(buildingId, page, size);
//     }

//     @GetMapping("/{commonAreaId}")
//     @PreAuthorize("@bookingAccessService.canViewCollection(authentication)")
//     public CommonAreaResponse getCommonArea(
//             @PathVariable UUID commonAreaId
//     ) {
//         return commonAreaService.getCommonArea(commonAreaId);
//     }

//     @PostMapping
//     @ResponseStatus(HttpStatus.CREATED)
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
//     public CommonAreaResponse createCommonArea(
//             @Valid @RequestBody CommonAreaCreateRequest request
//     ) {
//         return commonAreaService.createCommonArea(request);
//     }

//     @PutMapping("/{commonAreaId}")
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
//     public CommonAreaResponse updateCommonArea(
//             @PathVariable UUID commonAreaId,
//             @Valid @RequestBody CommonAreaUpdateRequest request
//     ) {
//         return commonAreaService.updateCommonArea(commonAreaId, request);
//     }

//     @DeleteMapping("/{commonAreaId}")
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
//     public ResponseEntity<Void> deactivateCommonArea(
//             @PathVariable UUID commonAreaId
//     ) {
//         commonAreaService.deactivateCommonArea(commonAreaId);
//         return ResponseEntity.noContent().build();
//     }
// }


//--------------------------- M24.2 --------------------------
package com.condotrack.backend.controller;

import com.condotrack.backend.dto.AmenityResponse;
import com.condotrack.backend.dto.CommonAreaCreateRequest;
import com.condotrack.backend.dto.CommonAreaResponse;
import com.condotrack.backend.dto.CommonAreaUpdateRequest;
import com.condotrack.backend.service.CommonAreaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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

    @GetMapping("/amenities")
    @PreAuthorize("@bookingAccessService.canViewCollection(authentication)")
    public List<AmenityResponse> getActiveAmenities() {
        return commonAreaService.getActiveAmenities();
    }

    @GetMapping("/{commonAreaId}")
    @PreAuthorize("@bookingAccessService.canViewCollection(authentication)")
    public CommonAreaResponse getCommonArea(
            @PathVariable UUID commonAreaId
    ) {
        return commonAreaService.getCommonArea(commonAreaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
    public CommonAreaResponse createCommonArea(
            @Valid @RequestBody CommonAreaCreateRequest request
    ) {
        return commonAreaService.createCommonArea(request);
    }

    @PutMapping("/{commonAreaId}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
    public CommonAreaResponse updateCommonArea(
            @PathVariable UUID commonAreaId,
            @Valid @RequestBody CommonAreaUpdateRequest request
    ) {
        return commonAreaService.updateCommonArea(commonAreaId, request);
    }

    @DeleteMapping("/{commonAreaId}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
    public ResponseEntity<Void> deactivateCommonArea(
            @PathVariable UUID commonAreaId
    ) {
        commonAreaService.deactivateCommonArea(commonAreaId);
        return ResponseEntity.noContent().build();
    }
}