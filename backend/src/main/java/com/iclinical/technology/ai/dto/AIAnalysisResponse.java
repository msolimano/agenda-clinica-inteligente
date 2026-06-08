package com.iclinical.technology.ai.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AIAnalysisResponse(
    UUID id,
    UUID organizationId,
    UUID patientId,
    UUID clinicalDocumentId,
    UUID aiConsentId,
    String analysisType,
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
    List<String> relevantFindings,
    List<String> mentionedDiagnoses,
    List<String> mentionedMedications,
    List<String> mentionedAllergies,
    String recommendations,
    String errorMessage,
    Instant startedAt,
    Instant completedAt,
    Instant createdAt,
    Instant updatedAt,
    String disclaimer
) {
}
