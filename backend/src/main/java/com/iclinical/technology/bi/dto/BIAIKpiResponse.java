package com.iclinical.technology.bi.dto;

public record BIAIKpiResponse(
    long aiAnalysesTotal,
    long aiAnalysesCompleted,
    long aiAnalysesFailed,
    long aiConsentsActive
) {
}
