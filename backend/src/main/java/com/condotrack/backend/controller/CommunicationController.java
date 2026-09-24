package com.condotrack.backend.controller;

import com.condotrack.backend.dto.CommunicationSendRequest;
import com.condotrack.backend.dto.CommunicationSendResponse;
import com.condotrack.backend.dto.CommunicationPageResponse;
import com.condotrack.backend.dto.CommunicationResponse;
import com.condotrack.backend.service.CommunicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@RestController
@RequestMapping("/api/communications")
@Tag(name = "Communications", description = "Administrator in-app communication sending")
@RequiredArgsConstructor
public class CommunicationController {

    private final CommunicationService communicationService;


    @GetMapping
    @Operation(summary = "List sent communications")
    @PreAuthorize("isAuthenticated()")
    public CommunicationPageResponse getCommunications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        return communicationService.getCommunications(authentication, page, size);
    }

    @GetMapping("/{communicationId}")
    @Operation(summary = "Get one sent communication and recipient read statistics")
    @PreAuthorize("isAuthenticated()")
    public CommunicationResponse getCommunication(
            @PathVariable UUID communicationId,
            Authentication authentication
    ) {
        return communicationService.getCommunication(communicationId, authentication);
    }
    @PostMapping
    @Operation(summary = "Send an in-app announcement")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionService.hasPermission(authentication, 'COMMUNICATIONS_CREATE')")
    public CommunicationSendResponse sendCommunication(
            @Valid @RequestBody CommunicationSendRequest request,
            Authentication authentication
    ) {
        return communicationService.send(request, authentication);
    }
}
