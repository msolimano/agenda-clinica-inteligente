package com.iclinical.technology.medications.dto;

import java.time.Instant;
import java.util.UUID;

public record MedicationCatalogResponse(
    UUID id,
    UUID organizationId,
    String medicationCode,
    String medicationCodeSystem,
    String medicationName,
    String activeIngredient,
    String presentation,
    String strength,
    String pharmaceuticalForm,
    String route,
    String manufacturer,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
}
