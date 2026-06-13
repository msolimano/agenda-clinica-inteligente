package com.iclinical.technology.ai.extraction;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record DocumentTextExtractionResult(
    String status,
    String method,
    String text,
    String preview,
    BigDecimal confidence,
    String errorMessage
) {
    private static final int PREVIEW_LIMIT = 1200;

    public static DocumentTextExtractionResult completed(String method, String text, double confidence) {
        var normalized = normalize(text);
        if (normalized.isBlank()) {
            return empty(method);
        }
        return new DocumentTextExtractionResult(
            "completed",
            method,
            normalized,
            preview(normalized),
            confidence(confidence),
            null
        );
    }

    public static DocumentTextExtractionResult empty(String method) {
        return new DocumentTextExtractionResult(
            "empty",
            method,
            null,
            null,
            confidence(0.1000),
            "No fue posible extraer texto clínico estructurado del documento."
        );
    }

    public static DocumentTextExtractionResult unsupported(String mimeType) {
        return new DocumentTextExtractionResult(
            "unsupported",
            "noop",
            null,
            null,
            confidence(0.0000),
            "Tipo de documento no soportado para extracción de texto: " + nullSafe(mimeType)
        );
    }

    public static DocumentTextExtractionResult failed(String method, String message) {
        return new DocumentTextExtractionResult(
            "failed",
            method,
            null,
            null,
            confidence(0.0000),
            message == null || message.isBlank() ? "No fue posible extraer texto clínico estructurado del documento." : message
        );
    }

    public boolean hasText() {
        return text != null && !text.isBlank();
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\u0000', ' ').replaceAll("\\s+", " ").trim();
    }

    private static String preview(String value) {
        return value.length() <= PREVIEW_LIMIT ? value : value.substring(0, PREVIEW_LIMIT);
    }

    private static BigDecimal confidence(double value) {
        return BigDecimal.valueOf(Math.max(0, Math.min(1, value))).setScale(4, RoundingMode.HALF_UP);
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
