package com.condotrack.backend.controller;

import com.condotrack.backend.dto.NotificationPageResponse;
import com.condotrack.backend.dto.NotificationResponse;
import com.condotrack.backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "In-app notification inbox and read-state operations")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List notifications for the current user")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasAuthority('ROLE_RESIDENT') or hasAuthority('ROLE_OWNER')")
    public NotificationPageResponse getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        return notificationService.getNotifications(authentication, page, size);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasAuthority('ROLE_RESIDENT') or hasAuthority('ROLE_OWNER')")
    public long unreadCount(Authentication authentication) {
        return notificationService.unreadCount(authentication);
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "Get one notification")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasAuthority('ROLE_RESIDENT') or hasAuthority('ROLE_OWNER')")
    public NotificationResponse getNotification(
            @PathVariable UUID notificationId,
            Authentication authentication
    ) {
        return notificationService.getNotification(notificationId, authentication);
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark one notification as read")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasAuthority('ROLE_RESIDENT') or hasAuthority('ROLE_OWNER')")
    public NotificationResponse markRead(
            @PathVariable UUID notificationId,
            Authentication authentication
    ) {
        return notificationService.markRead(notificationId, authentication);
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all current-user notifications as read")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasAuthority('ROLE_RESIDENT') or hasAuthority('ROLE_OWNER')")
    public int markAllRead(Authentication authentication) {
        return notificationService.markAllRead(authentication);
    }
}
