package com.iclinical.technology.documents;

record StoredClinicalDocument(
    String fileName,
    String mimeType,
    long fileSize,
    String objectKey,
    String checksumSha256
) {
}
