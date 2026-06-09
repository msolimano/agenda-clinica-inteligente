package com.iclinical.technology.clinicalevolutions.dto;

import java.time.Instant;

public record ClinicalEvolutionUpdateRequest(
    Instant evolutionDate,
    String subjective,
    String objective,
    String assessment,
    String plan,
    String notes,
    String evolutionStatus
) {
}
