package com.iclinical.technology.appointments.availability.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ProfessionalAvailabilityRequest(
    Short weekday,
    LocalTime startTime,
    LocalTime endTime,
    Integer slotMinutes,
    String location,
    LocalDate validFrom,
    LocalDate validTo,
    String status,
    Boolean allowsOverbooking,
    Integer maxOverbookings
) {
}
