package com.iclinical.technology.clinicalrecords.dto;

import java.time.Instant;
import java.util.UUID;

public record ClinicalRecordResponse(
    UUID id,
    UUID organizationId,
    UUID patientId,
    String patientName,
    UUID professionalId,
    String professionalName,
    UUID appointmentId,
    Instant recordDate,
    String chiefComplaint,
    String anamnesis,
    String physicalExam,
    String assessment,
    String plan,
    String notes,
    String status,
    Instant finalizedAt,
    Instant createdAt,
    Instant updatedAt
) {
}
