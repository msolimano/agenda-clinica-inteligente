package com.iclinical.technology.professionals.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProfessionalUpdateRequest(
    UUID userId,
    @Size(max = 40) String documentType,
    @Size(max = 80) String documentNumber,
    @Size(max = 80) String registryNumber,
    @NotBlank @Size(max = 120) String firstName,
    @NotBlank @Size(max = 120) String lastName,
    @Email @Size(max = 255) String email,
    @Size(max = 40) String phone
) {
}
