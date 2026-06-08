package com.iclinical.technology.appointments.waitinglist.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WaitingListRecommendationRequest(UUID professionalId, UUID specialtyId, OffsetDateTime startAt, OffsetDateTime endAt) {
}
