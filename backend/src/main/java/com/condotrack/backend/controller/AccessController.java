//------------------- milestone 14 ---------------------
// package com.condotrack.backend.controller;

// import com.condotrack.backend.dto.AccessLogResponse;
// import com.condotrack.backend.dto.VisitorAuthorizationCreateRequest;
// import com.condotrack.backend.dto.VisitorAuthorizationResponse;
// import com.condotrack.backend.service.AccessAccessService;
// import com.condotrack.backend.service.AccessService;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.HttpStatus;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.security.core.Authentication;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.ResponseStatus;
// import org.springframework.web.bind.annotation.RestController;

// import java.util.UUID;

// @RestController
// @RequestMapping("/api/access")
// @RequiredArgsConstructor
// public class AccessController {

//     private final AccessService accessService;
//     private final AccessAccessService accessAccessService;

//     @PostMapping("/visitor-authorizations")
//     @ResponseStatus(HttpStatus.CREATED)
//     @PreAuthorize("@accessAccessService.canCreateVisitorAuthorization(authentication, #request.unitId, #request.residentId)")
//     public VisitorAuthorizationResponse createVisitorAuthorization(
//             @Valid @RequestBody VisitorAuthorizationCreateRequest request,
//             Authentication authentication
//     ) {
//         return accessService.createAuthorization(request, authentication);
//     }

//     @PostMapping("/visitor-authorizations/{authorizationId}/check-in")
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'ACCESS_CREATE')")
//     public AccessLogResponse checkIn(
//             @PathVariable UUID authorizationId,
//             Authentication authentication
//     ) {
//         return accessService.checkIn(authorizationId, authentication);
//     }
// }


//------------------- milestone 14.1 ---------------------
// package com.condotrack.backend.controller;

// import com.condotrack.backend.dto.AccessLogResponse;
// import com.condotrack.backend.dto.QrAccessRequest;
// import com.condotrack.backend.dto.VisitorAuthorizationCreateRequest;
// import com.condotrack.backend.dto.VisitorAuthorizationPageResponse;
// import com.condotrack.backend.dto.VisitorAuthorizationResponse;
// import com.condotrack.backend.service.AccessAccessService;
// import com.condotrack.backend.service.AccessService;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.web.PageableDefault;
// import org.springframework.http.HttpStatus;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.security.core.Authentication;
// import org.springframework.web.bind.annotation.*;

// import java.util.UUID;

// @RestController
// @RequestMapping("/api/access")
// @RequiredArgsConstructor
// public class AccessController {

//     private final AccessService accessService;
//     private final AccessAccessService accessAccessService;

//     @PostMapping("/visitor-authorizations")
//     @ResponseStatus(HttpStatus.CREATED)
//     @PreAuthorize("@accessAccessService.canCreateVisitorAuthorization(authentication, #request.unitId, #request.residentId)")
//     public VisitorAuthorizationResponse createVisitorAuthorization(
//             @Valid @RequestBody VisitorAuthorizationCreateRequest request,
//             Authentication authentication
//     ) {
//         return accessService.createAuthorization(request, authentication);
//     }

//     @GetMapping("/visitor-authorizations")
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'ACCESS_VIEW') || @permissionService.hasPermission(authentication, 'ACCESS_VIEW_OWN')")
//     public VisitorAuthorizationPageResponse listVisitorAuthorizations(
//             Authentication authentication,
//             @RequestParam(required = false) String status,
//             @PageableDefault(size = 20) Pageable pageable
//     ) {
//         return accessService.listAuthorizations(authentication, pageable, status);
//     }

//     @GetMapping("/visitor-authorizations/{authorizationId}")
//     @PreAuthorize("@accessAccessService.canViewAuthorization(authentication, #authorizationId)")
//     public VisitorAuthorizationResponse getVisitorAuthorization(
//             @PathVariable UUID authorizationId
//     ) {
//         return accessService.getAuthorization(authorizationId);
//     }

//     @PostMapping("/visitor-authorizations/{authorizationId}/check-in")
//     @PreAuthorize("@accessAccessService.canOperateStaff(authentication)")
//     public AccessLogResponse checkIn(
//             @PathVariable UUID authorizationId,
//             Authentication authentication
//     ) {
//         return accessService.checkIn(authorizationId, authentication);
//     }

//     @PostMapping("/visitor-authorizations/check-in")
//     @PreAuthorize("@accessAccessService.canOperateStaff(authentication)")
//     public AccessLogResponse checkInByQrToken(
//             @Valid @RequestBody QrAccessRequest request,
//             Authentication authentication
//     ) {
//         return accessService.checkInByQrToken(request.qrToken(), authentication);
//     }

//     @PostMapping("/visitor-authorizations/{authorizationId}/check-out")
//     @PreAuthorize("@accessAccessService.canOperateStaff(authentication)")
//     public AccessLogResponse checkOut(
//             @PathVariable UUID authorizationId,
//             Authentication authentication
//     ) {
//         return accessService.checkOut(authorizationId, authentication);
//     }

//     @PostMapping("/visitor-authorizations/check-out")
//     @PreAuthorize("@accessAccessService.canOperateStaff(authentication)")
//     public AccessLogResponse checkOutByQrToken(
//             @Valid @RequestBody QrAccessRequest request,
//             Authentication authentication
//     ) {
//         return accessService.checkOutByQrToken(request.qrToken(), authentication);
//     }

//     @GetMapping("/active-visitors")
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'ACCESS_VIEW') || @permissionService.hasPermission(authentication, 'ACCESS_VIEW_OWN')")
//     public org.springframework.data.domain.Page<AccessLogResponse> listActiveVisitors(
//             Authentication authentication,
//             @PageableDefault(size = 20) Pageable pageable
//     ) {
//         return accessService.listActiveVisitors(authentication, pageable);
//     }
// }


//--------------------- milestone 20 locl fix ----------------------------
 package com.condotrack.backend.controller;

import com.condotrack.backend.dto.AccessLogResponse;
import com.condotrack.backend.dto.VisitorAuthorizationCreateRequest;
import com.condotrack.backend.dto.VisitorAuthorizationResponse;
import com.condotrack.backend.service.AccessAccessService;
import com.condotrack.backend.service.AccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/access")
@RequiredArgsConstructor
public class AccessController {

    private final AccessService accessService;
    private final AccessAccessService accessAccessService;

    @PostMapping("/visitor-authorizations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@accessAccessService.canCreateVisitorAuthorization(authentication, #request.unitId, #request.residentId)")
    public VisitorAuthorizationResponse createVisitorAuthorization(
            @Valid @RequestBody VisitorAuthorizationCreateRequest request,
            Authentication authentication
    ) {
        return accessService.createAuthorization(request, authentication);
    }

    @PostMapping("/visitor-authorizations/{authorizationId}/check-in")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'ACCESS_CREATE')")
    public AccessLogResponse checkIn(
            @PathVariable UUID authorizationId,
            Authentication authentication
    ) {
        return accessService.checkIn(authorizationId, authentication);
    }
}
