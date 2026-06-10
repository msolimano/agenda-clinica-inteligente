package com.iclinical.technology.bi.dto;

import java.time.Instant;
import java.util.UUID;

public record BISummaryResponse(
    Instant from,
    Instant to,
    UUID professionalId,
    UUID specialtyId,
    BIAgendaKpiResponse agenda,
    BIWaitingListKpiResponse waitingList,
    BIPatientKpiResponse patients,
    BIClinicalKpiResponse clinical,
    BIDocumentKpiResponse documents,
    BIAIKpiResponse ai,
    BIFHIRKpiResponse fhir
) {
}
