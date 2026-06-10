package com.iclinical.technology.patientportal.dto;

import java.time.Instant;
import java.util.UUID;

public record PatientPortalAIConsentResponse(
    UUID id,
    boolean active,
    String consentType,
    String consentVersion,
    Instant grantedAt,
    Instant revokedAt,
    String status
) {
}
