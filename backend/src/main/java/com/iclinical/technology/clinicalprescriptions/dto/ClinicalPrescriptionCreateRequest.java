package com.iclinical.technology.clinicalprescriptions.dto;

import java.util.UUID;

public record ClinicalPrescriptionCreateRequest(
    UUID diagnosisId,
    UUID medicationCatalogId,
    String medicationName,
    String dosage,
    String frequency,
    String duration,
    String route,
    String patientInstructions,
    String clinicalNotes,
    String prescriptionStatus
) {
}
