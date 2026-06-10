package com.iclinical.technology.appointments.availability.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ProfessionalAvailabilityResponse(
    UUID id,
    UUID organizationId,
    UUID professionalId,
    short weekday,
    LocalTime startTime,
    LocalTime endTime,
    int slotMinutes,
    String location,
    LocalDate validFrom,
    LocalDate validTo,
    String status,
    boolean allowsOverbooking,
    int maxOverbookings,
    Instant createdAt,
    Instant updatedAt
) {
}
