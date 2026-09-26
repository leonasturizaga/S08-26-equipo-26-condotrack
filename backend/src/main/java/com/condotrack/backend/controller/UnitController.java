//-------------- milestone 7 ---------------------
// package com.condotrack.backend.controller;

// import com.condotrack.backend.dto.UnitResponse;
// import com.condotrack.backend.service.UnitAccessService;
// import com.condotrack.backend.service.UnitService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import java.util.UUID;

// @RestController
// @RequestMapping("/api/units")
// @RequiredArgsConstructor
// public class UnitController {

//     private final UnitAccessService unitAccessService;
//     private final UnitService unitService;

//     @GetMapping("/{unitId}")
//     @PreAuthorize("@unitAccessService.canView(authentication, #unitId)")
//     public UnitResponse getUnit(@PathVariable UUID unitId) {
//         return unitService.getUnit(unitId);
//     }
// }


//---------------- milestone 8 ---------------------
// package com.condotrack.backend.controller;

// import com.condotrack.backend.dto.UnitPageResponse;
// import com.condotrack.backend.dto.UnitResponse;
// import com.condotrack.backend.service.UnitAccessService;
// import com.condotrack.backend.service.UnitService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.security.core.Authentication;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import java.util.UUID;

// @RestController
// @RequestMapping("/api/units")
// @RequiredArgsConstructor
// public class UnitController {

//     private final UnitAccessService unitAccessService;
//     private final UnitService unitService;

//     @GetMapping
//     @PreAuthorize("@unitCollectionAccessService.canViewCollection(authentication)")
//     public UnitPageResponse getUnits(
//             Authentication authentication,
//             @RequestParam(defaultValue = "0") int page,
//             @RequestParam(defaultValue = "20") int size
//     ) {
//         return unitService.getUnits(authentication, page, size);
//     }

//     @GetMapping("/{unitId}")
//     @PreAuthorize("@unitAccessService.canView(authentication, #unitId)")
//     public UnitResponse getUnit(@PathVariable UUID unitId) {
//         return unitService.getUnit(unitId);
//     }
// }

//---------------- milestone 23 ---------------------
package com.condotrack.backend.controller;

import com.condotrack.backend.dto.UnitCreateRequest;
import com.condotrack.backend.dto.UnitPageResponse;
import com.condotrack.backend.dto.UnitLookupResponse;
import com.condotrack.backend.dto.UnitResponse;
import com.condotrack.backend.dto.UnitUpdateRequest;
import com.condotrack.backend.service.UnitAccessService;
import com.condotrack.backend.service.UnitService;
import com.condotrack.backend.service.UnitLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitAccessService unitAccessService;
    private final UnitService unitService;
    private final UnitLookupService unitLookupService;

    @GetMapping
    @PreAuthorize("@unitCollectionAccessService.canViewCollection(authentication)")
    public UnitPageResponse getUnits(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return unitService.getUnits(authentication, page, size);
    }

    @GetMapping("/lookup")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'UNITS_VIEW')")
    public List<UnitLookupResponse> lookupUnits(@RequestParam String query) {
        return unitLookupService.search(query);
    }
    @GetMapping("/{unitId}")
    @PreAuthorize("@unitAccessService.canView(authentication, #unitId)")
    public UnitResponse getUnit(@PathVariable UUID unitId) {
        return unitService.getUnit(unitId);
    }

    @PostMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, 'UNITS_CREATE')")
    public UnitResponse createUnit(
            @Valid @RequestBody UnitCreateRequest request,
            Authentication authentication
    ) {
        return unitService.createUnit(request, authentication);
    }

    @PutMapping("/{unitId}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'UNITS_UPDATE')")
    public UnitResponse updateUnit(
            @PathVariable UUID unitId,
            @Valid @RequestBody UnitUpdateRequest request,
            Authentication authentication
    ) {
        return unitService.updateUnit(unitId, request, authentication);
    }
}