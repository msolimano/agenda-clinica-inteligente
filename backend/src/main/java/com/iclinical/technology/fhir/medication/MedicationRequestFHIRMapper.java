package com.iclinical.technology.fhir.medication;

import com.iclinical.technology.clinicalprescriptions.ClinicalPrescription;
import com.iclinical.technology.fhir.FHIRJson;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
public class MedicationRequestFHIRMapper {

    public Map<String, Object> toFHIR(ClinicalPrescription prescription) {
        var resource = FHIRJson.resource("MedicationRequest", prescription.getId());
        resource.put("status", medicationRequestStatus(prescription.getPrescriptionStatus()));
        resource.put("intent", "order");
        resource.put("subject", FHIRJson.reference("Patient", prescription.getPatientId()));
        resource.put("requester", FHIRJson.reference("Practitioner", prescription.getProfessionalId()));
        resource.put("encounter", FHIRJson.reference("Encounter", prescription.getClinicalRecordId()));
        if (prescription.getDiagnosisId() != null) {
            resource.put("reasonReference", List.of(FHIRJson.reference("Condition", prescription.getDiagnosisId())));
        }
        resource.put("medicationCodeableConcept", medicationCodeableConcept(prescription));
        resource.put("authoredOn", FHIRJson.instant(prescription.getCreatedAt()));
        resource.put("dosageInstruction", List.of(dosageInstruction(prescription)));
        if (StringUtils.hasText(prescription.getClinicalNotes())) {
            resource.put("note", List.of(Map.of("text", prescription.getClinicalNotes().trim())));
        }
        return resource;
    }

    private Map<String, Object> medicationCodeableConcept(ClinicalPrescription prescription) {
        var coding = new java.util.ArrayList<Map<String, Object>>();
        if (StringUtils.hasText(prescription.getMedicationCode())) {
            coding.add(FHIRJson.coding(
                prescription.getMedicationCodeSystem(),
                prescription.getMedicationCode(),
                prescription.getMedicationCodeDisplay()
            ));
        }
        return FHIRJson.codeableConcept(prescription.getMedicationName(), coding);
    }

    private Map<String, Object> dosageInstruction(ClinicalPrescription prescription) {
        var dosage = FHIRJson.map();
        var text = String.join(" ", List.of(
            prescription.getDosage(),
            prescription.getFrequency(),
            StringUtils.hasText(prescription.getDuration()) ? prescription.getDuration().trim() : ""
        )).trim();
        FHIRJson.putIfText(dosage, "text", text);
        if (StringUtils.hasText(prescription.getPatientInstructions())) {
            FHIRJson.putIfText(dosage, "patientInstruction", prescription.getPatientInstructions());
        }
        if (StringUtils.hasText(prescription.getRoute())) {
            dosage.put("route", FHIRJson.codeableConcept(prescription.getRoute(), null));
        }
        return dosage;
    }

    private String medicationRequestStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "unknown";
        }
        return switch (status.trim().toLowerCase()) {
            case "draft" -> "draft";
            case "active" -> "active";
            case "suspended" -> "on-hold";
            case "completed" -> "completed";
            case "cancelled" -> "cancelled";
            default -> "unknown";
        };
    }
}
