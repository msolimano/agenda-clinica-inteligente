package com.iclinical.technology.patients.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PatientSummaryResponse(
    UUID id,
    String documentType,
    String documentNumber,
    String firstName,
    String lastName,
    LocalDate birthDate,
    String sex,
    String email,
    String phone,
    String status
) {
}
