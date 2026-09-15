//--------------- milestone 6 ---------------------
// package com.condotrack.backend.controller;

// import com.condotrack.backend.dto.ResidentResponse;
// import com.condotrack.backend.dto.ResidentUpdateRequest;
// import com.condotrack.backend.service.ResidentAccessService;
// import com.condotrack.backend.service.ResidentService;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import java.util.UUID;

// @RestController
// @RequestMapping("/api/residents")
// @RequiredArgsConstructor
// public class ResidentController {

//     private final ResidentAccessService residentAccessService;
//     private final ResidentService residentService;

//     @GetMapping("/{residentId}")
//     @PreAuthorize("@residentAccessService.canView(authentication, #residentId)")
//     public ResidentResponse getResident(@PathVariable UUID residentId) {
//         return residentService.getResident(residentId);
//     }

//     @PutMapping("/{residentId}")
//     @PreAuthorize("@residentAccessService.canUpdate(authentication, #residentId)")
//     public ResidentResponse updateResident(
//             @PathVariable UUID residentId,
//             @Valid @RequestBody ResidentUpdateRequest request
//     ) {
//         return residentService.updateResident(residentId, request);
//     }
// }


// ----------------- milestone 7 ---------------------
// package com.condotrack.backend.controller;

// import com.condotrack.backend.dto.ResidentPageResponse;
// import com.condotrack.backend.dto.ResidentResponse;
// import com.condotrack.backend.dto.ResidentUpdateRequest;
// import com.condotrack.backend.service.ResidentAccessService;
// import com.condotrack.backend.service.ResidentService;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.web.PageableDefault;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import java.util.UUID;

// @RestController
// @RequestMapping("/api/residents")
// @RequiredArgsConstructor
// public class ResidentController {

//     private final ResidentAccessService residentAccessService;
//     private final ResidentService residentService;

//     @GetMapping
//     @PreAuthorize("@residentAccessService.canList(authentication)")
//     public ResidentPageResponse listResidents(
//             Authentication authentication,
//             @PageableDefault(size = 20) Pageable pageable
//     ) {
//         return residentService.listResidents(authentication, pageable);
//     }

//     @GetMapping("/{residentId}")
//     @PreAuthorize("@residentAccessService.canView(authentication, #residentId)")
//     public ResidentResponse getResident(@PathVariable UUID residentId) {
//         return residentService.getResident(residentId);
//     }

//     @PutMapping("/{residentId}")
//     @PreAuthorize("@residentAccessService.canUpdate(authentication, #residentId)")
//     public ResidentResponse updateResident(
//             @PathVariable UUID residentId,
//             @Valid @RequestBody ResidentUpdateRequest request
//     ) {
//         return residentService.updateResident(residentId, request);
//     }
// }


//----------------- milestone 12 ----------------------
package com.condotrack.backend.controller;

import com.condotrack.backend.dto.ResidentCreateRequest;
import com.condotrack.backend.dto.ResidentPageResponse;
import com.condotrack.backend.dto.ResidentResponse;
import com.condotrack.backend.dto.ResidentUpdateRequest;
import com.condotrack.backend.dto.ResidentUnitAssignmentUpdateRequest;
import com.condotrack.backend.service.ResidentAccessService;
import com.condotrack.backend.service.ResidentService;
import com.condotrack.backend.service.ResidentAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@RestController
@RequestMapping("/api/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentAccessService residentAccessService;
    private final ResidentService residentService;
    private final ResidentAssignmentService residentAssignmentService;

    @GetMapping
    @PreAuthorize("@residentAccessService.canList(authentication)")
    public ResidentPageResponse listResidents(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return residentService.listResidents(authentication, pageable);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionService.hasPermission(authentication, 'RESIDENTS_CREATE')")
    public ResidentResponse assignResident(
            @Valid @RequestBody ResidentCreateRequest request,
            Authentication authentication
    ) {
        return residentAssignmentService.assignResident(
                request,
                residentAssignmentService.getAuthenticatedUserId(authentication)
        );
    }

    @PutMapping("/{residentId}/unit")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'RESIDENTS_UPDATE')")
    public ResidentResponse reassignResident(
            @PathVariable UUID residentId,
            @Valid @RequestBody ResidentUnitAssignmentUpdateRequest request,
            Authentication authentication
    ) {
        return residentAssignmentService.reassignResident(
                residentId,
                request,
                residentAssignmentService.getAuthenticatedUserId(authentication)
        );
    }

    @GetMapping("/{residentId}")
    @PreAuthorize("@residentAccessService.canView(authentication, #residentId)")
    public ResidentResponse getResident(@PathVariable UUID residentId) {
        return residentService.getResident(residentId);
    }

    @PutMapping("/{residentId}")
    @PreAuthorize("@residentAccessService.canUpdate(authentication, #residentId)")
    public ResidentResponse updateResident(
            @PathVariable UUID residentId,
            @Valid @RequestBody ResidentUpdateRequest request
    ) {
        return residentService.updateResident(residentId, request);
    }
}