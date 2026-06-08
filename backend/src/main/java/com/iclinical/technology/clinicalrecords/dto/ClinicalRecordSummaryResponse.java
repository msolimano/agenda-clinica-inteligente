package com.iclinical.technology.clinicalrecords.dto;

import java.time.Instant;
import java.util.UUID;

public record ClinicalRecordSummaryResponse(
    UUID id,
    UUID patientId,
    String patientName,
    UUID professionalId,
    String professionalName,
    UUID appointmentId,
    Instant recordDate,
    String chiefComplaint,
    String assessment,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
}
