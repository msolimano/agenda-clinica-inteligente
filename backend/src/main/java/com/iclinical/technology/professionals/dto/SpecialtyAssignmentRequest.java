package com.iclinical.technology.professionals.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SpecialtyAssignmentRequest(@NotNull UUID specialtyId, Boolean primary) {
}
