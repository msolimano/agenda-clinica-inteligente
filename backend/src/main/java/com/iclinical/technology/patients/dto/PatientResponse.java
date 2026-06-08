package com.iclinical.technology.patients.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
    UUID id,
    UUID organizationId,
    UUID userId,
    String documentType,
    String documentNumber,
    String firstName,
    String lastName,
    LocalDate birthDate,
    String sex,
    String email,
    String phone,
    String address,
    String emergencyContactName,
    String emergencyContactPhone,
    String emergencyContactRelationship,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
}
