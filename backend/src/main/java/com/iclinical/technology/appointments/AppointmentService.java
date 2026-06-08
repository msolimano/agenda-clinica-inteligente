package com.iclinical.technology.appointments;

import com.iclinical.technology.appointments.dto.AppointmentCancelRequest;
import com.iclinical.technology.appointments.dto.AppointmentCreateRequest;
import com.iclinical.technology.appointments.dto.AppointmentRescheduleRequest;
import com.iclinical.technology.appointments.dto.AppointmentResponse;
import com.iclinical.technology.appointments.dto.AppointmentSlotResponse;
import com.iclinical.technology.patients.Patient;
import com.iclinical.technology.patients.PatientRepository;
import com.iclinical.technology.professionals.Professional;
import com.iclinical.technology.professionals.ProfessionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AppointmentService {

    private static final Set<String> BLOCKING_STATUSES = Set.of("scheduled", "confirmed", "completed", "blocked");
    private static final Set<String> APPOINTMENT_TYPES = Set.of("consultation", "control", "procedure");
    private static final ZoneId AGENDA_ZONE = ZoneId.of("America/Santiago");

    private final AppointmentRepository appointmentRepository;
    private final ProfessionalAvailabilityRepository availabilityRepository;
    private final ProfessionalRepository professionalRepository;
    private final PatientRepository patientRepository;

    public AppointmentService(
        AppointmentRepository appointmentRepository,
        ProfessionalAvailabilityRepository availabilityRepository,
        ProfessionalRepository professionalRepository,
        PatientRepository patientRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.availabilityRepository = availabilityRepository;
        this.professionalRepository = professionalRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getCalendar(UUID professionalId, OffsetDateTime from, OffsetDateTime to) {
        if (from == null || to == null || !from.toInstant().isBefore(to.toInstant())) {
            throw new AppointmentValidationException("Debe indicar un rango de fechas valido");
        }

        findActiveProfessional(professionalId);
        return appointmentRepository.findByProfessionalIdAndStartAtLessThanAndEndAtGreaterThanAndStatusNotOrderByStartAtAsc(
                professionalId,
                to.toInstant(),
                from.toInstant(),
                "deleted"
            )
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public List<AppointmentSlotResponse> getSlots(UUID professionalId, LocalDate date) {
        if (date == null) {
            throw new AppointmentValidationException("Debe indicar una fecha para consultar cupos");
        }

        var professional = findActiveProfessional(professionalId);
        var availabilityRules = activeAvailabilityFor(professional, date);
        var dayStart = date.atStartOfDay(AGENDA_ZONE).toInstant();
        var dayEnd = date.plusDays(1).atStartOfDay(AGENDA_ZONE).toInstant();
        var appointments = appointmentRepository.findByProfessionalIdAndStartAtLessThanAndEndAtGreaterThanAndStatusNotOrderByStartAtAsc(
            professionalId,
            dayEnd,
            dayStart,
            "deleted"
        );

        return availabilityRules.stream()
            .flatMap(rule -> buildSlotsForRule(date, rule, appointments).stream())
            .sorted(Comparator.comparing(AppointmentSlotResponse::startAt))
            .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getById(UUID id) {
        return toResponse(findAppointment(id));
    }

    @Transactional
    public AppointmentResponse create(AppointmentCreateRequest request) {
        var professional = findActiveProfessional(request.professionalId());
        var patient = findActivePatient(request.patientId());
        validateSameOrganization(professional, patient);

        var startAt = toInstant(request.startAt());
        var endAt = toInstant(request.endAt());
        validateAppointmentWindow(professional, startAt, endAt, null);

        var appointment = new Appointment();
        appointment.setOrganizationId(professional.getOrganizationId());
        appointment.setProfessionalId(professional.getId());
        appointment.setPatientId(patient.getId());
        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);
        appointment.setAppointmentType(resolveAppointmentType(request.appointmentType()));
        appointment.setReason(clean(request.reason()));
        appointment.setStatus("scheduled");

        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse reschedule(UUID id, AppointmentRescheduleRequest request) {
        var original = findAppointment(id);
        if (!canChangeSchedule(original)) {
            throw new AppointmentValidationException("Solo se pueden reagendar citas programadas o confirmadas");
        }

        var professional = findActiveProfessional(original.getProfessionalId());
        var startAt = toInstant(request.startAt());
        var endAt = toInstant(request.endAt());
        original.setStatus("cancelled");
        original.setCancellationReason("Reagendada");
        original.setCancelledAt(Instant.now());

        validateAppointmentWindow(professional, startAt, endAt, original.getId());

        var rescheduled = new Appointment();
        rescheduled.setOrganizationId(original.getOrganizationId());
        rescheduled.setProfessionalId(original.getProfessionalId());
        rescheduled.setPatientId(original.getPatientId());
        rescheduled.setStartAt(startAt);
        rescheduled.setEndAt(endAt);
        rescheduled.setAppointmentType(original.getAppointmentType());
        rescheduled.setReason(StringUtils.hasText(request.reason()) ? request.reason().trim() : original.getReason());
        rescheduled.setStatus("scheduled");
        rescheduled.setRescheduledFromId(original.getId());

        return toResponse(appointmentRepository.save(rescheduled));
    }

    @Transactional
    public AppointmentResponse cancel(UUID id, AppointmentCancelRequest request) {
        var appointment = findAppointment(id);
        if ("cancelled".equals(appointment.getStatus())) {
            return toResponse(appointment);
        }
        if ("completed".equals(appointment.getStatus())) {
            throw new AppointmentValidationException("Una cita completada no puede cancelarse");
        }

        appointment.setStatus("cancelled");
        appointment.setCancellationReason(clean(request == null ? null : request.cancellationReason()));
        appointment.setCancelledAt(Instant.now());
        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse confirm(UUID id) {
        var appointment = findAppointment(id);
        if (!"scheduled".equals(appointment.getStatus())) {
            throw new AppointmentValidationException("Solo se pueden confirmar citas programadas");
        }

        appointment.setStatus("confirmed");
        appointment.setConfirmedAt(Instant.now());
        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse noShow(UUID id) {
        var appointment = findAppointment(id);
        if (!"scheduled".equals(appointment.getStatus()) && !"confirmed".equals(appointment.getStatus())) {
            throw new AppointmentValidationException("Solo se puede registrar inasistencia en citas programadas o confirmadas");
        }

        appointment.setStatus("no_show");
        appointment.setNoShowAt(Instant.now());
        return toResponse(appointment);
    }

    private List<AppointmentSlotResponse> buildSlotsForRule(LocalDate date, ProfessionalAvailability rule, List<Appointment> appointments) {
        var slots = new java.util.ArrayList<AppointmentSlotResponse>();
        var cursor = rule.getStartTime();
        var end = rule.getEndTime();
        var step = rule.getSlotMinutes();

        while (!cursor.plusMinutes(step).isAfter(end)) {
            var slotStart = ZonedDateTime.of(date, cursor, AGENDA_ZONE).toInstant();
            var slotEnd = ZonedDateTime.of(date, cursor.plusMinutes(step), AGENDA_ZONE).toInstant();
            var occupied = appointments.stream()
                .filter(appointment -> overlaps(appointment.getStartAt(), appointment.getEndAt(), slotStart, slotEnd))
                .filter(appointment -> !"cancelled".equals(appointment.getStatus()) && !"no_show".equals(appointment.getStatus()))
                .findFirst();

            if (occupied.isPresent()) {
                var appointment = occupied.get();
                slots.add(new AppointmentSlotResponse(slotStart, slotEnd, appointment.getStatus(), appointment.getId(), patientName(appointment.getPatientId()), appointment.getReason()));
            } else {
                slots.add(new AppointmentSlotResponse(slotStart, slotEnd, "available", null, null, null));
            }

            cursor = cursor.plusMinutes(step);
        }

        return slots;
    }

    private List<ProfessionalAvailability> activeAvailabilityFor(Professional professional, LocalDate date) {
        var availability = availabilityRepository.findByProfessionalIdAndStatusOrderByWeekdayAscStartTimeAsc(professional.getId(), "active");
        if (availability.isEmpty()) {
            createDefaultAvailability(professional);
            availability = availabilityRepository.findByProfessionalIdAndStatusOrderByWeekdayAscStartTimeAsc(professional.getId(), "active");
        }

        var weekday = (short) date.getDayOfWeek().getValue();
        return availability.stream()
            .filter(rule -> rule.getWeekday() == weekday)
            .filter(rule -> rule.getValidFrom() == null || !date.isBefore(rule.getValidFrom()))
            .filter(rule -> rule.getValidTo() == null || !date.isAfter(rule.getValidTo()))
            .toList();
    }

    private void createDefaultAvailability(Professional professional) {
        for (short weekday = 1; weekday <= 5; weekday++) {
            var availability = new ProfessionalAvailability();
            availability.setOrganizationId(professional.getOrganizationId());
            availability.setProfessionalId(professional.getId());
            availability.setWeekday(weekday);
            availability.setStartTime(LocalTime.of(9, 0));
            availability.setEndTime(LocalTime.of(17, 0));
            availability.setSlotMinutes(30);
            availability.setLocation("Consulta principal");
            availability.setStatus("active");
            availabilityRepository.save(availability);
        }
    }

    private void validateAppointmentWindow(Professional professional, Instant startAt, Instant endAt, UUID excludedAppointmentId) {
        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) {
            throw new AppointmentValidationException("El horario de la cita no es valido");
        }

        var date = ZonedDateTime.ofInstant(startAt, AGENDA_ZONE).toLocalDate();
        var endDate = ZonedDateTime.ofInstant(endAt, AGENDA_ZONE).toLocalDate();
        if (!date.equals(endDate)) {
            throw new AppointmentValidationException("La cita debe iniciar y terminar el mismo dia");
        }

        var isInsideAvailability = activeAvailabilityFor(professional, date).stream()
            .anyMatch(rule -> isInsideRule(date, rule, startAt, endAt));
        if (!isInsideAvailability) {
            throw new AppointmentValidationException("El horario no pertenece a la disponibilidad del profesional");
        }

        if (appointmentRepository.existsBlockingOverlap(professional.getId(), startAt, endAt, BLOCKING_STATUSES, excludedAppointmentId)) {
            throw new AppointmentValidationException("El horario ya se encuentra ocupado");
        }
    }

    private boolean isInsideRule(LocalDate date, ProfessionalAvailability rule, Instant startAt, Instant endAt) {
        var ruleStart = ZonedDateTime.of(date, rule.getStartTime(), AGENDA_ZONE).toInstant();
        var ruleEnd = ZonedDateTime.of(date, rule.getEndTime(), AGENDA_ZONE).toInstant();
        return !startAt.isBefore(ruleStart) && !endAt.isAfter(ruleEnd);
    }

    private boolean overlaps(Instant firstStart, Instant firstEnd, Instant secondStart, Instant secondEnd) {
        return firstStart.isBefore(secondEnd) && firstEnd.isAfter(secondStart);
    }

    private Professional findActiveProfessional(UUID professionalId) {
        return professionalRepository.findById(professionalId)
            .filter(professional -> "active".equals(professional.getStatus()))
            .orElseThrow(() -> new AppointmentValidationException("El profesional indicado no existe o no esta activo"));
    }

    private Patient findActivePatient(UUID patientId) {
        return patientRepository.findById(patientId)
            .filter(patient -> "active".equals(patient.getStatus()))
            .orElseThrow(() -> new AppointmentValidationException("El paciente indicado no existe o no esta activo"));
    }

    private Appointment findAppointment(UUID id) {
        return appointmentRepository.findById(id)
            .filter(appointment -> !"deleted".equals(appointment.getStatus()))
            .orElseThrow(() -> new AppointmentNotFoundException("Cita no encontrada"));
    }

    private void validateSameOrganization(Professional professional, Patient patient) {
        if (!professional.getOrganizationId().equals(patient.getOrganizationId())) {
            throw new AppointmentValidationException("Profesional y paciente deben pertenecer a la misma organizacion");
        }
    }

    private Instant toInstant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    private boolean canChangeSchedule(Appointment appointment) {
        return "scheduled".equals(appointment.getStatus()) || "confirmed".equals(appointment.getStatus());
    }

    private String resolveAppointmentType(String value) {
        var cleaned = StringUtils.hasText(value) ? value.trim() : "consultation";
        if (!APPOINTMENT_TYPES.contains(cleaned)) {
            throw new AppointmentValidationException("Tipo de cita permitido: consultation, control o procedure");
        }
        return cleaned;
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
            appointment.getId(),
            appointment.getOrganizationId(),
            appointment.getProfessionalId(),
            professionalName(appointment.getProfessionalId()),
            appointment.getPatientId(),
            patientName(appointment.getPatientId()),
            appointment.getStartAt(),
            appointment.getEndAt(),
            appointment.getAppointmentType(),
            appointment.getStatus(),
            appointment.getReason(),
            appointment.getCancellationReason(),
            appointment.getRescheduledFromId(),
            appointment.getConfirmedAt(),
            appointment.getCancelledAt(),
            appointment.getNoShowAt(),
            appointment.isOverbooking(),
            appointment.getCreatedAt(),
            appointment.getUpdatedAt()
        );
    }

    private String professionalName(UUID professionalId) {
        return professionalRepository.findById(professionalId)
            .map(professional -> professional.getFirstName() + " " + professional.getLastName())
            .orElse("Profesional no disponible");
    }

    private String patientName(UUID patientId) {
        if (patientId == null) {
            return null;
        }
        return patientRepository.findById(patientId)
            .map(patient -> patient.getFirstName() + " " + patient.getLastName())
            .orElse("Paciente no disponible");
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
