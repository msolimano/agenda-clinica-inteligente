package com.iclinical.technology.clinicaldiagnoses.dto;

import java.time.Instant;
import java.util.UUID;

public record ClinicalDiagnosisResponse(
    UUID id,
    UUID organizationId,
    UUID clinicalRecordId,
    UUID patientId,
    String patientName,
    UUID professionalId,
    String professionalName,
    String diagnosisText,
    boolean primary,
    String diagnosisStatus,
    String observations,
    String codeSystem,
    String diagnosisCode,
    String diagnosisCodeDisplay,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
}
