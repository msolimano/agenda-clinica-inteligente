package com.iclinical.technology.fhir.bundle;

import java.time.Instant;
import java.util.UUID;

public record FHIRBundleRequest(
    UUID patientId,
    Instant from,
    Instant to,
    boolean includeDocuments,
    boolean includePrescriptions,
    boolean includeDiagnoses,
    boolean includeEncounters
) {
}
