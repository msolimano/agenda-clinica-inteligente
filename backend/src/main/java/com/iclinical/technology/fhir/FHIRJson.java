package com.iclinical.technology.fhir;

import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FHIRJson {

    private FHIRJson() {
    }

    public static Map<String, Object> resource(String resourceType, UUID id) {
        var resource = map();
        resource.put("resourceType", resourceType);
        resource.put("id", id.toString());
        return resource;
    }

    public static Map<String, Object> map() {
        return new LinkedHashMap<>();
    }

    public static Map<String, Object> reference(String reference) {
        var value = map();
        value.put("reference", reference);
        return value;
    }

    public static Map<String, Object> reference(String resourceType, UUID id) {
        return reference(resourceType + "/" + id);
    }

    public static Map<String, Object> coding(String system, String code, String display) {
        var coding = map();
        putIfText(coding, "system", system);
        putIfText(coding, "code", code);
        putIfText(coding, "display", display);
        return coding;
    }

    public static Map<String, Object> codeableConcept(String text, List<Map<String, Object>> coding) {
        var concept = map();
        if (coding != null && !coding.isEmpty()) {
            concept.put("coding", coding);
        }
        putIfText(concept, "text", text);
        return concept;
    }

    public static void putIfText(Map<String, Object> target, String key, String value) {
        if (StringUtils.hasText(value)) {
            target.put(key, value.trim());
        }
    }

    public static void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    public static String instant(Instant value) {
        return value == null ? null : value.toString();
    }

    public static String localDate(LocalDate value) {
        return value == null ? null : value.toString();
    }
}
