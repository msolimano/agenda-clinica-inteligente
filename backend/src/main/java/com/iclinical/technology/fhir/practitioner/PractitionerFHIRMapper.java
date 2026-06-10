package com.iclinical.technology.fhir.practitioner;

import com.iclinical.technology.fhir.FHIRJson;
import com.iclinical.technology.professionals.Professional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
public class PractitionerFHIRMapper {

    public Map<String, Object> toFHIR(Professional professional) {
        var resource = FHIRJson.resource("Practitioner", professional.getId());
        resource.put("active", "active".equals(professional.getStatus()));
        addIdentifiers(resource, professional);
        addName(resource, professional.getLastName(), professional.getFirstName());
        addTelecom(resource, professional.getEmail(), professional.getPhone());
        return resource;
    }

    private void addIdentifiers(Map<String, Object> resource, Professional professional) {
        var identifiers = new java.util.ArrayList<Map<String, Object>>();
        if (StringUtils.hasText(professional.getDocumentNumber())) {
            var identifier = FHIRJson.map();
            FHIRJson.putIfText(identifier, "system", documentTypeSystem(professional.getDocumentType()));
            identifier.put("value", professional.getDocumentNumber().trim());
            identifiers.add(identifier);
        }
        if (StringUtils.hasText(professional.getRegistryNumber())) {
            identifiers.add(Map.of(
                "system", "urn:iclinical:professional-registry",
                "value", professional.getRegistryNumber().trim()
            ));
        }
        if (!identifiers.isEmpty()) {
            resource.put("identifier", identifiers);
        }
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
        return StringUtils.hasText(documentType) ? "urn:iclinical:professional-document-type:" + documentType.trim().toLowerCase() : null;
    }
}
