// //--------------------------- M24.6 -------------------
// package com.condotrack.backend.media.controller;

// import com.condotrack.backend.media.dto.MediaResponse;
// import com.condotrack.backend.media.dto.MediaReorderRequest;
// import com.condotrack.backend.media.model.MediaEntityType;
// import com.condotrack.backend.media.model.MediaPurpose;
// import com.condotrack.backend.media.service.MediaService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.HttpStatus;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.security.core.Authentication;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.ResponseStatus;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RestController;
// import jakarta.validation.Valid;
// import org.springframework.web.multipart.MultipartFile;
// import org.springframework.web.server.ResponseStatusException;

// import java.util.List;
// import java.util.UUID;

// @RestController
// @RequestMapping("/api/media")
// @RequiredArgsConstructor
// public class MediaController {

//     private final MediaService mediaService;

//     @GetMapping("/common-areas/{commonAreaId}")
//     @PreAuthorize("@bookingAccessService.canViewCollection(authentication)")
//     public List<MediaResponse> getCommonAreaMedia(
//             @PathVariable UUID commonAreaId
//     ) {
//         return mediaService.getCommonAreaMedia(commonAreaId);
//     }

//     @PostMapping(value = "/common-areas/{commonAreaId}/primary", consumes = "multipart/form-data")
//     @ResponseStatus(HttpStatus.CREATED)
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
//     public MediaResponse uploadCommonAreaPrimary(
//             Authentication authentication,
//             @PathVariable UUID commonAreaId,
//             @RequestParam("file") MultipartFile file
//     ) {
//         return mediaService.uploadCommonAreaPrimary(authentication, commonAreaId, file);
//     }

//     @PutMapping(value = "/common-areas/{commonAreaId}/primary", consumes = "multipart/form-data")
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
//     public MediaResponse replaceCommonAreaPrimary(
//             Authentication authentication,
//             @PathVariable UUID commonAreaId,
//             @RequestParam("file") MultipartFile file
//     ) {
//         return mediaService.replaceCommonAreaPrimary(authentication, commonAreaId, file);
//     }

//     @PostMapping(value = "/common-areas/{commonAreaId}/gallery", consumes = "multipart/form-data")
//     @ResponseStatus(HttpStatus.CREATED)
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
//     public MediaResponse uploadCommonAreaGallery(
//             Authentication authentication,
//             @PathVariable UUID commonAreaId,
//             @RequestParam("file") MultipartFile file
//     ) {
//         return mediaService.uploadCommonAreaGallery(authentication, commonAreaId, file);
//     }

//     @PutMapping("/common-areas/{commonAreaId}/gallery/reorder")
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
//     public List<MediaResponse> reorderCommonAreaGallery(
//             Authentication authentication,
//             @PathVariable UUID commonAreaId,
//             @Valid @RequestBody MediaReorderRequest request
//     ) {
//         return mediaService.reorderCommonAreaGallery(authentication, commonAreaId, request);
//     }

//     @DeleteMapping("/common-areas/{commonAreaId}/{mediaId}")
//     @ResponseStatus(HttpStatus.NO_CONTENT)
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
//     public void deactivateCommonAreaMedia(
//             Authentication authentication,
//             @PathVariable UUID commonAreaId,
//             @PathVariable UUID mediaId
//     ) {
//         mediaService.deactivateCommonAreaMedia(authentication, commonAreaId, mediaId);
//     }

//     @PostMapping(consumes = "multipart/form-data")
//     @ResponseStatus(HttpStatus.CREATED)
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
//     public MediaResponse upload(
//             Authentication authentication,
//             @RequestParam MediaEntityType entityType,
//             @RequestParam UUID entityId,
//             @RequestParam MediaPurpose purpose,
//             @RequestParam("file") MultipartFile file
//     ) {
//         if (entityType == MediaEntityType.COMMON_AREA) {
//             throw new ResponseStatusException(
//                     HttpStatus.BAD_REQUEST,
//                     "Use the Common Area media endpoints for COMMON_AREA media"
//             );
//         }

//         return mediaService.upload(authentication, entityType, entityId, purpose, file);
//     }

//     @GetMapping
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_VIEW')")
//     public List<MediaResponse> getAttachments(
//             @RequestParam MediaEntityType entityType,
//             @RequestParam UUID entityId
//     ) {
//         return mediaService.getAttachments(entityType, entityId);
//     }

//     @GetMapping("/{mediaId}")
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_VIEW')")
//     public MediaResponse getMedia(@PathVariable UUID mediaId) {
//         return mediaService.getMedia(mediaId);
//     }

//     @DeleteMapping("/{mediaId}")
//     @ResponseStatus(HttpStatus.NO_CONTENT)
//     @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
//     public void deactivate(
//             Authentication authentication,
//             @PathVariable UUID mediaId
//     ) {
//         mediaService.deactivate(authentication, mediaId);
//     }
// }



package com.condotrack.backend.media.controller;

import com.condotrack.backend.media.dto.MediaResponse;
import com.condotrack.backend.media.dto.MediaReorderRequest;
import com.condotrack.backend.media.model.MediaEntityType;
import com.condotrack.backend.media.model.MediaPurpose;
import com.condotrack.backend.media.service.MediaAccessService;
import com.condotrack.backend.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final MediaAccessService mediaAccessService;

    @GetMapping("/common-areas/{commonAreaId}")
    @PreAuthorize("@bookingAccessService.canViewCollection(authentication)")
    public List<MediaResponse> getCommonAreaMedia(
            @PathVariable UUID commonAreaId
    ) {
        return mediaService.getCommonAreaMedia(commonAreaId);
    }

    @PostMapping(
            value = "/common-areas/{commonAreaId}/primary",
            consumes = "multipart/form-data"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
    public MediaResponse uploadCommonAreaPrimary(
            Authentication authentication,
            @PathVariable UUID commonAreaId,
            @RequestParam("file") MultipartFile file
    ) {
        return mediaService.uploadCommonAreaPrimary(
                authentication,
                commonAreaId,
                file
        );
    }

    @PutMapping(
            value = "/common-areas/{commonAreaId}/primary",
            consumes = "multipart/form-data"
    )
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
    public MediaResponse replaceCommonAreaPrimary(
            Authentication authentication,
            @PathVariable UUID commonAreaId,
            @RequestParam("file") MultipartFile file
    ) {
        return mediaService.replaceCommonAreaPrimary(
                authentication,
                commonAreaId,
                file
        );
    }

    @PostMapping(
            value = "/common-areas/{commonAreaId}/gallery",
            consumes = "multipart/form-data"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
    public MediaResponse uploadCommonAreaGallery(
            Authentication authentication,
            @PathVariable UUID commonAreaId,
            @RequestParam("file") MultipartFile file
    ) {
        return mediaService.uploadCommonAreaGallery(
                authentication,
                commonAreaId,
                file
        );
    }

    @PutMapping("/common-areas/{commonAreaId}/gallery/reorder")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
    public List<MediaResponse> reorderCommonAreaGallery(
            Authentication authentication,
            @PathVariable UUID commonAreaId,
            @RequestBody MediaReorderRequest request
    ) {
        return mediaService.reorderCommonAreaGallery(
                authentication,
                commonAreaId,
                request
        );
    }

    @DeleteMapping("/common-areas/{commonAreaId}/{mediaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
    public void deactivateCommonAreaMedia(
            Authentication authentication,
            @PathVariable UUID commonAreaId,
            @PathVariable UUID mediaId
    ) {
        mediaService.deactivateCommonAreaMedia(
                authentication,
                commonAreaId,
                mediaId
        );
    }

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@mediaAccessService.canUpload(authentication, #entityType, #entityId)")
    public MediaResponse upload(
            Authentication authentication,
            @RequestParam MediaEntityType entityType,
            @RequestParam UUID entityId,
            @RequestParam MediaPurpose purpose,
            @RequestParam("file") MultipartFile file
    ) {
        if (entityType == MediaEntityType.COMMON_AREA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Use the Common Area media endpoints for COMMON_AREA media"
            );
        }

        return mediaService.upload(
                authentication,
                entityType,
                entityId,
                purpose,
                file
        );
    }

    @GetMapping
    @PreAuthorize("@mediaAccessService.canView(authentication, #entityType, #entityId)")
    public List<MediaResponse> getAttachments(
            @RequestParam MediaEntityType entityType,
            @RequestParam UUID entityId
    ) {
        return mediaService.getAttachments(
                entityType,
                entityId
        );
    }

    @GetMapping("/{mediaId}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_VIEW')")
    public MediaResponse getMedia(
            @PathVariable UUID mediaId
    ) {
        return mediaService.getMedia(mediaId);
    }

    @DeleteMapping("/{mediaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissionService.hasPermission(authentication, 'BUILDING_CONFIG_UPDATE')")
    public void deactivate(
            Authentication authentication,
            @PathVariable UUID mediaId
    ) {
        mediaService.deactivate(
                authentication,
                mediaId
        );
    }
}