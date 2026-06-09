package com.iclinical.technology.clinicalevolutions.dto;

import java.time.Instant;
import java.util.UUID;

public record ClinicalEvolutionResponse(
    UUID id,
    UUID organizationId,
    UUID clinicalRecordId,
    UUID patientId,
    String patientName,
    UUID professionalId,
    String professionalName,
    Instant evolutionDate,
    String subjective,
    String objective,
    String assessment,
    String plan,
    String notes,
    String evolutionStatus,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
}
