package com.iclinical.technology.fhir.condition;

import com.iclinical.technology.clinicaldiagnoses.ClinicalDiagnosis;
import com.iclinical.technology.fhir.FHIRJson;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
public class ConditionFHIRMapper {

    public Map<String, Object> toFHIR(ClinicalDiagnosis diagnosis) {
        var resource = FHIRJson.resource("Condition", diagnosis.getId());
        resource.put("subject", FHIRJson.reference("Patient", diagnosis.getPatientId()));
        resource.put("encounter", FHIRJson.reference("Encounter", diagnosis.getClinicalRecordId()));
        resource.put("recorder", FHIRJson.reference("Practitioner", diagnosis.getProfessionalId()));
        resource.put("recordedDate", FHIRJson.instant(diagnosis.getCreatedAt()));
        resource.put("code", code(diagnosis));
        resource.put("clinicalStatus", FHIRJson.codeableConcept(null, List.of(clinicalStatusCoding(diagnosis.getDiagnosisStatus()))));
        resource.put("verificationStatus", FHIRJson.codeableConcept(null, List.of(verificationStatusCoding(diagnosis.getDiagnosisStatus()))));
        if (StringUtils.hasText(diagnosis.getObservations())) {
            resource.put("note", List.of(Map.of("text", diagnosis.getObservations().trim())));
        }
        return resource;
    }

    private Map<String, Object> code(ClinicalDiagnosis diagnosis) {
        var coding = new java.util.ArrayList<Map<String, Object>>();
        if (StringUtils.hasText(diagnosis.getDiagnosisCode())) {
            coding.add(FHIRJson.coding(diagnosis.getCodeSystem(), diagnosis.getDiagnosisCode(), diagnosis.getDiagnosisCodeDisplay()));
        }
        return FHIRJson.codeableConcept(diagnosis.getDiagnosisText(), coding);
    }

    private Map<String, Object> clinicalStatusCoding(String diagnosisStatus) {
        var code = "active";
        var display = "Active";
        if ("resolved".equals(diagnosisStatus)) {
            code = "resolved";
            display = "Resolved";
        }
        if ("ruled_out".equals(diagnosisStatus)) {
            code = "inactive";
            display = "Inactive";
        }
        return FHIRJson.coding("http://terminology.hl7.org/CodeSystem/condition-clinical", code, display);
    }

    private Map<String, Object> verificationStatusCoding(String diagnosisStatus) {
        var code = "unconfirmed";
        var display = "Unconfirmed";
        if ("confirmed".equals(diagnosisStatus) || "resolved".equals(diagnosisStatus)) {
            code = "confirmed";
            display = "Confirmed";
        }
        if ("ruled_out".equals(diagnosisStatus)) {
            code = "refuted";
            display = "Refuted";
        }
        return FHIRJson.coding("http://terminology.hl7.org/CodeSystem/condition-ver-status", code, display);
    }
}
