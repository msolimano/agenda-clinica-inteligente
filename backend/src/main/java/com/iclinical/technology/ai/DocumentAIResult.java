package com.iclinical.technology.ai;

import java.util.List;

public record DocumentAIResult(
    String modelName,
    String clinicalSummary,
    List<String> relevantFindings,
    List<String> mentionedDiagnoses,
    List<String> mentionedMedications,
    List<String> mentionedAllergies,
    String recommendations
) {
}
