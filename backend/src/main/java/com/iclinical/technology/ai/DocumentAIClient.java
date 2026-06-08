package com.iclinical.technology.ai;

import com.iclinical.technology.documents.ClinicalDocument;

public interface DocumentAIClient {
    DocumentAIResult analyze(ClinicalDocument document);
    DocumentAIProviderStatus status();
}
