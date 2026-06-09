package com.iclinical.technology.diagnosiscatalog.dto;

import java.util.UUID;

public record DiagnosisCatalogRequest(
    UUID organizationId,
    String diagnosisCode,
    String codeSystem,
    String diagnosisDisplay,
    String category,
    String description,
    String status
) {
}
