package com.iclinical.technology.bi.dto;

import java.time.Instant;
import java.util.List;

public record BIEfficiencyResponse(
    Instant from,
    Instant to,
    List<BIEfficiencyMetricResponse> metrics
) {
}
