package com.iclinical.technology.appointments.overbooking.dto;

import java.time.Instant;
import java.util.UUID;

public record OverbookingCapacityResponse(
    UUID professionalId,
    Instant startAt,
    Instant endAt,
    boolean insideAvailability,
    boolean allowsOverbooking,
    int maxOverbookings,
    long activeOverbookings,
    long remainingOverbookings,
    boolean blocked,
    String message
) {
}
