package com.iclinical.technology.appointments.dto;

import jakarta.validation.constraints.Size;

public record AppointmentCancelRequest(@Size(max = 300) String cancellationReason) {
}
