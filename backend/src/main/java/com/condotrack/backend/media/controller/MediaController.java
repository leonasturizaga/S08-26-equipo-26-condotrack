package com.condotrack.backend.media.controller;

import com.condotrack.backend.media.dto.MediaResponse;
import com.condotrack.backend.media.model.MediaEntityType;
import com.condotrack.backend.media.model.MediaPurpose;
import com.condotrack.backend.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
    public MediaResponse upload(
            Authentication authentication,
            @RequestParam MediaEntityType entityType,
            @RequestParam UUID entityId,
            @RequestParam MediaPurpose purpose,
            @RequestParam("file") MultipartFile file
    ) {
        return mediaService.upload(authentication, entityType, entityId, purpose, file);
    }

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_VIEW')")
    public List<MediaResponse> getAttachments(
            @RequestParam MediaEntityType entityType,
            @RequestParam UUID entityId
    ) {
        return mediaService.getAttachments(entityType, entityId);
    }

    @GetMapping("/{mediaId}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_VIEW')")
    public MediaResponse getMedia(@PathVariable UUID mediaId) {
        return mediaService.getMedia(mediaId);
    }

    @DeleteMapping("/{mediaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
    public void deactivate(
            Authentication authentication,
            @PathVariable UUID mediaId
    ) {
        mediaService.deactivate(authentication, mediaId);
    }
}
