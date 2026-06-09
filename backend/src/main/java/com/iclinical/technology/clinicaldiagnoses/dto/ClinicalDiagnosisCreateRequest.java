package com.iclinical.technology.clinicaldiagnoses.dto;

public record ClinicalDiagnosisCreateRequest(
    String diagnosisText,
    Boolean primary,
    String diagnosisStatus,
    String observations
) {
}
