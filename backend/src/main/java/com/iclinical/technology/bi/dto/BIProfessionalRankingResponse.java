package com.iclinical.technology.bi.dto;

import java.util.UUID;

public record BIProfessionalRankingResponse(
    UUID professionalId,
    String professionalName,
    String specialtyName,
    long totalAppointments,
    long cancelledAppointments,
    long noShowAppointments,
    long overbookings,
    long clinicalRecordsTotal
) {
}
