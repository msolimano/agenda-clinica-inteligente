package com.iclinical.technology.bi.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BISpecialtyOccupancyResponse(
    UUID specialtyId,
    String specialtyName,
    long occupiedAppointments,
    long operativeAppointments,
    BigDecimal occupancyRate,
    boolean occupancyRateApproximate
) {
}
