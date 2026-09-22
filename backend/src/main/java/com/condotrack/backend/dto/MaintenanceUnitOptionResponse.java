package com.condotrack.backend.dto;

import java.util.UUID;

public record MaintenanceUnitOptionResponse(UUID unitId, String unitNumber, UUID buildingId, String buildingCode) {}
