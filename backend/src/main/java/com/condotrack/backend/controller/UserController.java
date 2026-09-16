package com.condotrack.backend.controller;

import com.condotrack.backend.dto.UserCreateRequest;
import com.condotrack.backend.dto.UserPageResponse;
import com.condotrack.backend.dto.UserResponse;
import com.condotrack.backend.dto.UserRolesUpdateRequest;
import com.condotrack.backend.dto.UserStatusUpdateRequest;
import com.condotrack.backend.dto.UserUpdateRequest;
import com.condotrack.backend.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserManagementService userManagementService;

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, 'USER_MANAGEMENT_VIEW')")
    public UserPageResponse listUsers(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return userManagementService.getUsers(pageable.getPageNumber(), pageable.getPageSize());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'USER_MANAGEMENT_VIEW')")
    public UserResponse getUser(@PathVariable UUID userId) {
        return userManagementService.getUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionService.hasPermission(authentication, 'USER_MANAGEMENT_CREATE')")
    public UserResponse createUser(
            @Valid @RequestBody UserCreateRequest request,
            Authentication authentication
    ) {
        return userManagementService.createUser(
                request,
                userManagementService.getAuthenticatedUserId(authentication)
        );
    }

    @PutMapping("/{userId}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'USER_MANAGEMENT_UPDATE')")
    public UserResponse updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication
    ) {
        return userManagementService.updateUser(
                userId,
                request,
                userManagementService.getAuthenticatedUserId(authentication)
        );
    }

    @PutMapping("/{userId}/roles")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'USER_MANAGEMENT_UPDATE')")
    public UserResponse updateRoles(
            @PathVariable UUID userId,
            @Valid @RequestBody UserRolesUpdateRequest request,
            Authentication authentication
    ) {
        return userManagementService.updateRoles(
                userId,
                request,
                userManagementService.getAuthenticatedUserId(authentication)
        );
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'USER_MANAGEMENT_UPDATE')")
    public UserResponse updateStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UserStatusUpdateRequest request,
            Authentication authentication
    ) {
        return userManagementService.updateStatus(
                userId,
                request,
                userManagementService.getAuthenticatedUserId(authentication)
        );
    }
}
