package com.condotrack.backend.media.service;

import com.condotrack.backend.media.dto.MediaResponse;
import com.condotrack.backend.media.model.Media;
import com.condotrack.backend.media.model.MediaAttachment;
import com.condotrack.backend.media.model.MediaEntityType;
import com.condotrack.backend.media.model.MediaPurpose;
import com.condotrack.backend.media.model.MediaType;
import com.condotrack.backend.media.model.StorageProvider;
import com.condotrack.backend.model.Building;
import com.condotrack.backend.model.CommonArea;
import com.condotrack.backend.model.MaintenanceRequest;
import com.condotrack.backend.repository.AuditLogRepository;
import com.condotrack.backend.repository.CommonAreaRepository;
import com.condotrack.backend.repository.MaintenanceRequestRepository;
import com.condotrack.backend.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final long MAX_DOCUMENT_BYTES = 20L * 1024 * 1024;
    private static final long MAX_VIDEO_BYTES = 50L * 1024 * 1024;

    private final com.condotrack.backend.media.repository.MediaRepository mediaRepository;
    private final com.condotrack.backend.media.repository.MediaAttachmentRepository mediaAttachmentRepository;
    private final CommonAreaRepository commonAreaRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final CloudinaryMediaStorageService storageService;
    private final AuditService auditService;

    @Transactional
    public MediaResponse upload(
            Authentication authentication,
            MediaEntityType entityType,
            UUID entityId,
            MediaPurpose purpose,
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A media file is required");
        }

        EntityContext entity = resolveEntity(entityType, entityId);
        validatePurpose(entityType, purpose);
        MediaType mediaType = resolveMediaType(purpose, file.getContentType(), file.getOriginalFilename());
        validateFile(mediaType, file);

        String folder = buildFolder(entityType, entityId);
        String resourceType = resourceType(mediaType);

        CloudinaryMediaStorageService.CloudinaryUploadResult uploaded =
                storageService.upload(file, folder, resourceType);

        try {
            Media media = new Media();
            media.setBuilding(entity.building());
            media.setStorageProvider(StorageProvider.CLOUDINARY);
            media.setMediaType(mediaType);
            media.setCloudinaryAssetId(requireCloudinaryValue(uploaded.assetId(), "asset_id"));
            media.setCloudinaryPublicId(requireCloudinaryValue(uploaded.publicId(), "public_id"));
            media.setCloudinaryResourceType(requireCloudinaryValue(uploaded.resourceType(), "resource_type"));
            media.setCloudinaryDeliveryType(requireCloudinaryValue(uploaded.deliveryType(), "type"));
            media.setSecureUrl(requireCloudinaryValue(uploaded.secureUrl(), "secure_url"));
            media.setOriginalFilename(normalizeFilename(file.getOriginalFilename()));
            media.setContentType(normalizeContentType(file.getContentType()));
            media.setFileFormat(uploaded.format());
            media.setFileSize(uploaded.bytes() > 0 ? uploaded.bytes() : file.getSize());
            media.setWidth(uploaded.width());
            media.setHeight(uploaded.height());
            media.setDurationSeconds(uploaded.durationSeconds());
            media.setActive(true);

            Media savedMedia = mediaRepository.save(media);

            MediaAttachment attachment = new MediaAttachment();
            attachment.setMedia(savedMedia);
            attachment.setEntityType(entityType);
            attachment.setEntityId(entityId);
            attachment.setPurpose(purpose);
            attachment.setSortOrder(nextSortOrder(entityType, entityId));
            attachment.setActive(true);
            mediaAttachmentRepository.save(attachment);

            auditService.record(
                    authentication.getName(),
                    entity.building(),
                    "MEDIA",
                    savedMedia.getId(),
                    "UPLOAD",
                    Map.of(
                            "entityType", entityType.name(),
                            "entityId", entityId.toString(),
                            "purpose", purpose.name(),
                            "mediaType", mediaType.name(),
                            "originalFilename", savedMedia.getOriginalFilename()
                    )
            );

            return toResponse(savedMedia, attachment);
        } catch (RuntimeException exception) {
            try {
                storageService.delete(
                        uploaded.publicId(),
                        uploaded.resourceType(),
                        uploaded.deliveryType()
                );
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<MediaResponse> getAttachments(MediaEntityType entityType, UUID entityId) {
        return mediaAttachmentRepository
                .findByEntityTypeAndEntityIdAndActiveTrueOrderBySortOrderAscCreatedAtAsc(entityType, entityId)
                .stream()
                .filter(attachment -> attachment.getMedia().isActive())
                .map(attachment -> toResponse(attachment.getMedia(), attachment))
                .toList();
    }

    @Transactional(readOnly = true)
    public MediaResponse getMedia(UUID mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .filter(Media::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found: " + mediaId));

        MediaAttachment attachment = mediaAttachmentRepository.findByMediaIdAndActiveTrue(mediaId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media attachment not found: " + mediaId));

        return toResponse(media, attachment);
    }

    @Transactional
    public void deactivate(Authentication authentication, UUID mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .filter(Media::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found: " + mediaId));

        List<MediaAttachment> attachments = mediaAttachmentRepository.findByMediaIdAndActiveTrue(mediaId);
        media.setActive(false);
        attachments.forEach(attachment -> attachment.setActive(false));
        mediaAttachmentRepository.saveAll(attachments);
        mediaRepository.save(media);

        try {
            storageService.delete(
                    media.getCloudinaryPublicId(),
                    media.getCloudinaryResourceType(),
                    media.getCloudinaryDeliveryType()
            );
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Media was deactivated in CondoTrack, but Cloudinary deletion failed",
                    exception
            );
        }

        auditService.record(
                authentication.getName(),
                media.getBuilding(),
                "MEDIA",
                media.getId(),
                "DEACTIVATE",
                Map.of(
                        "mediaType", media.getMediaType().name(),
                        "cloudinaryPublicId", media.getCloudinaryPublicId()
                )
        );
    }

    private EntityContext resolveEntity(MediaEntityType entityType, UUID entityId) {
        if (entityType == null || entityId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "entityType and entityId are required");
        }

        return switch (entityType) {
            case COMMON_AREA -> {
                CommonArea area = commonAreaRepository.findById(entityId)
                        .filter(CommonArea::isActive)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active common area not found: " + entityId));
                yield new EntityContext(area.getBuilding());
            }
            case MAINTENANCE -> {
                MaintenanceRequest maintenance = maintenanceRequestRepository.findById(entityId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance request not found: " + entityId));
                yield new EntityContext(maintenance.getBuilding());
            }
        };
    }

    private void validatePurpose(MediaEntityType entityType, MediaPurpose purpose) {
        if (purpose == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "purpose is required");
        }

        boolean valid = switch (entityType) {
            case COMMON_AREA -> purpose == MediaPurpose.PRIMARY_IMAGE || purpose == MediaPurpose.GALLERY_IMAGE;
            case MAINTENANCE -> purpose == MediaPurpose.PROBLEM_IMAGE
                    || purpose == MediaPurpose.SOLUTION_IMAGE
                    || purpose == MediaPurpose.DOCUMENT
                    || purpose == MediaPurpose.VIDEO;
        };

        if (!valid) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Purpose " + purpose + " is not valid for entity type " + entityType
            );
        }
    }

    private MediaType resolveMediaType(MediaPurpose purpose, String contentType, String filename) {
        if (purpose == MediaPurpose.PRIMARY_IMAGE || purpose == MediaPurpose.GALLERY_IMAGE
                || purpose == MediaPurpose.PROBLEM_IMAGE || purpose == MediaPurpose.SOLUTION_IMAGE) {
            return MediaType.IMAGE;
        }
        if (purpose == MediaPurpose.DOCUMENT) {
            return MediaType.DOCUMENT;
        }
        if (purpose == MediaPurpose.VIDEO) {
            return MediaType.VIDEO;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported media purpose");
    }

    private void validateFile(MediaType mediaType, MultipartFile file) {
        String contentType = normalizeContentType(file.getContentType());
        String filename = normalizeFilename(file.getOriginalFilename()).toLowerCase();

        long maxBytes = switch (mediaType) {
            case IMAGE -> MAX_IMAGE_BYTES;
            case DOCUMENT -> MAX_DOCUMENT_BYTES;
            case VIDEO -> MAX_VIDEO_BYTES;
        };

        if (file.getSize() > maxBytes) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "File exceeds the maximum allowed size of " + (maxBytes / (1024 * 1024)) + " MB"
            );
        }

        boolean allowed = switch (mediaType) {
            case IMAGE -> List.of("image/jpeg", "image/png", "image/webp").contains(contentType)
                    && (filename.endsWith(".jpg") || filename.endsWith(".jpeg")
                    || filename.endsWith(".png") || filename.endsWith(".webp"));
            case DOCUMENT -> "application/pdf".equals(contentType) && filename.endsWith(".pdf");
            case VIDEO -> List.of("video/mp4", "video/webm", "video/quicktime").contains(contentType)
                    && (filename.endsWith(".mp4") || filename.endsWith(".webm") || filename.endsWith(".mov"));
        };

        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File type is not allowed for " + mediaType
            );
        }
    }

    private String resourceType(MediaType mediaType) {
        return switch (mediaType) {
            case IMAGE -> "image";
            case DOCUMENT -> "image";
            case VIDEO -> "video";
        };
    }

    private String buildFolder(MediaEntityType entityType, UUID entityId) {
        return switch (entityType) {
            case COMMON_AREA -> "condotrack/condotrack_commonarea/" + entityId;
            case MAINTENANCE -> "condotrack/condotrack_maintenance/" + entityId;
        };
    }

    private int nextSortOrder(MediaEntityType entityType, UUID entityId) {
        return mediaAttachmentRepository
                .findByEntityTypeAndEntityIdAndActiveTrueOrderBySortOrderAscCreatedAtAsc(entityType, entityId)
                .stream()
                .mapToInt(MediaAttachment::getSortOrder)
                .max()
                .orElse(-1) + 1;
    }

    private MediaResponse toResponse(Media media, MediaAttachment attachment) {
        return new MediaResponse(
                media.getId(),
                media.getBuilding() == null ? null : media.getBuilding().getId(),
                media.getMediaType(),
                attachment.getEntityType(),
                attachment.getEntityId(),
                attachment.getPurpose(),
                attachment.getSortOrder(),
                media.getOriginalFilename(),
                media.getContentType(),
                media.getFileFormat(),
                media.getFileSize(),
                media.getWidth(),
                media.getHeight(),
                media.getDurationSeconds(),
                media.getSecureUrl(),
                media.isActive(),
                media.getCreatedAt(),
                media.getUpdatedAt()
        );
    }

    private String requireCloudinaryValue(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Cloudinary response did not contain " + field);
        }
        return value;
    }

    private String normalizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "uploaded-file";
        }
        String normalized = filename.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        normalized = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        return normalized.length() > 255 ? normalized.substring(0, 255) : normalized;
    }

    private String normalizeContentType(String contentType) {
        return contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType.trim().toLowerCase();
    }

    private record EntityContext(Building building) {
    }
}
