package com.iclinical.technology.appointments.blocks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AgendaBlockCreateRequest(
    @NotNull UUID professionalId,
    @NotNull OffsetDateTime startAt,
    @NotNull OffsetDateTime endAt,
    @NotBlank @Size(max = 300) String reason,
    boolean confirmAffectedAppointments
) {
}
