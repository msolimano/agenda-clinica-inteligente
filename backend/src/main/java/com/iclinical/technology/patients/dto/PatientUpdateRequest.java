package com.iclinical.technology.patients.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record PatientUpdateRequest(
    UUID userId,
    @Size(max = 40) String documentType,
    @Size(max = 80) String documentNumber,
    @NotBlank @Size(max = 120) String firstName,
    @NotBlank @Size(max = 120) String lastName,
    @PastOrPresent LocalDate birthDate,
    @Size(max = 30) String sex,
    @Email @Size(max = 255) String email,
    @Size(max = 40) String phone,
    @Size(max = 300) String address,
    @Size(max = 160) String emergencyContactName,
    @Size(max = 40) String emergencyContactPhone,
    @Size(max = 80) String emergencyContactRelationship
) {
}
