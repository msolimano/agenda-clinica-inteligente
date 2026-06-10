package com.iclinical.technology.patientportal.dto;

import java.util.List;

public record PatientPortalClinicalHistoryResponse(
    List<PatientPortalClinicalRecordResponse> records
) {
}
