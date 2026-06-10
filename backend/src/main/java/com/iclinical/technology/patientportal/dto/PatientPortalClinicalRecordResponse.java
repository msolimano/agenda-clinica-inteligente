package com.iclinical.technology.patientportal.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PatientPortalClinicalRecordResponse(
    UUID id,
    UUID professionalId,
    String professionalName,
    UUID appointmentId,
    Instant recordDate,
    String chiefComplaint,
    String assessment,
    String plan,
    String status,
    List<PatientPortalEvolutionResponse> evolutions
) {
}
