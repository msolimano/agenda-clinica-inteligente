package com.iclinical.technology.fhir.encounter;

import com.iclinical.technology.clinicalrecords.ClinicalRecord;
import com.iclinical.technology.fhir.FHIRJson;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
public class EncounterFHIRMapper {

    public Map<String, Object> toFHIR(ClinicalRecord record) {
        var resource = FHIRJson.resource("Encounter", record.getId());
        resource.put("status", encounterStatus(record.getStatus()));
        resource.put("class", Map.of(
            "system", "http://terminology.hl7.org/CodeSystem/v3-ActCode",
            "code", "AMB",
            "display", "ambulatory"
        ));
        resource.put("subject", FHIRJson.reference("Patient", record.getPatientId()));
        resource.put("participant", List.of(Map.of(
            "individual", FHIRJson.reference("Practitioner", record.getProfessionalId())
        )));
        if (record.getAppointmentId() != null) {
            resource.put("appointment", List.of(FHIRJson.reference("Appointment", record.getAppointmentId())));
        }
        var period = FHIRJson.map();
        FHIRJson.putIfPresent(period, "start", FHIRJson.instant(record.getRecordDate()));
        FHIRJson.putIfPresent(period, "end", FHIRJson.instant(record.getFinalizedAt()));
        if (!period.isEmpty()) {
            resource.put("period", period);
        }
        if (StringUtils.hasText(record.getChiefComplaint())) {
            resource.put("reasonCode", List.of(FHIRJson.codeableConcept(record.getChiefComplaint(), null)));
        }
        return resource;
    }

    private String encounterStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "unknown";
        }
        return switch (status.trim().toLowerCase()) {
            case "draft" -> "planned";
            case "open" -> "in-progress";
            case "closed" -> "finished";
            default -> "unknown";
        };
    }
}
