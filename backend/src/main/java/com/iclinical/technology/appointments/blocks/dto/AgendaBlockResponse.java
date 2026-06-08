package com.iclinical.technology.appointments.blocks.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AgendaBlockResponse(
    UUID id,
    UUID organizationId,
    UUID professionalId,
    String professionalName,
    Instant startAt,
    Instant endAt,
    String appointmentType,
    String status,
    String reason,
    String cancellationReason,
    Instant cancelledAt,
    Instant createdAt,
    Instant updatedAt,
    List<AffectedAppointmentResponse> affectedAppointments,
    List<RescheduleSuggestionResponse> rescheduleSuggestions
) {
}
