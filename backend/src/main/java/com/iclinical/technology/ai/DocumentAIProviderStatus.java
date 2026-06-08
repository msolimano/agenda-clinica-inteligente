package com.iclinical.technology.ai;

public record DocumentAIProviderStatus(
    String activeProvider,
    boolean configured,
    String modelName,
    String promptVersion,
    boolean mockAvailable,
    boolean openAIConfigured,
    String message
) {
}
