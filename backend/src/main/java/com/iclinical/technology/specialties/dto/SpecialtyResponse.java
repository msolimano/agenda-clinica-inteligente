package com.iclinical.technology.specialties.dto;

import java.util.UUID;

public record SpecialtyResponse(
    UUID id,
    UUID organizationId,
    String name,
    String code,
    String status
) {
}
