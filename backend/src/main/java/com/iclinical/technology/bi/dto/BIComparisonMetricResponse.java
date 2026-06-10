package com.iclinical.technology.bi.dto;

import java.math.BigDecimal;

public record BIComparisonMetricResponse(
    String key,
    String label,
    long currentValue,
    long previousValue,
    long absoluteDifference,
    BigDecimal percentageVariation,
    String variationLabel,
    String variationType
) {
}
