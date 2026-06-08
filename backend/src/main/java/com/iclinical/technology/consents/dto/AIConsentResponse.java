package com.iclinical.technology.consents.dto;

import java.time.Instant;
import java.util.UUID;

public record AIConsentResponse(
    UUID id,
    UUID organizationId,
    UUID patientId,
    String consentType,
    boolean active,
    boolean granted,
    String consentVersion,
    Instant grantedAt,
    Instant revokedAt,
    String source,
    String notes,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
}
