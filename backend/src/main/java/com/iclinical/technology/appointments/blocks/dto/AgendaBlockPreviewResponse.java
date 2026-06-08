package com.iclinical.technology.appointments.blocks.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AgendaBlockPreviewResponse(
    UUID professionalId,
    Instant startAt,
    Instant endAt,
    String reason,
    boolean requiresConfirmation,
    List<AffectedAppointmentResponse> affectedAppointments,
    List<RescheduleSuggestionResponse> rescheduleSuggestions
) {
}
