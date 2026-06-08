package com.iclinical.technology.documents;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.storage")
public class ClinicalDocumentStorageProperties {

    private String provider = "local";
    private String localBasePath = "./storage";
    private long maxFileSizeBytes = 20L * 1024L * 1024L;
    private List<String> allowedMimeTypes = List.of(
        "application/pdf",
        "image/jpeg",
        "image/png",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getLocalBasePath() { return localBasePath; }
    public void setLocalBasePath(String localBasePath) { this.localBasePath = localBasePath; }
    public long getMaxFileSizeBytes() { return maxFileSizeBytes; }
    public void setMaxFileSizeBytes(long maxFileSizeBytes) { this.maxFileSizeBytes = maxFileSizeBytes; }
    public List<String> getAllowedMimeTypes() { return allowedMimeTypes; }
    public void setAllowedMimeTypes(List<String> allowedMimeTypes) { this.allowedMimeTypes = allowedMimeTypes; }
}
