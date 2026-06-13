package com.iclinical.technology.ai.extraction;

import com.iclinical.technology.documents.ClinicalDocument;
import org.springframework.core.io.FileSystemResource;

interface DocumentTextExtractor {
    boolean supports(String mimeType);

    DocumentTextExtractionResult extract(ClinicalDocument document, FileSystemResource resource);
}
