package com.iclinical.technology.clinicalinsights.dto;

import java.util.UUID;

public record ClinicalInsightReviewRequest(
    UUID reviewedByProfessionalId,
    String reviewNotes
) {
}
