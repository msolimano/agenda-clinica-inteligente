package com.iclinical.technology.ai.extraction;

import com.iclinical.technology.documents.ClinicalDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(20)
class PdfBoxDocumentTextExtractor implements DocumentTextExtractor {

    private static final String MIME_TYPE = "application/pdf";

    @Override
    public boolean supports(String mimeType) {
        return MIME_TYPE.equalsIgnoreCase(mimeType);
    }

    @Override
    public DocumentTextExtractionResult extract(ClinicalDocument document, FileSystemResource resource) {
        try (var pdf = Loader.loadPDF(resource.getFile())) {
            if (pdf.isEncrypted()) {
                return DocumentTextExtractionResult.failed("pdfbox-text", "No fue posible extraer texto clínico estructurado del PDF protegido.");
            }
            var stripper = new PDFTextStripper();
            var text = stripper.getText(pdf);
            return DocumentTextExtractionResult.completed("pdfbox-text", text, confidence(text));
        } catch (IOException exception) {
            return DocumentTextExtractionResult.failed("pdfbox-text", "No fue posible extraer texto clínico estructurado del PDF.");
        }
    }

    private double confidence(String text) {
        if (text == null || text.isBlank()) {
            return 0.10;
        }
        return text.length() >= 120 ? 0.86 : 0.55;
    }
}
