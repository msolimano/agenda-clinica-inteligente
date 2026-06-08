package com.iclinical.technology.ai;

import java.util.List;
import java.util.Map;

public record DocumentAIResult(
    String providerName,
    String providerRequestId,
    String promptVersion,
    Integer inputTokenCount,
    Integer outputTokenCount,
    Integer totalTokenCount,
    Integer latencyMs,
    String providerErrorCode,
    Map<String, Object> providerMetadata,
    String modelName,
    String clinicalSummary,
    List<String> relevantFindings,
    List<String> mentionedDiagnoses,
    List<String> mentionedMedications,
    List<String> mentionedAllergies,
    String recommendations
) {
}
