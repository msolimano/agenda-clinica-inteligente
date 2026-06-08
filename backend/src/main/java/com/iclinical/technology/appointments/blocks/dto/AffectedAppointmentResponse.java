package com.iclinical.technology.appointments.blocks.dto;

import java.time.Instant;
import java.util.UUID;

public record AffectedAppointmentResponse(
    UUID appointmentId,
    UUID patientId,
    String patientName,
    Instant startAt,
    Instant endAt,
    String appointmentType,
    String status,
    String reason
) {
}
