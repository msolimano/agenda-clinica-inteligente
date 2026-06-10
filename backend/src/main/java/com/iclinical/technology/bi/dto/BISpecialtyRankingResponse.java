package com.iclinical.technology.bi.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BISpecialtyRankingResponse(
    UUID specialtyId,
    String specialtyName,
    long totalAppointments,
    long completedAppointments,
    long cancelledAppointments,
    long noShowAppointments,
    BigDecimal occupancyRate,
    boolean occupancyRateApproximate
) {
}
