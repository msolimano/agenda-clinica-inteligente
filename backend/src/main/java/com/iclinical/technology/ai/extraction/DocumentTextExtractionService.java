package com.iclinical.technology.ai.extraction;

import com.iclinical.technology.documents.ClinicalDocument;
import com.iclinical.technology.documents.LocalClinicalDocumentStorageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentTextExtractionService {

    private final LocalClinicalDocumentStorageService storageService;
    private final List<DocumentTextExtractor> extractors;

    public DocumentTextExtractionService(LocalClinicalDocumentStorageService storageService, List<DocumentTextExtractor> extractors) {
        this.storageService = storageService;
        this.extractors = extractors;
    }

    public DocumentTextExtractionResult extract(ClinicalDocument document) {
        if (document == null) {
            return DocumentTextExtractionResult.failed("local-storage", "Debe indicar el documento clínico para extraer texto.");
        }

        var extractor = extractors.stream()
            .filter(candidate -> candidate.supports(document.getMimeType()))
            .findFirst();

        if (extractor.isEmpty()) {
            return DocumentTextExtractionResult.unsupported(document.getMimeType());
        }

        try {
            var resource = storageService.load(document.getObjectKey());
            return extractor.get().extract(document, resource);
        } catch (RuntimeException exception) {
            return DocumentTextExtractionResult.failed("local-storage", "No fue posible leer el archivo clínico para extracción de texto.");
        }
    }
}
