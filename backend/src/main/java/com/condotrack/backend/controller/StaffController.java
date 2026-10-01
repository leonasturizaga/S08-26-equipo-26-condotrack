package com.condotrack.backend.controller;

import com.condotrack.backend.dto.StaffAssignmentRequest;
import com.condotrack.backend.dto.StaffAssignmentResponse;
import com.condotrack.backend.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class StaffController {

    private final UserManagementService userManagementService;

    @PutMapping("/{userId}/staff")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'USER_MANAGEMENT_UPDATE')")
    public StaffAssignmentResponse saveStaffAssignment(
            @PathVariable UUID userId,
            @Valid @RequestBody StaffAssignmentRequest request,
            org.springframework.security.core.Authentication authentication
    ) {
        return userManagementService.saveStaffAssignment(
                userId,
                request,
                userManagementService.getAuthenticatedUserId(authentication)
        );
    }
}