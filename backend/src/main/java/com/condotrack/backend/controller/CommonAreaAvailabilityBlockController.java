package com.condotrack.backend.controller;

import com.condotrack.backend.dto.CommonAreaAvailabilityBlockRequest;
import com.condotrack.backend.dto.CommonAreaAvailabilityBlockResponse;
import com.condotrack.backend.service.CommonAreaAvailabilityBlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/common-areas")
@RequiredArgsConstructor
public class CommonAreaAvailabilityBlockController {

    private final CommonAreaAvailabilityBlockService blockService;

    @GetMapping("/{commonAreaId}/availability-blocks")
    @PreAuthorize("@bookingAccessService.canViewCollection(authentication)")
    public List<CommonAreaAvailabilityBlockResponse> getBlocks(
            @PathVariable UUID commonAreaId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to
    ) {
        return blockService.getBlocks(commonAreaId, from, to);
    }

    @PostMapping("/{commonAreaId}/availability-blocks")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
    public CommonAreaAvailabilityBlockResponse createBlock(
            @PathVariable UUID commonAreaId,
            @Valid @RequestBody CommonAreaAvailabilityBlockRequest request,
            Authentication authentication
    ) {
        return blockService.createBlock(
                commonAreaId,
                request,
                authentication
        );
    }

    @PutMapping("/availability-blocks/{blockId}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
    public CommonAreaAvailabilityBlockResponse updateBlock(
            @PathVariable UUID blockId,
            @Valid @RequestBody CommonAreaAvailabilityBlockRequest request,
            Authentication authentication
    ) {
        return blockService.updateBlock(
                blockId,
                request,
                authentication
        );
    }

    @DeleteMapping("/availability-blocks/{blockId}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
    public ResponseEntity<Void> deactivateBlock(
            @PathVariable UUID blockId,
            Authentication authentication
    ) {
        blockService.deactivateBlock(blockId, authentication);
        return ResponseEntity.noContent().build();
    }
}