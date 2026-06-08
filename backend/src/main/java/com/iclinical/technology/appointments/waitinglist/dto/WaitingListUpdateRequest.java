package com.iclinical.technology.appointments.waitinglist.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record WaitingListUpdateRequest(
    @NotNull UUID patientId,
    @NotNull UUID specialtyId,
    UUID professionalId,
    LocalDate requestedFrom,
    LocalDate requestedTo,
    @Min(1) @Max(5) Integer priority,
    String availabilityNotes
) {
}
