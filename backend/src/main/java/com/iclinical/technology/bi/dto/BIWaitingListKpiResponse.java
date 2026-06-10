package com.iclinical.technology.bi.dto;

public record BIWaitingListKpiResponse(
    long waitingListTotal,
    long waitingListScheduled,
    long waitingListContacted,
    long waitingListCancelled
) {
}
