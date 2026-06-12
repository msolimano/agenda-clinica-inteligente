package com.iclinical.technology.ai;

import com.iclinical.technology.documents.ClinicalDocument;

import java.util.List;
import java.util.Map;

class MockDocumentAIClient implements DocumentAIClient {

    private static final String PROVIDER = "mock";
    private static final String MODEL = "mock-clinical-document-analyzer-v1";
    private final DocumentAIProperties properties;

    MockDocumentAIClient(DocumentAIProperties properties) {
        this.properties = properties;
    }

    @Override
    public DocumentAIResult analyze(ClinicalDocument document) {
        var started = System.nanoTime();
        var title = document.getTitle() == null || document.getTitle().isBlank() ? "documento clinico" : document.getTitle();
        var type = document.getDocumentType() == null ? "other" : document.getDocumentType();
        var latencyMs = Math.toIntExact((System.nanoTime() - started) / 1_000_000L);

        return new DocumentAIResult(
            PROVIDER,
            null,
            properties.getPromptVersion(),
            null,
            null,
            null,
            latencyMs,
            null,
            Map.of("mode", "simulated", "documentType", type),
            MODEL,
            "Analisis simulado del documento \"" + title + "\". El contenido fue procesado como apoyo documental y requiere revision profesional.",
            List.of(
                "Documento clasificado como " + type + ".",
                "Se detecta informacion clinica que debe contrastarse con anamnesis, examen fisico e historia del paciente.",
                "No se identifican alertas criticas desde el analisis simulado."
            ),
            List.of("Diagnosticos mencionados no disponibles en analisis simulado"),
            List.of("Medicamentos mencionados no disponibles en analisis simulado"),
            List.of("Alergias mencionadas no disponibles en analisis simulado"),
            "Revisar el documento original y validar todo hallazgo con criterio clinico. Este resultado no constituye diagnostico medico ni indicacion terapeutica.",
            List.of(
                new DocumentAIInsight(
                    "clinical_summary",
                    "Resumen documental preliminar",
                    "Documento cargado para revision clinica. No se pudo extraer contenido estructurado en esta version.",
                    title,
                    0.62
                ),
                new DocumentAIInsight(
                    "clinical_alert",
                    "Requiere revision profesional",
                    "La informacion del documento debe ser contrastada con anamnesis, examen fisico e historia clinica antes de tomar decisiones.",
                    type,
                    0.58
                ),
                new DocumentAIInsight(
                    "observation_candidate",
                    "Observacion sugerida",
                    "Registrar en ficha solo si el profesional confirma que el contenido del documento es clinicamente relevante.",
                    title,
                    0.54
                )
            )
        );
    }

    @Override
    public DocumentAIProviderStatus status() {
        return new DocumentAIProviderStatus(PROVIDER, true, MODEL, properties.getPromptVersion(), true, properties.isOpenAIConfigured(), "Proveedor mock activo");
    }
}
