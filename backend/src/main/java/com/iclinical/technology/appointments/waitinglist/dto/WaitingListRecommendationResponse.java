package com.iclinical.technology.appointments.waitinglist.dto;

import java.time.LocalDate;
import java.util.UUID;

public record WaitingListRecommendationResponse(
    UUID waitingListId,
    UUID patientId,
    String patientName,
    UUID specialtyId,
    String specialtyName,
    UUID professionalId,
    String professionalName,
    LocalDate requestedFrom,
    LocalDate requestedTo,
    int priority,
    String status,
    int score,
    String reason
) {
}
