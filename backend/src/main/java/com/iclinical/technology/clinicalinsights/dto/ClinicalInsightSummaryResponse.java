package com.iclinical.technology.clinicalinsights.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ClinicalInsightSummaryResponse(
    UUID id,
    UUID organizationId,
    UUID patientId,
    UUID clinicalDocumentId,
    UUID clinicalRecordId,
    UUID professionalId,
    UUID aiAnalysisId,
    String sourceDocumentName,
    String insightType,
    String title,
    String description,
    String sourceText,
    BigDecimal confidence,
    String status,
    Instant createdAt,
    Instant updatedAt,
    Instant reviewedAt,
    UUID reviewedByProfessionalId,
    String reviewNotes,
    String disclaimer
) {
}
