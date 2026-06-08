package com.iclinical.technology.professionals.dto;

import java.util.UUID;

public record ProfessionalSpecialtyResponse(UUID id, UUID specialtyId, String specialtyName, String specialtyCode, boolean primary, String status) {
}
