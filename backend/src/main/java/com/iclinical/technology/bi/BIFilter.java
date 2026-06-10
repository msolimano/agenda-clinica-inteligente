package com.iclinical.technology.bi;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record BIFilter(
    Instant from,
    Instant to,
    UUID professionalId,
    UUID specialtyId
) {
    public static BIFilter of(Instant from, Instant to, UUID professionalId, UUID specialtyId) {
        var resolvedTo = to == null ? Instant.now() : to;
        var resolvedFrom = from == null ? resolvedTo.minus(30, ChronoUnit.DAYS) : from;
        return new BIFilter(resolvedFrom, resolvedTo, professionalId, specialtyId);
    }
}
