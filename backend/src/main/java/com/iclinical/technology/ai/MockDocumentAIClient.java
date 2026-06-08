package com.iclinical.technology.ai;

import com.iclinical.technology.documents.ClinicalDocument;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class MockDocumentAIClient implements DocumentAIClient {

    @Override
    public DocumentAIResult analyze(ClinicalDocument document) {
        var title = document.getTitle() == null || document.getTitle().isBlank() ? "documento clinico" : document.getTitle();
        var type = document.getDocumentType() == null ? "other" : document.getDocumentType();

        return new DocumentAIResult(
            "mock-clinical-document-analyzer-v1",
            "Analisis simulado del documento \"" + title + "\". El contenido fue procesado como apoyo documental y requiere revision profesional.",
            List.of(
                "Documento clasificado como " + type + ".",
                "Se detecta informacion clinica que debe contrastarse con anamnesis, examen fisico e historia del paciente.",
                "No se identifican alertas criticas desde el analisis simulado."
            ),
            List.of("Diagnosticos mencionados no disponibles en analisis simulado"),
            List.of("Medicamentos mencionados no disponibles en analisis simulado"),
            List.of("Alergias mencionadas no disponibles en analisis simulado"),
            "Revisar el documento original y validar todo hallazgo con criterio clinico. Este resultado no constituye diagnostico medico ni indicacion terapeutica."
        );
    }
}
