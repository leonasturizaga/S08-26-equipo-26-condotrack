package com.condotrack.backend.media.repository;

import com.condotrack.backend.media.model.MediaAttachment;
import com.condotrack.backend.media.model.MediaEntityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MediaAttachmentRepository extends JpaRepository<MediaAttachment, UUID> {

    List<MediaAttachment> findByEntityTypeAndEntityIdAndActiveTrueOrderBySortOrderAscCreatedAtAsc(
            MediaEntityType entityType,
            UUID entityId
    );

    List<MediaAttachment> findByMediaIdAndActiveTrue(UUID mediaId);
}
