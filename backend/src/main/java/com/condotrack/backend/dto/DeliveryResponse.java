package com.condotrack.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeliveryResponse(
        UUID id,
        UUID buildingId,
        UUID unitId,
        String unitNumber,
        UUID residentId,
        String residentName,
        String carrierName,
        String trackingNumber,
        String deliveryType,
        String status,
        OffsetDateTime receivedAt,
        OffsetDateTime notifiedAt,
        OffsetDateTime collectedAt,
        UUID collectedByUserId,
        String notes
) {
}
