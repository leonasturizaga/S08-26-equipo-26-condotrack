//-------------- milestone 8 ---------------------
// package com.condotrack.backend.controller;

// import com.condotrack.backend.dto.BuildingResponse;
// import com.condotrack.backend.service.BuildingService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import java.util.List;

// @RestController
// @RequestMapping("/api/buildings")
// @RequiredArgsConstructor
// public class BuildingController {

//     private final BuildingService buildingService;

//     @GetMapping
//    //  @PreAuthorize("hasRole('ADMINISTRATOR')")    //milestone 3
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDINGS_VIEW')")   //milestone 4
//     public List<BuildingResponse> getAllBuildings() {
//         return buildingService.getAllBuildings();
//     }
// }


//---------------- milestone 9 ---------------------
// package com.condotrack.backend.controller;

// import com.condotrack.backend.dto.BuildingPageResponse;
// import com.condotrack.backend.dto.BuildingResponse;
// import com.condotrack.backend.service.BuildingService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import java.util.UUID;

// @RestController
// @RequestMapping("/api/buildings")
// @RequiredArgsConstructor
// public class BuildingController {

//     private final BuildingService buildingService;

//     @GetMapping
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDINGS_VIEW')")
//     public BuildingPageResponse getBuildings(
//             @RequestParam(defaultValue = "0") int page,
//             @RequestParam(defaultValue = "20") int size
//     ) {
//         return buildingService.getBuildings(page, size);
//     }

//     @GetMapping("/{buildingId}")
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDINGS_VIEW')")
//     public BuildingResponse getBuilding(@PathVariable UUID buildingId) {
//         return buildingService.getBuilding(buildingId);
//     }
// }


//----------------- milestone 10 ---------------------
package com.condotrack.backend.controller;

import com.condotrack.backend.dto.BuildingCreateRequest;
import com.condotrack.backend.dto.BuildingPageResponse;
import com.condotrack.backend.dto.BuildingResponse;
import com.condotrack.backend.dto.BuildingUpdateRequest;
import com.condotrack.backend.service.BuildingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
															  
															
															  

import java.util.UUID;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDINGS_VIEW')")
    public BuildingPageResponse getBuildings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return buildingService.getBuildings(page, size);
    }

    @GetMapping("/{buildingId}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDINGS_VIEW')")
    public BuildingResponse getBuilding(@PathVariable UUID buildingId) {
        return buildingService.getBuilding(buildingId);
    }

    @PostMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDINGS_CREATE')")
    public BuildingResponse createBuilding(
            @Valid @RequestBody BuildingCreateRequest request,
            Authentication authentication
    ) {
        return buildingService.createBuilding(request, authentication);
    }

    @PutMapping("/{buildingId}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDINGS_UPDATE')")
    public BuildingResponse updateBuilding(
            @PathVariable UUID buildingId,
            @Valid @RequestBody BuildingUpdateRequest request,
            Authentication authentication
    ) {
        return buildingService.updateBuilding(buildingId, request, authentication);
    }
}