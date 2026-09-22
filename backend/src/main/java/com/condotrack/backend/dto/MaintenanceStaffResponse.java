package com.condotrack.backend.dto;

import java.util.UUID;

public record MaintenanceStaffResponse(UUID staffId, UUID userId, UUID buildingId, String firstName, String lastName, String staffType, String employeeCode) {}
