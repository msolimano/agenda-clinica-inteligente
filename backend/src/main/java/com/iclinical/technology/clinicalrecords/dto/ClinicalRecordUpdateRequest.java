package com.iclinical.technology.clinicalrecords.dto;

import java.time.Instant;
import java.util.UUID;

public record ClinicalRecordUpdateRequest(
    UUID patientId,
    UUID professionalId,
    UUID appointmentId,
    Instant recordDate,
    String chiefComplaint,
    String anamnesis,
    String physicalExam,
    String assessment,
    String plan,
    String notes,
    String status
) {
}
