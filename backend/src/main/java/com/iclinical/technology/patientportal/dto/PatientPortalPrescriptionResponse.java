package com.iclinical.technology.patientportal.dto;

import java.time.Instant;
import java.util.UUID;

public record PatientPortalPrescriptionResponse(
    UUID id,
    UUID clinicalRecordId,
    UUID diagnosisId,
    String diagnosisText,
    String professionalName,
    String medicationName,
    String dosage,
    String frequency,
    String duration,
    String route,
    String patientInstructions,
    String clinicalNotes,
    String prescriptionStatus,
    boolean historical,
    Instant createdAt,
    Instant updatedAt
) {
}
