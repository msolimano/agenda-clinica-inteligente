package com.iclinical.technology.appointments.overbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OverbookingCreateRequest(
    @NotNull UUID professionalId,
    @NotNull UUID patientId,
    @NotNull OffsetDateTime startAt,
    @NotNull OffsetDateTime endAt,
    @Size(max = 40) String appointmentType,
    @NotBlank @Size(max = 300) String reason
) {
}
