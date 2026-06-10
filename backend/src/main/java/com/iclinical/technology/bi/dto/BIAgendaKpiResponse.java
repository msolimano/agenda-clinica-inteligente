package com.iclinical.technology.bi.dto;

import java.math.BigDecimal;

public record BIAgendaKpiResponse(
    long totalAppointments,
    long scheduledAppointments,
    long confirmedAppointments,
    long cancelledAppointments,
    long noShowAppointments,
    long blockedSlots,
    long overbookings,
    BigDecimal occupancyRate,
    boolean occupancyRateApproximate
) {
}
