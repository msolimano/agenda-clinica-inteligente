package com.iclinical.technology.bi.dto;

public record BIAgendaResponse(
    BIAgendaKpiResponse agenda,
    BIWaitingListKpiResponse waitingList,
    BIPatientKpiResponse patients
) {
}
