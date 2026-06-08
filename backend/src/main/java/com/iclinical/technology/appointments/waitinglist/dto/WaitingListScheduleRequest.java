package com.iclinical.technology.appointments.waitinglist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WaitingListScheduleRequest(
    UUID professionalId,
    @NotNull OffsetDateTime startAt,
    @NotNull OffsetDateTime endAt,
    @Size(max = 40) String appointmentType,
    @Size(max = 300) String reason
) {
}
