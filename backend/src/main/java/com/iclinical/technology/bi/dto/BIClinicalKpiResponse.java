package com.iclinical.technology.bi.dto;

import java.util.List;

public record BIClinicalKpiResponse(
    long clinicalRecordsTotal,
    long clinicalRecordsClosed,
    long evolutionsTotal,
    long diagnosesTotal,
    long prescriptionsTotal,
    List<BIBarItemResponse> topDiagnoses,
    List<BIBarItemResponse> topMedications
) {
}
