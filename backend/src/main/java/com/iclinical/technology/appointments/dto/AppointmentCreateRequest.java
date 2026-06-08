package com.iclinical.technology.appointments.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentCreateRequest(
    @NotNull UUID professionalId,
    @NotNull UUID patientId,
    @NotNull OffsetDateTime startAt,
    @NotNull OffsetDateTime endAt,
    @Size(max = 40) String appointmentType,
    @Size(max = 300) String reason
) {
}
