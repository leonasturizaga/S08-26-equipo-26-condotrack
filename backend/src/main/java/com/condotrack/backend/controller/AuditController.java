package com.condotrack.backend.controller;

import com.condotrack.backend.dto.AuditLogPageResponse;
import com.condotrack.backend.service.AuditQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Administrator audit and traceability records.")
public class AuditController {
    private final AuditQueryService auditQueryService;

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, 'AUDIT_VIEW')")
    @Operation(summary = "Search audit records")
    public AuditLogPageResponse getAudit(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) UUID buildingId,
            @RequestParam(required = false) String actorEmail,
            @Parameter(description = "Inclusive ISO-8601 date/time")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @Parameter(description = "Inclusive ISO-8601 date/time")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return auditQueryService.search(page, size, entityType, action, buildingId, actorEmail, from, to);
    }
}
