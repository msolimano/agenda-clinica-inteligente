package com.iclinical.technology.professionals.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProfessionalResponse(
    UUID id,
    UUID organizationId,
    UUID userId,
    String documentType,
    String documentNumber,
    String registryNumber,
    String firstName,
    String lastName,
    String email,
    String phone,
    String status,
    List<ProfessionalSpecialtyResponse> specialties,
    Instant createdAt,
    Instant updatedAt
) {
}
