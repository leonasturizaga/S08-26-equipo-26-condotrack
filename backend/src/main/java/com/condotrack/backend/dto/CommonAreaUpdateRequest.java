// //CommonAreaUpdateRequest.java
// package com.condotrack.backend.dto;

// import jakarta.validation.constraints.Min;
// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.Size;

// public record CommonAreaUpdateRequest(

//         @NotBlank(message = "name is required")
//         @Size(max = 150, message = "name must not exceed 150 characters")
//         String name,

//         @NotBlank(message = "areaType is required")
//         @Size(max = 50, message = "areaType must not exceed 50 characters")
//         String areaType,

//         @Min(value = 1, message = "capacity must be at least 1")
//         Integer capacity,

//         boolean bookingRequired,

//         @Min(value = 1, message = "bookingDurationMinutes must be at least 1")
//         Integer bookingDurationMinutes
// ) {
// }


//------------------------------ M24.2 ------------------------
package com.condotrack.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CommonAreaUpdateRequest(

        @NotBlank(message = "name is required")
        @Size(max = 150, message = "name must not exceed 150 characters")
        String name,

        @NotBlank(message = "areaType is required")
        @Size(max = 50, message = "areaType must not exceed 50 characters")
        String areaType,

        @Size(max = 5000, message = "description must not exceed 5000 characters")
        String description,

        @Min(value = 1, message = "capacity must be at least 1")
        Integer capacity,

        boolean bookingRequired,

        @Min(value = 1, message = "bookingDurationMinutes must be at least 1")
        Integer bookingDurationMinutes,

        List<UUID> amenityIds
) {
}