package com.iclinical.technology.bi.dto;

public record BIFHIRKpiResponse(
    long fhirBundlesGenerated,
    long fhirResourcesAvailable
) {
}
