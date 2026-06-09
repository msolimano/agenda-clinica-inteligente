package com.iclinical.technology.diagnosiscatalog.dto;

import java.time.Instant;
import java.util.UUID;

public record DiagnosisCatalogResponse(
    UUID id,
    UUID organizationId,
    String diagnosisCode,
    String codeSystem,
    String diagnosisDisplay,
    String category,
    String description,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
}
