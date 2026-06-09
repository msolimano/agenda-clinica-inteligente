package com.iclinical.technology.clinicalprescriptions.dto;

import java.util.UUID;

public record ClinicalPrescriptionUpdateRequest(
    UUID diagnosisId,
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
