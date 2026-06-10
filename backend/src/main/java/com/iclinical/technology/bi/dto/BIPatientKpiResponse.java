package com.iclinical.technology.bi.dto;

public record BIPatientKpiResponse(
    long totalPatients,
    long activePatients,
    long newPatientsInRange
) {
}
