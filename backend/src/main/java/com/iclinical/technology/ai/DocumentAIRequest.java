package com.iclinical.technology.ai;

import com.iclinical.technology.ai.extraction.DocumentTextExtractionResult;
import com.iclinical.technology.documents.ClinicalDocument;

public record DocumentAIRequest(
    ClinicalDocument document,
    DocumentTextExtractionResult textExtraction
) {
    public String extractedText() {
        return textExtraction == null ? null : textExtraction.text();
    }

    public String textExtractionStatus() {
        return textExtraction == null ? "not_requested" : textExtraction.status();
    }
}
