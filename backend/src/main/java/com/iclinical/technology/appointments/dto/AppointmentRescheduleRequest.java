package com.iclinical.technology.appointments.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record AppointmentRescheduleRequest(
    @NotNull OffsetDateTime startAt,
    @NotNull OffsetDateTime endAt,
    @Size(max = 300) String reason
) {
}
