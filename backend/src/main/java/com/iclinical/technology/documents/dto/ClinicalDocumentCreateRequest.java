package com.iclinical.technology.documents.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record ClinicalDocumentCreateRequest(
    UUID patientId,
    UUID professionalId,
    String documentType,
    String title,
    String description,
    MultipartFile file
) {
}
