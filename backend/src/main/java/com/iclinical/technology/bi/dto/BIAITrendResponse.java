package com.iclinical.technology.bi.dto;

public record BIAITrendResponse(
    long completed,
    long failed,
    long pending,
    long processing
) {
}
