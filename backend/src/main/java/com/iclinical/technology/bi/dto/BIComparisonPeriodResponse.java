package com.iclinical.technology.bi.dto;

import java.time.Instant;

public record BIComparisonPeriodResponse(
    Instant from,
    Instant to
) {
}
