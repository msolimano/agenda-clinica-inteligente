package com.iclinical.technology.ai.extraction;

import com.iclinical.technology.documents.ClinicalDocument;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class NoopDocumentTextExtractor implements DocumentTextExtractor {

    @Override
    public boolean supports(String mimeType) {
        return true;
    }

    @Override
    public DocumentTextExtractionResult extract(ClinicalDocument document, FileSystemResource resource) {
        return DocumentTextExtractionResult.unsupported(document.getMimeType());
    }
}
