package com.iclinical.technology.appointments.blocks.dto;

import java.time.Instant;

public record SuggestedSlotResponse(
    Instant startAt,
    Instant endAt
) {
}
