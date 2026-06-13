package com.iclinical.technology.ai;

public interface DocumentAIClient {
    DocumentAIResult analyze(DocumentAIRequest request);
    DocumentAIProviderStatus status();
}
