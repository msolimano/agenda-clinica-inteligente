package com.iclinical.technology.appointments.overbooking.dto;

import java.time.Instant;
import java.util.UUID;

public record OverbookingResponse(
    UUID id,
    UUID organizationId,
    UUID appointmentId,
    UUID professionalId,
    String professionalName,
    UUID patientId,
    String patientName,
    Instant startAt,
    Instant endAt,
    String appointmentType,
    String appointmentStatus,
    String reason,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
}
