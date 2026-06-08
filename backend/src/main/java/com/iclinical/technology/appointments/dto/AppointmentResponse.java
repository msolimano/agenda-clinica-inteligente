package com.iclinical.technology.appointments.dto;

import java.time.Instant;
import java.util.UUID;

public record AppointmentResponse(
    UUID id,
    UUID organizationId,
    UUID professionalId,
    String professionalName,
    UUID patientId,
    String patientName,
    Instant startAt,
    Instant endAt,
    String appointmentType,
    String status,
    String reason,
    String cancellationReason,
    UUID rescheduledFromId,
    Instant confirmedAt,
    Instant cancelledAt,
    Instant noShowAt,
    Instant createdAt,
    Instant updatedAt
) {
}
