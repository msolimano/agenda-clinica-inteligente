package com.iclinical.technology.ai;

import com.iclinical.technology.ai.extraction.DocumentTextExtractionResult;
import com.iclinical.technology.documents.ClinicalDocument;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class MockDocumentAIClient implements DocumentAIClient {

    private static final String PROVIDER = "mock";
    private static final String MODEL = "mock-clinical-document-analyzer-v2";
    private final DocumentAIProperties properties;

    MockDocumentAIClient(DocumentAIProperties properties) {
        this.properties = properties;
    }

    @Override
    public DocumentAIResult analyze(DocumentAIRequest request) {
        var started = System.nanoTime();
        var document = request.document();
        var extraction = request.textExtraction();
        var title = document.getTitle() == null || document.getTitle().isBlank() ? "documento clinico" : document.getTitle();
        var type = document.getDocumentType() == null ? "other" : document.getDocumentType();
        var text = extraction == null ? null : extraction.text();
        var normalized = normalize(text);
        var hasText = text != null && !text.isBlank();
        var latencyMs = Math.toIntExact((System.nanoTime() - started) / 1_000_000L);

        if (!hasText) {
            return conservativeResult(document, extraction, title, type, latencyMs);
        }

        var medications = detectMedications(normalized);
        var diagnoses = detectDiagnoses(normalized);
        var allergies = detectAllergies(normalized);
        var findings = detectFindings(normalized, medications);
        var insights = buildInsights(normalized, medications, findings, allergies, extraction);

        return new DocumentAIResult(
            PROVIDER,
            null,
            properties.getPromptVersion(),
            null,
            null,
            null,
            latencyMs,
            null,
            Map.of(
                "mode", "simulated_with_extracted_text",
                "documentType", type,
                "textExtractionStatus", extraction.status(),
                "textLength", text.length()
            ),
            MODEL,
            "Análisis simulado basado en texto extraído del documento \"" + title + "\". La información queda pendiente de revisión profesional.",
            findings,
            diagnoses,
            medications,
            allergies,
            recommendation(medications, normalized),
            insights
        );
    }

    @Override
    public DocumentAIProviderStatus status() {
        return new DocumentAIProviderStatus(PROVIDER, true, MODEL, properties.getPromptVersion(), true, properties.isOpenAIConfigured(), "Proveedor mock activo");
    }

    private DocumentAIResult conservativeResult(ClinicalDocument document, DocumentTextExtractionResult extraction, String title, String type, int latencyMs) {
        var extractionStatus = extraction == null ? "not_requested" : extraction.status();
        var extractionMessage = extraction == null || extraction.errorMessage() == null
            ? "No fue posible extraer texto clínico estructurado del documento."
            : extraction.errorMessage();
        return new DocumentAIResult(
            PROVIDER,
            null,
            properties.getPromptVersion(),
            null,
            null,
            null,
            latencyMs,
            null,
            Map.of("mode", "simulated_without_extracted_text", "documentType", type, "textExtractionStatus", extractionStatus),
            MODEL,
            extractionMessage,
            List.of(
                "Documento clasificado como " + type + ".",
                "No se identificó texto clínico estructurado suficiente para análisis detallado."
            ),
            List.of(),
            List.of(),
            List.of(),
            "Revisar el documento original y repetir carga/análisis si la imagen no es legible. Este resultado no constituye diagnóstico médico ni indicación terapéutica.",
            List.of(
                new DocumentAIInsight(
                    "clinical_summary",
                    "Texto clínico no disponible",
                    extractionMessage,
                    title,
                    0.20
                )
            )
        );
    }

    private List<String> detectMedications(String normalized) {
        var medications = new LinkedHashSet<String>();
        addIfPresent(medications, normalized, "rosuvastatina", "Rosuvastatina 40 mg", "Rosuvastatina");
        addIfPresent(medications, normalized, "clopidogrel", "Clopidogrel 75 mg", "Clopidogrel");
        addIfPresent(medications, normalized, "cilostazol", "Cilostazol 100 mg cada 12 horas", "Cilostazol");
        addIfPresent(medications, normalized, "rivaroxaban", "Rivaroxabán 20 mg", "Rivaroxabán");
        addIfPresent(medications, normalized, "metformina", "Metformina mencionada", "Metformina");
        addIfPresent(medications, normalized, "losartan", "Losartán mencionado", "Losartán");
        return new ArrayList<>(medications);
    }

    private List<String> detectDiagnoses(String normalized) {
        var diagnoses = new LinkedHashSet<String>();
        if (contains(normalized, "hipertension")) {
            diagnoses.add("Hipertensión mencionada");
        }
        if (contains(normalized, "diabetes")) {
            diagnoses.add("Diabetes mencionada");
        }
        return new ArrayList<>(diagnoses);
    }

    private List<String> detectAllergies(String normalized) {
        var allergies = new LinkedHashSet<String>();
        if (contains(normalized, "alerg") && contains(normalized, "penicilina")) {
            allergies.add("Alergia a penicilina mencionada");
        } else if (contains(normalized, "alerg")) {
            allergies.add("Alergia mencionada en documento");
        }
        return new ArrayList<>(allergies);
    }

    private List<String> detectFindings(String normalized, List<String> medications) {
        var findings = new LinkedHashSet<String>();
        if (contains(normalized, "vascular")) {
            findings.add("Documento emitido por especialista vascular.");
        }
        if ((contains(normalized, "actividad fisica") || contains(normalized, "actividad física")) && (contains(normalized, "retorno") || contains(normalized, "retomar") || contains(normalized, "reanudar"))) {
            findings.add("Se indica retorno a actividad física.");
        }
        if (!medications.isEmpty() && medications.stream().anyMatch(this::isCardiovascularMedication)) {
            findings.add("Se identifica tratamiento cardiovascular/vascular activo.");
        }
        if (contains(normalized, "hemoglobina")) {
            findings.add("Hemoglobina mencionada en documento.");
        }
        if (contains(normalized, "glicemia")) {
            findings.add("Glicemia mencionada en documento.");
        }
        if (contains(normalized, "creatinina")) {
            findings.add("Creatinina mencionada en documento.");
        }
        if (findings.isEmpty()) {
            findings.add("Texto clínico extraído disponible para revisión profesional.");
        }
        return new ArrayList<>(findings);
    }

    private List<DocumentAIInsight> buildInsights(String normalized, List<String> medications, List<String> findings, List<String> allergies, DocumentTextExtractionResult extraction) {
        var confidence = extraction.confidence() == null ? 0.55 : extraction.confidence().doubleValue();
        var insights = new ArrayList<DocumentAIInsight>();
        insights.add(new DocumentAIInsight(
            "clinical_summary",
            "Resumen documental preliminar",
            "Texto clínico extraído y estructurado para revisión profesional.",
            extraction.preview(),
            Math.min(0.85, confidence)
        ));
        if (!medications.isEmpty()) {
            insights.add(new DocumentAIInsight(
                "medication_candidate",
                "Medicamentos detectados",
                String.join(", ", medications),
                String.join("; ", medications),
                Math.min(0.86, confidence)
            ));
        }
        if (medications.stream().anyMatch(this::requiresAntithromboticReview)) {
            insights.add(new DocumentAIInsight(
                "clinical_alert",
                "Validar antiagregantes/anticoagulante",
                "Uso de antiagregantes/anticoagulante requiere validación clínica de vigencia, adherencia, sangrado y antecedentes vasculares.",
                String.join("; ", medications),
                Math.min(0.82, confidence)
            ));
        }
        allergies.forEach(allergy -> insights.add(new DocumentAIInsight(
            "allergy_candidate",
            "Alergia mencionada",
            allergy,
            allergy,
            Math.min(0.78, confidence)
        )));
        findings.forEach(finding -> insights.add(new DocumentAIInsight(
            "observation_candidate",
            "Observación preconsulta",
            finding,
            finding,
            Math.min(0.74, confidence)
        )));
        if (contains(normalized, "hemoglobina") || contains(normalized, "glicemia") || contains(normalized, "creatinina")) {
            insights.add(new DocumentAIInsight(
                "lab_result_candidate",
                "Exámenes mencionados",
                "El documento menciona exámenes o parámetros de laboratorio. Revisar valores y fechas antes de la consulta.",
                "hemoglobina/glicemia/creatinina",
                Math.min(0.72, confidence)
            ));
        }
        return insights;
    }

    private String recommendation(List<String> medications, String normalized) {
        if (medications.stream().anyMatch(this::requiresAntithromboticReview)) {
            return "Validar vigencia, adherencia y antecedentes vasculares durante la consulta. Revisar especialmente uso combinado de antiagregantes y anticoagulante.";
        }
        if (!medications.isEmpty()) {
            return "Validar vigencia, dosis, adherencia y motivo clínico de los medicamentos mencionados durante la consulta.";
        }
        if (contains(normalized, "alerg")) {
            return "Confirmar alergias mencionadas antes de indicar medicamentos o procedimientos.";
        }
        return "Revisar el documento original y validar todo hallazgo con criterio clínico. Este resultado no constituye diagnóstico médico ni indicación terapéutica.";
    }

    private void addIfPresent(LinkedHashSet<String> medications, String normalized, String token, String withDose, String withoutDose) {
        if (!contains(normalized, token)) {
            return;
        }
        if (contains(normalized, token + " 40") || contains(normalized, token + " 75") || contains(normalized, token + " 100") || contains(normalized, token + " 20")) {
            medications.add(withDose);
        } else {
            medications.add(withoutDose);
        }
    }

    private boolean isCardiovascularMedication(String medication) {
        var normalized = normalize(medication);
        return contains(normalized, "rosuvastatina") || contains(normalized, "clopidogrel") || contains(normalized, "cilostazol") || contains(normalized, "rivaroxaban") || contains(normalized, "losartan");
    }

    private boolean requiresAntithromboticReview(String medication) {
        var normalized = normalize(medication);
        return contains(normalized, "clopidogrel") || contains(normalized, "cilostazol") || contains(normalized, "rivaroxaban");
    }

    private boolean contains(String normalized, String token) {
        return normalized.contains(normalize(token));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        var normalized = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").replaceAll("\\s+", " ").trim();
    }
}
