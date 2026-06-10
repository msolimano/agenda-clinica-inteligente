package com.iclinical.technology.fhir.document;

import com.iclinical.technology.documents.ClinicalDocument;
import com.iclinical.technology.fhir.FHIRJson;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
public class DocumentReferenceFHIRMapper {

    public Map<String, Object> toFHIR(ClinicalDocument document) {
        var resource = FHIRJson.resource("DocumentReference", document.getId());
        resource.put("status", "current");
        resource.put("docStatus", "final");
        resource.put("type", FHIRJson.codeableConcept(documentTypeDisplay(document.getDocumentType()), List.of(
            FHIRJson.coding("urn:iclinical:document-type", document.getDocumentType(), documentTypeDisplay(document.getDocumentType()))
        )));
        resource.put("subject", FHIRJson.reference("Patient", document.getPatientId()));
        if (document.getProfessionalId() != null) {
            resource.put("author", List.of(FHIRJson.reference("Practitioner", document.getProfessionalId())));
        }
        if (document.getClinicalRecordId() != null) {
            resource.put("context", Map.of("encounter", List.of(FHIRJson.reference("Encounter", document.getClinicalRecordId()))));
        }
        resource.put("date", FHIRJson.instant(document.getCreatedAt()));
        FHIRJson.putIfText(resource, "description", description(document));
        resource.put("content", List.of(Map.of("attachment", attachment(document))));
        return resource;
    }

    private Map<String, Object> attachment(ClinicalDocument document) {
        var attachment = FHIRJson.map();
        FHIRJson.putIfText(attachment, "contentType", document.getMimeType());
        FHIRJson.putIfText(attachment, "title", title(document));
        FHIRJson.putIfPresent(attachment, "size", document.getFileSizeBytes());
        attachment.put("url", "/api/clinical-documents/" + document.getId() + "/download");
        return attachment;
    }

    private String title(ClinicalDocument document) {
        return StringUtils.hasText(document.getTitle()) ? document.getTitle() : document.getOriginalFilename();
    }

    private String description(ClinicalDocument document) {
        if (StringUtils.hasText(document.getDescription())) {
            return document.getDescription();
        }
        return title(document);
    }

    private String documentTypeDisplay(String documentType) {
        if (!StringUtils.hasText(documentType)) {
            return "Clinical document";
        }
        return switch (documentType.trim()) {
            case "medical_report" -> "Medical report";
            case "laboratory_exam" -> "Laboratory exam";
            case "imaging_exam" -> "Imaging exam";
            case "prescription" -> "Prescription";
            case "medical_order" -> "Medical order";
            case "certificate" -> "Certificate";
            default -> "Other clinical document";
        };
    }
}
