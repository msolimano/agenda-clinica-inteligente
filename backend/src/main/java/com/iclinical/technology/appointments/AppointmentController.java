package com.iclinical.technology.appointments;

import com.iclinical.technology.appointments.dto.AppointmentCancelRequest;
import com.iclinical.technology.appointments.dto.AppointmentCreateRequest;
import com.iclinical.technology.appointments.dto.AppointmentRescheduleRequest;
import com.iclinical.technology.appointments.dto.AppointmentResponse;
import com.iclinical.technology.appointments.dto.AppointmentSlotResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/calendar")
    public List<AppointmentResponse> getCalendar(
        @RequestParam UUID professionalId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return appointmentService.getCalendar(professionalId, from, to);
    }

    @GetMapping("/slots")
    public List<AppointmentSlotResponse> getSlots(
        @RequestParam UUID professionalId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return appointmentService.getSlots(professionalId, date);
    }

    @GetMapping("/{id}")
    public AppointmentResponse getAppointment(@PathVariable UUID id) {
        return appointmentService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse createAppointment(@Valid @RequestBody AppointmentCreateRequest request) {
        return appointmentService.create(request);
    }

    @PutMapping("/{id}/reschedule")
    public AppointmentResponse rescheduleAppointment(@PathVariable UUID id, @Valid @RequestBody AppointmentRescheduleRequest request) {
        return appointmentService.reschedule(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public AppointmentResponse cancelAppointment(@PathVariable UUID id, @RequestBody(required = false) AppointmentCancelRequest request) {
        return appointmentService.cancel(id, request);
    }

    @PatchMapping("/{id}/confirm")
    public AppointmentResponse confirmAppointment(@PathVariable UUID id) {
        return appointmentService.confirm(id);
    }

    @PatchMapping("/{id}/no-show")
    public AppointmentResponse registerNoShow(@PathVariable UUID id) {
        return appointmentService.noShow(id);
    }
}
