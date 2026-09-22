package com.condotrack.backend.controller;

import com.condotrack.backend.dto.MoveCreateRequest;
import com.condotrack.backend.dto.MoveOwnerAuthorizationRequest;
import com.condotrack.backend.dto.MovePageResponse;
import com.condotrack.backend.dto.MoveResidentOptionResponse;
import com.condotrack.backend.dto.MoveResponse;
import com.condotrack.backend.dto.MoveStatusUpdateRequest;
import com.condotrack.backend.dto.MoveUnitOptionResponse;
import com.condotrack.backend.service.MoveAccessService;
import com.condotrack.backend.service.MoveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/moves")
@RequiredArgsConstructor
@Tag(name = "Move Requests", description = "Move-in and move-out request workflow and approval.")
public class MoveController {
    private final MoveService moveService;
    private final MoveAccessService moveAccessService;

    @GetMapping
    @PreAuthorize("@moveAccessService.canList(authentication)")
    @Operation(summary = "List move requests")
    public MovePageResponse getMoves(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        return moveService.getMoves(authentication, page, size);
    }

    @GetMapping("/{moveId}")
    @PreAuthorize("@moveAccessService.canView(authentication, #moveId)")
    @Operation(summary = "Get a move request")
    public MoveResponse getMove(@PathVariable UUID moveId) {
        return moveService.getMove(moveId);
    }

    @GetMapping("/options/units")
    @PreAuthorize("@moveAccessService.canCreateOptions(authentication)")
    @Operation(summary = "List units available for move request creation")
    public List<MoveUnitOptionResponse> getUnitOptions(Authentication authentication) {
        return moveService.getUnitOptions(authentication);
    }

    @GetMapping("/options/residents")
    @PreAuthorize("@moveAccessService.canCreateOptions(authentication)")
    @Operation(summary = "List active residents for a selected unit")
    public List<MoveResidentOptionResponse> getResidentOptions(@RequestParam UUID unitId, Authentication authentication) {
        return moveService.getResidentOptions(unitId, authentication);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@moveAccessService.canCreate(authentication, #request.unitId)")
    @Operation(summary = "Create a move request")
    public MoveResponse createMove(
            @Valid @RequestBody MoveCreateRequest request,
            Authentication authentication
    ) {
        return moveService.createMove(request, authentication);
    }

    @PutMapping("/{moveId}/owner-authorization")
    @PreAuthorize("@moveAccessService.canAuthorizeOwner(authentication, #moveId)")
    @Operation(summary = "Set owner authorization for a move request")
    public MoveResponse updateOwnerAuthorization(
            @PathVariable UUID moveId,
            @Valid @RequestBody MoveOwnerAuthorizationRequest request,
            Authentication authentication
    ) {
        return moveService.updateOwnerAuthorization(moveId, request, authentication);
    }

    @PutMapping("/{moveId}/status")
    @PreAuthorize("@moveAccessService.canUpdateStatus(authentication, #moveId)")
    @Operation(summary = "Approve, schedule, complete, reject, or cancel a move request")
    public MoveResponse updateStatus(
            @PathVariable UUID moveId,
            @Valid @RequestBody MoveStatusUpdateRequest request,
            Authentication authentication
    ) {
        return moveService.updateStatus(moveId, request, authentication);
    }
}
