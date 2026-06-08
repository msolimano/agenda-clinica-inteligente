package com.iclinical.technology.ai.dto;

import java.time.Instant;
import java.util.UUID;

public record AIAnalysisSummaryResponse(
    UUID id,
    UUID patientId,
    UUID clinicalDocumentId,
    UUID aiConsentId,
    String status,
    String modelName,
    String providerName,
    String providerRequestId,
    String promptVersion,
    Integer inputTokenCount,
    Integer outputTokenCount,
    Integer totalTokenCount,
    Integer latencyMs,
    String providerErrorCode,
    String clinicalSummary,
    String errorMessage,
    Instant createdAt,
    Instant completedAt
) {
}
