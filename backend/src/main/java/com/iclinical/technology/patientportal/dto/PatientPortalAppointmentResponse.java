package com.iclinical.technology.patientportal.dto;

import java.time.Instant;
import java.util.UUID;

public record PatientPortalAppointmentResponse(
    UUID id,
    UUID professionalId,
    String professionalName,
    Instant startAt,
    Instant endAt,
    String appointmentType,
    String status,
    String reason,
    boolean upcoming
) {
}
