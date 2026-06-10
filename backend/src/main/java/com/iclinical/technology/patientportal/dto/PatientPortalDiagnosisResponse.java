package com.iclinical.technology.patientportal.dto;

import java.time.Instant;
import java.util.UUID;

public record PatientPortalDiagnosisResponse(
    UUID id,
    UUID clinicalRecordId,
    String professionalName,
    String diagnosisText,
    boolean primary,
    String diagnosisStatus,
    String displayStatus,
    String observations,
    String codeSystem,
    String diagnosisCode,
    String diagnosisCodeDisplay,
    Instant createdAt,
    Instant updatedAt
) {
}
