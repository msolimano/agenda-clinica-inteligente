package com.iclinical.technology.ai;

import java.util.Map;

class DocumentAIProviderException extends RuntimeException {

    private final String providerName;
    private final String modelName;
    private final String promptVersion;
    private final String errorCode;
    private final Integer latencyMs;
    private final Map<String, Object> metadata;

    DocumentAIProviderException(String message, String providerName, String modelName, String promptVersion, String errorCode, Integer latencyMs, Map<String, Object> metadata) {
        super(message);
        this.providerName = providerName;
        this.modelName = modelName;
        this.promptVersion = promptVersion;
        this.errorCode = errorCode;
        this.latencyMs = latencyMs;
        this.metadata = metadata == null ? Map.of() : metadata;
    }

    String getProviderName() { return providerName; }
    String getModelName() { return modelName; }
    String getPromptVersion() { return promptVersion; }
    String getErrorCode() { return errorCode; }
    Integer getLatencyMs() { return latencyMs; }
    Map<String, Object> getMetadata() { return metadata; }
}
