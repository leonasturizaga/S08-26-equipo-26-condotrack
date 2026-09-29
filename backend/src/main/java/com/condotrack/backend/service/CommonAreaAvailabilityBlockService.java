package com.condotrack.backend.service;

import com.condotrack.backend.dto.CommonAreaAvailabilityBlockRequest;
import com.condotrack.backend.dto.CommonAreaAvailabilityBlockResponse;
import com.condotrack.backend.model.CommonArea;
import com.condotrack.backend.model.CommonAreaAvailabilityBlock;
import com.condotrack.backend.model.CommonAreaBlockType;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.BookingRepository;
import com.condotrack.backend.repository.CommonAreaAvailabilityBlockRepository;
import com.condotrack.backend.repository.CommonAreaRepository;
import com.condotrack.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommonAreaAvailabilityBlockService {

    private final CommonAreaAvailabilityBlockRepository blockRepository;
    private final CommonAreaRepository commonAreaRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<CommonAreaAvailabilityBlockResponse> getBlocks(
            UUID commonAreaId,
            OffsetDateTime from,
            OffsetDateTime to
    ) {
        getActiveCommonArea(commonAreaId);

        if (from != null && to != null && !to.isAfter(from)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "to must be after from"
            );
        }

        List<CommonAreaAvailabilityBlock> blocks;

        if (from == null && to == null) {
            blocks = blockRepository
                    .findByCommonAreaIdAndActiveTrueOrderByStartAtAsc(commonAreaId);
        } else {
            OffsetDateTime effectiveFrom =
                    from != null ? from : OffsetDateTime.MIN;

            OffsetDateTime effectiveTo =
                    to != null ? to : OffsetDateTime.MAX;

            blocks = blockRepository
                    .findByCommonAreaIdAndActiveTrueAndEndAtGreaterThanAndStartAtLessThanOrderByStartAtAsc(
                            commonAreaId,
                            effectiveFrom,
                            effectiveTo
                    );
        }

        return blocks.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CommonAreaAvailabilityBlockResponse createBlock(
            UUID commonAreaId,
            CommonAreaAvailabilityBlockRequest request,
            Authentication authentication
    ) {
        CommonArea commonArea = getActiveCommonArea(commonAreaId);

        validateDates(request.startAt(), request.endAt());

        ensureNoBlockOverlap(
                commonAreaId,
                request.startAt(),
                request.endAt()
        );

        ensureNoBookingOverlap(
                commonAreaId,
                request.startAt(),
                request.endAt()
        );

        CommonAreaAvailabilityBlock block =
                new CommonAreaAvailabilityBlock();

        block.setCommonArea(commonArea);
        block.setBlockType(request.blockType());
        block.setStartAt(request.startAt());
        block.setEndAt(request.endAt());
        block.setNotes(normalizeOptional(request.notes()));
        block.setActive(true);

        User actor = getAuthenticatedUser(authentication);
        block.setUpdatedBy(actor.getId());

        CommonAreaAvailabilityBlock saved =
                blockRepository.save(block);

        auditService.record(
                authentication.getName(),
                commonArea.getBuilding(),
                "COMMON_AREA_AVAILABILITY_BLOCK",
                saved.getId(),
                "CREATE",
                null,
                saved.getBlockType().name(),
                null,
                Map.of(
                        "commonAreaId",
                        commonArea.getId().toString(),
                        "startAt",
                        saved.getStartAt().toString(),
                        "endAt",
                        saved.getEndAt().toString()
                )
        );

        return toResponse(saved);
    }

    @Transactional
    public CommonAreaAvailabilityBlockResponse updateBlock(
            UUID blockId,
            CommonAreaAvailabilityBlockRequest request,
            Authentication authentication
    ) {
        CommonAreaAvailabilityBlock block = getBlock(blockId);

        if (!block.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Availability block is already inactive"
            );
        }

        validateDates(request.startAt(), request.endAt());

        UUID commonAreaId = block.getCommonArea().getId();

        ensureNoBlockOverlapExcludingSelf(
                commonAreaId,
                blockId,
                request.startAt(),
                request.endAt()
        );

        ensureNoBookingOverlap(
                commonAreaId,
                request.startAt(),
                request.endAt()
        );

        User actor = getAuthenticatedUser(authentication);

        CommonAreaBlockType previousType = block.getBlockType();

        block.setBlockType(request.blockType());
        block.setStartAt(request.startAt());
        block.setEndAt(request.endAt());
        block.setNotes(normalizeOptional(request.notes()));
        block.setUpdatedBy(actor.getId());

        CommonAreaAvailabilityBlock saved =
                blockRepository.save(block);

        auditService.record(
                authentication.getName(),
                saved.getCommonArea().getBuilding(),
                "COMMON_AREA_AVAILABILITY_BLOCK",
                saved.getId(),
                "UPDATE",
                previousType.name(),
                saved.getBlockType().name(),
                null,
                Map.of(
                        "commonAreaId",
                        saved.getCommonArea().getId().toString(),
                        "startAt",
                        saved.getStartAt().toString(),
                        "endAt",
                        saved.getEndAt().toString()
                )
        );

        return toResponse(saved);
    }

    @Transactional
    public void deactivateBlock(
            UUID blockId,
            Authentication authentication
    ) {
        CommonAreaAvailabilityBlock block = getBlock(blockId);

        if (!block.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Availability block is already inactive"
            );
        }

        User actor = getAuthenticatedUser(authentication);

        block.setActive(false);
        block.setUpdatedBy(actor.getId());

        blockRepository.save(block);

        auditService.record(
                authentication.getName(),
                block.getCommonArea().getBuilding(),
                "COMMON_AREA_AVAILABILITY_BLOCK",
                block.getId(),
                "DEACTIVATE",
                block.getBlockType().name(),
                "INACTIVE",
                null,
                Map.of(
                        "commonAreaId",
                        block.getCommonArea().getId().toString()
                )
        );
    }

    private void ensureNoBlockOverlap(
            UUID commonAreaId,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {
        boolean overlap =
                blockRepository
                        .existsByCommonAreaIdAndActiveTrueAndStartAtLessThanAndEndAtGreaterThan(
                                commonAreaId,
                                endAt,
                                startAt
                        );

        if (overlap) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The requested period overlaps an existing availability block"
            );
        }
    }

    private void ensureNoBlockOverlapExcludingSelf(
            UUID commonAreaId,
            UUID blockId,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {
        boolean overlap =
                blockRepository
                        .existsByCommonAreaIdAndActiveTrueAndIdNotAndStartAtLessThanAndEndAtGreaterThan(
                                commonAreaId,
                                blockId,
                                endAt,
                                startAt
                        );

        if (overlap) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The requested period overlaps an existing availability block"
            );
        }
    }

    private void ensureNoBookingOverlap(
            UUID commonAreaId,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {
        boolean overlap = bookingRepository
                .existsByCommonAreaIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        commonAreaId,
                        List.of(
                                com.condotrack.backend.model.Enums.BookingStatus.PENDING,
                                com.condotrack.backend.model.Enums.BookingStatus.APPROVED
                        ),
                        endAt,
                        startAt
                );

        if (overlap) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The requested period overlaps an existing pending or approved booking"
            );
        }
    }

    private CommonArea getActiveCommonArea(UUID commonAreaId) {
        return commonAreaRepository.findById(commonAreaId)
                .filter(CommonArea::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Common area not found: " + commonAreaId
                ));
    }

    private CommonAreaAvailabilityBlock getBlock(UUID blockId) {
        return blockRepository.findById(blockId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Availability block not found: " + blockId
                ));
    }

    private void validateDates(
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {
        if (startAt == null || endAt == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "startAt and endAt are required"
            );
        }

        if (!endAt.isAfter(startAt)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "endAt must be after startAt"
            );
        }
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException(
                    "Authenticated user is required"
            );
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found"
                ));
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty() ? null : normalized;
    }

    private CommonAreaAvailabilityBlockResponse toResponse(
            CommonAreaAvailabilityBlock block
    ) {
        CommonArea area = block.getCommonArea();

        return new CommonAreaAvailabilityBlockResponse(
                block.getId(),
                area.getId(),
                area.getName(),
                area.getBuilding().getId(),
                area.getBuilding().getCode(),
                block.getBlockType(),
                block.getStartAt(),
                block.getEndAt(),
                block.getNotes(),
                block.isActive()
        );
    }
}