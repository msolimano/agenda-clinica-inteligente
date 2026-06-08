package com.iclinical.technology.documents;

import org.springframework.core.io.Resource;

record ClinicalDocumentDownload(
    String fileName,
    String mimeType,
    long fileSize,
    Resource resource
) {
}
