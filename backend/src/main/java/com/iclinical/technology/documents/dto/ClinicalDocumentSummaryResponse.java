package com.iclinical.technology.documents.dto;

import java.time.Instant;
import java.util.UUID;

public record ClinicalDocumentSummaryResponse(
    UUID id,
    UUID patientId,
    String patientName,
    UUID professionalId,
    String professionalName,
    String documentType,
    String title,
    String fileName,
    String mimeType,
    long fileSize,
    String status,
    String aiAnalysisStatus,
    Instant createdAt
) {
}
