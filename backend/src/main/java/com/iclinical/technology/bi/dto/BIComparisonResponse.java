package com.iclinical.technology.bi.dto;

import java.util.List;

public record BIComparisonResponse(
    BIComparisonPeriodResponse currentPeriod,
    BIComparisonPeriodResponse previousPeriod,
    List<BIComparisonMetricResponse> metrics
) {
}
