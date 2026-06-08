package com.iclinical.technology.documents.dto;

import java.time.Instant;
import java.util.UUID;

public record ClinicalDocumentResponse(
    UUID id,
    UUID organizationId,
    UUID patientId,
    String patientName,
    UUID professionalId,
    String professionalName,
    String documentType,
    String title,
    String description,
    String fileName,
    String mimeType,
    long fileSize,
    String storagePath,
    String checksumSha256,
    String aiAnalysisStatus,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
}
