package com.iclinical.technology.patientportal.dto;

import java.time.Instant;
import java.util.UUID;

public record PatientPortalEvolutionResponse(
    UUID id,
    UUID clinicalRecordId,
    String professionalName,
    Instant evolutionDate,
    String subjective,
    String objective,
    String assessment,
    String plan,
    String notes,
    String evolutionStatus,
    boolean cancelled,
    Instant createdAt,
    Instant updatedAt
) {
}
