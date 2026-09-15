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
package com.condotrack.backend.controller;

import com.condotrack.backend.dto.UnitPageResponse;
import com.condotrack.backend.dto.UnitResponse;
import com.condotrack.backend.service.UnitAccessService;
import com.condotrack.backend.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitAccessService unitAccessService;
    private final UnitService unitService;

    @GetMapping
    @PreAuthorize("@unitCollectionAccessService.canViewCollection(authentication)")
    public UnitPageResponse getUnits(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return unitService.getUnits(authentication, page, size);
    }

    @GetMapping("/{unitId}")
    @PreAuthorize("@unitAccessService.canView(authentication, #unitId)")
    public UnitResponse getUnit(@PathVariable UUID unitId) {
        return unitService.getUnit(unitId);
    }
}