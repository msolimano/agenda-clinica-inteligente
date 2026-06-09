package com.iclinical.technology.clinicaldiagnoses.dto;

import java.util.UUID;

public record ClinicalDiagnosisUpdateRequest(
    UUID diagnosisCatalogId,
    String diagnosisText,
    Boolean primary,
    String diagnosisStatus,
    String observations
) {
}
