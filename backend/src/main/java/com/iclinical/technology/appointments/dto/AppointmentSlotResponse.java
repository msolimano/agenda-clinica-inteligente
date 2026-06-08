package com.iclinical.technology.appointments.dto;

import java.time.Instant;
import java.util.UUID;

public record AppointmentSlotResponse(
    Instant startAt,
    Instant endAt,
    String status,
    UUID appointmentId,
    String patientName,
    String reason
) {
}
