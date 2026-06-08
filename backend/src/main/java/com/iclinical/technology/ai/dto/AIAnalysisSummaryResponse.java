package com.iclinical.technology.ai.dto;

import java.time.Instant;
import java.util.UUID;

public record AIAnalysisSummaryResponse(
    UUID id,
    UUID patientId,
    UUID clinicalDocumentId,
    String status,
    String modelName,
    String clinicalSummary,
    String errorMessage,
    Instant createdAt,
    Instant completedAt
) {
}
