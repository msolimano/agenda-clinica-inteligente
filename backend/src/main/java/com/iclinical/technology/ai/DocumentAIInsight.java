package com.iclinical.technology.ai;

public record DocumentAIInsight(
    String type,
    String title,
    String description,
    String sourceText,
    Double confidence
) {
}
