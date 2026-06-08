package com.iclinical.technology.appointments.blocks.dto;

import java.util.List;
import java.util.UUID;

public record RescheduleSuggestionResponse(
    UUID appointmentId,
    UUID patientId,
    String patientName,
    List<SuggestedSlotResponse> suggestedSlots
) {
}
