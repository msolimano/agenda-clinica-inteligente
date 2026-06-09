package com.iclinical.technology.medications.dto;

import java.util.UUID;

public record MedicationCatalogRequest(
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
    String status
) {
}
