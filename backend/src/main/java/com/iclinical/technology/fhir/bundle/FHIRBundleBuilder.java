package com.iclinical.technology.fhir.bundle;

import com.iclinical.technology.fhir.FHIRJson;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class FHIRBundleBuilder {

    public Map<String, Object> collection(List<Map<String, Object>> resources) {
        var bundle = FHIRJson.map();
        var entries = new ArrayList<Map<String, Object>>();
        for (var resource : resources) {
            entries.add(entry(resource));
        }
        bundle.put("resourceType", "Bundle");
        bundle.put("type", "collection");
        bundle.put("timestamp", Instant.now().toString());
        bundle.put("total", entries.size());
        bundle.put("entry", entries);
        return bundle;
    }

    private Map<String, Object> entry(Map<String, Object> resource) {
        var entry = FHIRJson.map();
        entry.put("fullUrl", "urn:uuid:" + resource.get("id"));
        entry.put("resource", resource);
        return entry;
    }
}
