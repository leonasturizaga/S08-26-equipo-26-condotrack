//------------------ new PR32 M21 ------------------------
// //CommonAreaResponse.java
// package com.condotrack.backend.dto;

// import java.util.UUID;

// public record CommonAreaResponse(
//         UUID id,
//         UUID buildingId,
//         String buildingCode,
//         String name,
//         String areaType,
//         Integer capacity,
//         boolean bookingRequired,
//         boolean active,
//         Integer bookingDurationMinutes
// ) {
// }


//----------------- M24.2 ---------------------
package com.condotrack.backend.dto;

import java.util.List;
import java.util.UUID;

public record CommonAreaResponse(
        UUID id,
        UUID buildingId,
        String buildingCode,
        String name,
        String areaType,
        String description,
        Integer capacity,
        boolean bookingRequired,
        boolean active,
        Integer bookingDurationMinutes,
        List<AmenityResponse> amenities
) {
}