package com.iclinical.technology.fhir.patient;

import com.iclinical.technology.fhir.FHIRJson;
import com.iclinical.technology.patients.Patient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
public class PatientFHIRMapper {

    public Map<String, Object> toFHIR(Patient patient) {
        var resource = FHIRJson.resource("Patient", patient.getId());
        resource.put("active", "active".equals(patient.getStatus()));
        addIdentifier(resource, patient.getDocumentType(), patient.getDocumentNumber());
        addName(resource, patient.getLastName(), patient.getFirstName());
        addTelecom(resource, patient.getEmail(), patient.getPhone());
        FHIRJson.putIfText(resource, "gender", gender(patient.getSex()));
        FHIRJson.putIfPresent(resource, "birthDate", FHIRJson.localDate(patient.getBirthDate()));
        if (StringUtils.hasText(patient.getAddress())) {
            resource.put("address", List.of(Map.of("text", patient.getAddress().trim())));
        }
        return resource;
    }

    private void addIdentifier(Map<String, Object> resource, String documentType, String documentNumber) {
        if (!StringUtils.hasText(documentNumber)) {
            return;
        }
        var identifier = FHIRJson.map();
        FHIRJson.putIfText(identifier, "system", documentTypeSystem(documentType));
        identifier.put("value", documentNumber.trim());
        resource.put("identifier", List.of(identifier));
    }

    private void addName(Map<String, Object> resource, String family, String given) {
        var name = FHIRJson.map();
        name.put("use", "official");
        FHIRJson.putIfText(name, "family", family);
        if (StringUtils.hasText(given)) {
            name.put("given", List.of(given.trim()));
        }
        resource.put("name", List.of(name));
    }

    private void addTelecom(Map<String, Object> resource, String email, String phone) {
        var telecom = new java.util.ArrayList<Map<String, Object>>();
        if (StringUtils.hasText(email)) {
            telecom.add(Map.of("system", "email", "value", email.trim()));
        }
        if (StringUtils.hasText(phone)) {
            telecom.add(Map.of("system", "phone", "value", phone.trim()));
        }
        if (!telecom.isEmpty()) {
            resource.put("telecom", telecom);
        }
    }

    private String documentTypeSystem(String documentType) {
        return StringUtils.hasText(documentType) ? "urn:iclinical:patient-document-type:" + documentType.trim().toLowerCase() : null;
    }

    private String gender(String sex) {
        if (!StringUtils.hasText(sex)) {
            return null;
        }
        return switch (sex.trim().toLowerCase()) {
            case "male", "m", "masculino" -> "male";
            case "female", "f", "femenino" -> "female";
            case "other", "otro", "otra" -> "other";
            default -> "unknown";
        };
    }
}
