package com.iclinical.technology.appointments.overbooking;

import com.iclinical.technology.appointments.Appointment;
import com.iclinical.technology.appointments.AppointmentRepository;
import com.iclinical.technology.appointments.AppointmentValidationException;
import com.iclinical.technology.appointments.ProfessionalAvailability;
import com.iclinical.technology.appointments.ProfessionalAvailabilityRepository;
import com.iclinical.technology.appointments.overbooking.dto.OverbookingCapacityResponse;
import com.iclinical.technology.appointments.overbooking.dto.OverbookingCreateRequest;
import com.iclinical.technology.appointments.overbooking.dto.OverbookingResponse;
import com.iclinical.technology.patients.Patient;
import com.iclinical.technology.patients.PatientRepository;
import com.iclinical.technology.professionals.Professional;
import com.iclinical.technology.professionals.ProfessionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class OverbookingService {

    private static final Set<String> APPOINTMENT_TYPES = Set.of("consultation", "control", "procedure");
    private static final Set<String> NORMAL_OCCUPANCY_STATUSES = Set.of("scheduled", "confirmed", "completed");
    private static final Set<String> ACTIVE_OVERBOOKING_STATUSES = Set.of("scheduled", "confirmed");
    private static final ZoneId AGENDA_ZONE = ZoneId.of("America/Santiago");

    private final AppointmentRepository appointmentRepository;
    private final ProfessionalAvailabilityRepository availabilityRepository;
    private final ProfessionalRepository professionalRepository;
    private final PatientRepository patientRepository;
    private final OverbookingRepository overbookingRepository;

    public OverbookingService(
        AppointmentRepository appointmentRepository,
        ProfessionalAvailabilityRepository availabilityRepository,
        ProfessionalRepository professionalRepository,
        PatientRepository patientRepository,
        OverbookingRepository overbookingRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.availabilityRepository = availabilityRepository;
        this.professionalRepository = professionalRepository;
        this.patientRepository = patientRepository;
        this.overbookingRepository = overbookingRepository;
    }

    @Transactional(readOnly = true)
    public OverbookingCapacityResponse capacity(UUID professionalId, OffsetDateTime startAtValue, OffsetDateTime endAtValue) {
        var professional = findActiveProfessional(professionalId);
        var startAt = toInstant(startAtValue);
        var endAt = toInstant(endAtValue);
        validateWindow(startAt, endAt);
        var matchingRule = matchingAvailability(professional, startAt, endAt);
        var blocked = appointmentRepository.existsBlockedSlotOverlap(professional.getId(), startAt, endAt, null);
        var active = appointmentRepository.countActiveOverbookings(professional.getId(), startAt, endAt, ACTIVE_OVERBOOKING_STATUSES);

        if (matchingRule.isEmpty()) {
            return new OverbookingCapacityResponse(professional.getId(), startAt, endAt, false, false, 0, active, 0, blocked, "Horario fuera de disponibilidad");
        }

        var rule = matchingRule.get();
        var remaining = Math.max(0, rule.getMaxOverbookings() - active);
        var message = blocked
            ? "Horario bloqueado para sobrecupos"
            : rule.isAllowsOverbooking() && remaining > 0
                ? "Sobrecupo disponible"
                : "El profesional no cuenta con capacidad de sobrecupo para este horario";

        return new OverbookingCapacityResponse(
            professional.getId(),
            startAt,
            endAt,
            true,
            rule.isAllowsOverbooking(),
            rule.getMaxOverbookings(),
            active,
            remaining,
            blocked,
            message
        );
    }

    @Transactional
    public OverbookingResponse create(OverbookingCreateRequest request) {
        var professional = findActiveProfessional(request.professionalId());
        var patient = findActivePatient(request.patientId());
        validateSameOrganization(professional, patient);

        var startAt = toInstant(request.startAt());
        var endAt = toInstant(request.endAt());
        validateWindow(startAt, endAt);
        var appointmentType = resolveAppointmentType(request.appointmentType());
        var reason = requireReason(request.reason());

        var capacity = capacity(professional.getId(), request.startAt(), request.endAt());
        if (!capacity.insideAvailability()) {
            throw new AppointmentValidationException("El horario no pertenece a la disponibilidad del profesional");
        }
        if (capacity.blocked()) {
            throw new AppointmentValidationException("No se puede crear un sobrecupo en un horario bloqueado");
        }
        if (!capacity.allowsOverbooking() || capacity.remainingOverbookings() <= 0) {
            throw new AppointmentValidationException("Se alcanzo el maximo de sobrecupos autorizados para el horario");
        }
        if (appointmentRepository.findOverlappingByProfessionalAndStatuses(professional.getId(), startAt, endAt, NORMAL_OCCUPANCY_STATUSES, null).isEmpty()) {
            throw new AppointmentValidationException("El horario tiene cupo disponible; use una reserva normal");
        }

        var appointment = new Appointment();
        appointment.setOrganizationId(professional.getOrganizationId());
        appointment.setProfessionalId(professional.getId());
        appointment.setPatientId(patient.getId());
        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);
        appointment.setAppointmentType(appointmentType);
        appointment.setReason(reason);
        appointment.setStatus("scheduled");
        appointment.setOverbooking(true);
        var savedAppointment = appointmentRepository.save(appointment);

        var overbooking = new Overbooking();
        overbooking.setOrganizationId(professional.getOrganizationId());
        overbooking.setAppointmentId(savedAppointment.getId());
        overbooking.setProfessionalId(professional.getId());
        overbooking.setPatientId(patient.getId());
        overbooking.setReason(reason);
        overbooking.setStatus("approved");
        var savedOverbooking = overbookingRepository.save(overbooking);

        return toResponse(savedOverbooking, savedAppointment);
    }

    @Transactional(readOnly = true)
    public List<OverbookingResponse> list(UUID professionalId, OffsetDateTime from, OffsetDateTime to) {
        if (from == null || to == null || !from.toInstant().isBefore(to.toInstant())) {
            throw new AppointmentValidationException("Debe indicar un rango de fechas valido");
        }

        findActiveProfessional(professionalId);
        return overbookingRepository.findActiveByProfessionalAndRange(professionalId, from.toInstant(), to.toInstant())
            .stream()
            .map(overbooking -> toResponse(overbooking, findAppointment(overbooking.getAppointmentId())))
            .toList();
    }

    private Optional<ProfessionalAvailability> matchingAvailability(Professional professional, Instant startAt, Instant endAt) {
        var date = ZonedDateTime.ofInstant(startAt, AGENDA_ZONE).toLocalDate();
        var endDate = ZonedDateTime.ofInstant(endAt, AGENDA_ZONE).toLocalDate();
        if (!date.equals(endDate)) {
            return Optional.empty();
        }

        return activeAvailabilityFor(professional, date).stream()
            .filter(rule -> isInsideRule(date, rule, startAt, endAt))
            .findFirst();
    }

    private List<ProfessionalAvailability> activeAvailabilityFor(Professional professional, LocalDate date) {
        var weekday = (short) date.getDayOfWeek().getValue();
        return availabilityRepository.findByProfessionalIdAndStatusOrderByWeekdayAscStartTimeAsc(professional.getId(), "active")
            .stream()
            .filter(rule -> rule.getWeekday() == weekday)
            .filter(rule -> rule.getValidFrom() == null || !date.isBefore(rule.getValidFrom()))
            .filter(rule -> rule.getValidTo() == null || !date.isAfter(rule.getValidTo()))
            .sorted(Comparator.comparing(ProfessionalAvailability::getStartTime))
            .toList();
    }

    private boolean isInsideRule(LocalDate date, ProfessionalAvailability rule, Instant startAt, Instant endAt) {
        var ruleStart = ZonedDateTime.of(date, rule.getStartTime(), AGENDA_ZONE).toInstant();
        var ruleEnd = ZonedDateTime.of(date, rule.getEndTime(), AGENDA_ZONE).toInstant();
        return !startAt.isBefore(ruleStart) && !endAt.isAfter(ruleEnd);
    }

    private Appointment findAppointment(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new AppointmentValidationException("La cita asociada al sobrecupo no existe"));
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

    private void validateSameOrganization(Professional professional, Patient patient) {
        if (!professional.getOrganizationId().equals(patient.getOrganizationId())) {
            throw new AppointmentValidationException("Profesional y paciente deben pertenecer a la misma organizacion");
        }
    }

    private void validateWindow(Instant startAt, Instant endAt) {
        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) {
            throw new AppointmentValidationException("El horario del sobrecupo no es valido");
        }
    }

    private Instant toInstant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    private String resolveAppointmentType(String value) {
        var cleaned = StringUtils.hasText(value) ? value.trim() : "consultation";
        if (!APPOINTMENT_TYPES.contains(cleaned)) {
            throw new AppointmentValidationException("Tipo de cita permitido: consultation, control o procedure");
        }
        return cleaned;
    }

    private String requireReason(String value) {
        var cleaned = StringUtils.hasText(value) ? value.trim() : null;
        if (!StringUtils.hasText(cleaned)) {
            throw new AppointmentValidationException("Debe indicar el motivo del sobrecupo");
        }
        return cleaned;
    }

    private OverbookingResponse toResponse(Overbooking overbooking, Appointment appointment) {
        return new OverbookingResponse(
            overbooking.getId(),
            overbooking.getOrganizationId(),
            overbooking.getAppointmentId(),
            overbooking.getProfessionalId(),
            professionalName(overbooking.getProfessionalId()),
            overbooking.getPatientId(),
            patientName(overbooking.getPatientId()),
            appointment.getStartAt(),
            appointment.getEndAt(),
            appointment.getAppointmentType(),
            appointment.getStatus(),
            overbooking.getReason(),
            overbooking.getStatus(),
            overbooking.getCreatedAt(),
            overbooking.getUpdatedAt()
        );
    }

    private String professionalName(UUID professionalId) {
        return professionalRepository.findById(professionalId)
            .map(professional -> professional.getFirstName() + " " + professional.getLastName())
            .orElse("Profesional no disponible");
    }

    private String patientName(UUID patientId) {
        return patientRepository.findById(patientId)
            .map(patient -> patient.getFirstName() + " " + patient.getLastName())
            .orElse("Paciente no disponible");
    }
}
