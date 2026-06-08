package com.iclinical.technology.ai.dto;

public record DocumentAIProviderStatusResponse(
    String activeProvider,
    boolean configured,
    String modelName,
    String promptVersion,
    boolean mockAvailable,
    boolean openAIConfigured,
    String message
) {
}
