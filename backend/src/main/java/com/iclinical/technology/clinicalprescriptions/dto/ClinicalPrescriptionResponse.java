package com.iclinical.technology.clinicalprescriptions.dto;

import java.time.Instant;
import java.util.UUID;

public record ClinicalPrescriptionResponse(
    UUID id,
    UUID organizationId,
    UUID clinicalRecordId,
    UUID patientId,
    String patientName,
    UUID professionalId,
    String professionalName,
    UUID diagnosisId,
    String diagnosisText,
    UUID medicationCatalogId,
    String medicationCode,
    String medicationCodeSystem,
    String medicationCodeDisplay,
    String medicationName,
    String dosage,
    String frequency,
    String duration,
    String route,
    String patientInstructions,
    String clinicalNotes,
    String prescriptionStatus,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
}
