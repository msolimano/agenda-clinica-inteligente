package com.iclinical.technology.appointments.blocks.dto;

import jakarta.validation.constraints.Size;

public record AgendaBlockCancelRequest(
    @Size(max = 300) String cancellationReason
) {
}
