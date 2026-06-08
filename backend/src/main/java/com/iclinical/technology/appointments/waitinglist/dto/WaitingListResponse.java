package com.iclinical.technology.appointments.waitinglist.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record WaitingListResponse(
    UUID id,
    UUID organizationId,
    UUID patientId,
    String patientName,
    UUID specialtyId,
    String specialtyName,
    UUID professionalId,
    String professionalName,
    LocalDate requestedFrom,
    LocalDate requestedTo,
    int priority,
    String availabilityNotes,
    String status,
    UUID scheduledAppointmentId,
    Instant contactedAt,
    Instant createdAt,
    Instant updatedAt
) {
}
