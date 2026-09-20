package com.condotrack.backend.dto;

import java.util.UUID;

public record IncidentAssignmentRequest(
        UUID assignedToStaffId
) {
}
