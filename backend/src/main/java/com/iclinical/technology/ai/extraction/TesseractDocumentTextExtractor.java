package com.iclinical.technology.ai.extraction;

import com.iclinical.technology.documents.ClinicalDocument;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Order(10)
class TesseractDocumentTextExtractor implements DocumentTextExtractor {

    @Override
    public boolean supports(String mimeType) {
        return "image/jpeg".equalsIgnoreCase(mimeType) || "image/png".equalsIgnoreCase(mimeType);
    }

    @Override
    public DocumentTextExtractionResult extract(ClinicalDocument document, FileSystemResource resource) {
        var language = configuredLanguage();
        try {
            var text = doOcr(resource, language);
            return DocumentTextExtractionResult.completed("tesseract-ocr:" + language, text, confidence(text));
        } catch (TesseractException exception) {
            if (!"eng".equals(language)) {
                return retryEnglish(resource, exception);
            }
            return DocumentTextExtractionResult.failed("tesseract-ocr:" + language, "No fue posible extraer texto clínico estructurado de la imagen.");
        } catch (LinkageError | RuntimeException exception) {
            return DocumentTextExtractionResult.failed("tesseract-ocr:" + language, "Tesseract OCR no está disponible o no pudo procesar la imagen localmente.");
        }
    }

    private DocumentTextExtractionResult retryEnglish(FileSystemResource resource, TesseractException originalException) {
        try {
            var text = doOcr(resource, "eng");
            return DocumentTextExtractionResult.completed("tesseract-ocr:eng", text, confidence(text));
        } catch (TesseractException | LinkageError | RuntimeException retryException) {
            return DocumentTextExtractionResult.failed("tesseract-ocr", "No fue posible extraer texto clínico estructurado de la imagen.");
        }
    }

    private String doOcr(FileSystemResource resource, String language) throws TesseractException {
        var tesseract = new Tesseract();
        var datapath = System.getenv("TESSDATA_PREFIX");
        if (StringUtils.hasText(datapath)) {
            tesseract.setDatapath(datapath.trim());
        }
        tesseract.setLanguage(language);
        return tesseract.doOCR(resource.getFile());
    }

    private String configuredLanguage() {
        var explicit = System.getenv("DOCUMENT_OCR_LANGUAGE");
        if (StringUtils.hasText(explicit)) {
            return explicit.trim();
        }
        var tesseractLanguage = System.getenv("TESSERACT_LANGUAGE");
        if (StringUtils.hasText(tesseractLanguage)) {
            return tesseractLanguage.trim();
        }
        return "spa+eng";
    }

    private double confidence(String text) {
        if (text == null || text.isBlank()) {
            return 0.10;
        }
        return text.length() >= 80 ? 0.72 : 0.45;
    }
}
