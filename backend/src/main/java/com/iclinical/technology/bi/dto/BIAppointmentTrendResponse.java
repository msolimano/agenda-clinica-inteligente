package com.iclinical.technology.bi.dto;

import java.time.Instant;
import java.util.List;

public record BIAppointmentTrendResponse(
    Instant from,
    Instant to,
    List<BITrendPointResponse> points
) {
}
