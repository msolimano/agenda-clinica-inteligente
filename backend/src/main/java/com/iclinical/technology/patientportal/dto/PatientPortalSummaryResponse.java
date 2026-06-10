package com.iclinical.technology.patientportal.dto;

public record PatientPortalSummaryResponse(
    PatientPortalPatientResponse patient,
    long upcomingAppointments,
    long historicalAppointments,
    long activeDocuments,
    long activePrescriptions,
    long activeDiagnoses,
    long clinicalRecords,
    PatientPortalAIConsentResponse aiConsent
) {
}
