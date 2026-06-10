package com.iclinical.technology.bi.dto;

import java.math.BigDecimal;

public record BIEfficiencyMetricResponse(
    String key,
    String label,
    long numerator,
    long denominator,
    BigDecimal rate,
    boolean approximate,
    String description
) {
}
