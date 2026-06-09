package com.iclinical.technology.clinicaldiagnoses.dto;

public record ClinicalDiagnosisUpdateRequest(
    String diagnosisText,
    Boolean primary,
    String diagnosisStatus,
    String observations
) {
}
