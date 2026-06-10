package com.iclinical.technology.patientportal.dto;

import java.time.Instant;
import java.util.UUID;

public record PatientPortalDocumentResponse(
    UUID id,
    UUID clinicalRecordId,
    String documentType,
    String title,
    String description,
    String fileName,
    String mimeType,
    long fileSize,
    String aiAnalysisStatus,
    String status,
    String downloadUrl,
    Instant createdAt
) {
}
