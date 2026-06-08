package com.iclinical.technology.appointments.blocks;

import com.iclinical.technology.appointments.Appointment;
import com.iclinical.technology.appointments.AppointmentNotFoundException;
import com.iclinical.technology.appointments.AppointmentRepository;
import com.iclinical.technology.appointments.AppointmentValidationException;
import com.iclinical.technology.appointments.ProfessionalAvailability;
import com.iclinical.technology.appointments.ProfessionalAvailabilityRepository;
import com.iclinical.technology.appointments.blocks.dto.AffectedAppointmentResponse;
import com.iclinical.technology.appointments.blocks.dto.AgendaBlockCancelRequest;
import com.iclinical.technology.appointments.blocks.dto.AgendaBlockCreateRequest;
import com.iclinical.technology.appointments.blocks.dto.AgendaBlockPreviewRequest;
import com.iclinical.technology.appointments.blocks.dto.AgendaBlockPreviewResponse;
import com.iclinical.technology.appointments.blocks.dto.AgendaBlockResponse;
import com.iclinical.technology.appointments.blocks.dto.RescheduleSuggestionResponse;
import com.iclinical.technology.appointments.blocks.dto.SuggestedSlotResponse;
import com.iclinical.technology.patients.PatientRepository;
import com.iclinical.technology.professionals.Professional;
import com.iclinical.technology.professionals.ProfessionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AgendaBlockService {

    private static final Set<String> AFFECTED_STATUSES = Set.of("scheduled", "confirmed");
    private static final Set<String> BLOCKING_STATUSES = Set.of("scheduled", "confirmed", "completed", "blocked");
    private static final ZoneId AGENDA_ZONE = ZoneId.of("America/Santiago");
    private static final int SUGGESTION_DAYS = 14;
    private static final int SUGGESTION_LIMIT = 3;

    private final AppointmentRepository appointmentRepository;
    private final ProfessionalAvailabilityRepository availabilityRepository;
    private final ProfessionalRepository professionalRepository;
    private final PatientRepository patientRepository;

    public AgendaBlockService(
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
    public AgendaBlockPreviewResponse preview(AgendaBlockPreviewRequest request) {
        var professional = findActiveProfessional(request.professionalId());
        var startAt = toInstant(request.startAt());
        var endAt = toInstant(request.endAt());
        validateBlockWindow(startAt, endAt);
        var reason = requireReason(request.reason());

        var affected = affectedAppointments(professional.getId(), startAt, endAt);
        var suggestions = rescheduleSuggestions(professional, startAt, endAt, affected);

        return new AgendaBlockPreviewResponse(
            professional.getId(),
            startAt,
            endAt,
            reason,
            !affected.isEmpty(),
            affected.stream().map(this::toAffectedResponse).toList(),
            suggestions
        );
    }

    @Transactional
    public AgendaBlockResponse create(AgendaBlockCreateRequest request) {
        var professional = findActiveProfessional(request.professionalId());
        var startAt = toInstant(request.startAt());
        var endAt = toInstant(request.endAt());
        validateBlockWindow(startAt, endAt);
        var reason = requireReason(request.reason());

        if (appointmentRepository.existsBlockedSlotOverlap(professional.getId(), startAt, endAt, null)) {
            throw new AppointmentValidationException("Ya existe un bloqueo activo en el rango indicado");
        }

        var affected = affectedAppointments(professional.getId(), startAt, endAt);
        if (!affected.isEmpty() && !request.confirmAffectedAppointments()) {
            throw new AppointmentValidationException("El bloqueo afecta citas existentes; confirme explicitamente para crearlo");
        }

        var block = new Appointment();
        block.setOrganizationId(professional.getOrganizationId());
        block.setProfessionalId(professional.getId());
        block.setPatientId(null);
        block.setStartAt(startAt);
        block.setEndAt(endAt);
        block.setAppointmentType("blocked_slot");
        block.setStatus("blocked");
        block.setReason(reason);

        var saved = appointmentRepository.save(block);
        return toBlockResponse(saved, affected, rescheduleSuggestions(professional, startAt, endAt, affected));
    }

    @Transactional(readOnly = true)
    public List<AgendaBlockResponse> list(UUID professionalId, OffsetDateTime from, OffsetDateTime to) {
        if (from == null || to == null || !from.toInstant().isBefore(to.toInstant())) {
            throw new AppointmentValidationException("Debe indicar un rango de fechas valido");
        }

        var professional = findActiveProfessional(professionalId);
        return appointmentRepository.findActiveBlocks(professional.getId(), from.toInstant(), to.toInstant())
            .stream()
            .map(block -> {
                var affected = affectedAppointments(block);
                return toBlockResponse(block, affected, rescheduleSuggestions(professional, block.getStartAt(), block.getEndAt(), affected));
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AffectedAppointmentResponse> affected(UUID blockId) {
        return affectedAppointments(findBlock(blockId)).stream()
            .map(this::toAffectedResponse)
            .toList();
    }

    @Transactional
    public AgendaBlockResponse cancel(UUID blockId, AgendaBlockCancelRequest request) {
        var block = findBlock(blockId);
        if ("cancelled".equals(block.getStatus())) {
            return toBlockResponse(block, List.of(), List.of());
        }

        block.setStatus("cancelled");
        block.setCancellationReason(clean(request == null ? null : request.cancellationReason()));
        block.setCancelledAt(Instant.now());
        return toBlockResponse(block, List.of(), List.of());
    }

    private Appointment findBlock(UUID blockId) {
        return appointmentRepository.findById(blockId)
            .filter(appointment -> "blocked_slot".equals(appointment.getAppointmentType()))
            .filter(appointment -> !"deleted".equals(appointment.getStatus()))
            .orElseThrow(() -> new AppointmentNotFoundException("Bloqueo de agenda no encontrado"));
    }

    private List<Appointment> affectedAppointments(Appointment block) {
        return affectedAppointments(block.getProfessionalId(), block.getStartAt(), block.getEndAt());
    }

    private List<Appointment> affectedAppointments(UUID professionalId, Instant startAt, Instant endAt) {
        return appointmentRepository.findOverlappingByProfessionalAndStatuses(professionalId, startAt, endAt, AFFECTED_STATUSES, null);
    }

    private List<RescheduleSuggestionResponse> rescheduleSuggestions(Professional professional, Instant blockStart, Instant blockEnd, Collection<Appointment> affected) {
        return affected.stream()
            .map(appointment -> new RescheduleSuggestionResponse(
                appointment.getId(),
                appointment.getPatientId(),
                patientName(appointment.getPatientId()),
                suggestedSlots(professional, appointment, blockStart, blockEnd)
            ))
            .toList();
    }

    private List<SuggestedSlotResponse> suggestedSlots(Professional professional, Appointment appointment, Instant blockStart, Instant blockEnd) {
        var duration = Duration.between(appointment.getStartAt(), appointment.getEndAt());
        var firstDate = ZonedDateTime.ofInstant(blockEnd, AGENDA_ZONE).toLocalDate();
        var suggestions = new java.util.ArrayList<SuggestedSlotResponse>();

        for (var dayOffset = 0; dayOffset <= SUGGESTION_DAYS && suggestions.size() < SUGGESTION_LIMIT; dayOffset++) {
            var date = firstDate.plusDays(dayOffset);
            var rules = activeAvailabilityFor(professional, date);
            for (var rule : rules) {
                var cursor = rule.getStartTime();
                while (!cursor.plus(duration).isAfter(rule.getEndTime())) {
                    var start = ZonedDateTime.of(date, cursor, AGENDA_ZONE).toInstant();
                    var end = start.plus(duration);
                    if (!overlaps(start, end, blockStart, blockEnd)
                        && !appointmentRepository.existsBlockingOverlap(professional.getId(), start, end, BLOCKING_STATUSES, appointment.getId())) {
                        suggestions.add(new SuggestedSlotResponse(start, end));
                        if (suggestions.size() == SUGGESTION_LIMIT) {
                            break;
                        }
                    }
                    cursor = cursor.plusMinutes(rule.getSlotMinutes());
                }
                if (suggestions.size() == SUGGESTION_LIMIT) {
                    break;
                }
            }
        }

        return suggestions;
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

    private AgendaBlockResponse toBlockResponse(Appointment block, List<Appointment> affected, List<RescheduleSuggestionResponse> suggestions) {
        return new AgendaBlockResponse(
            block.getId(),
            block.getOrganizationId(),
            block.getProfessionalId(),
            professionalName(block.getProfessionalId()),
            block.getStartAt(),
            block.getEndAt(),
            block.getAppointmentType(),
            block.getStatus(),
            block.getReason(),
            block.getCancellationReason(),
            block.getCancelledAt(),
            block.getCreatedAt(),
            block.getUpdatedAt(),
            affected.stream().map(this::toAffectedResponse).toList(),
            suggestions
        );
    }

    private AffectedAppointmentResponse toAffectedResponse(Appointment appointment) {
        return new AffectedAppointmentResponse(
            appointment.getId(),
            appointment.getPatientId(),
            patientName(appointment.getPatientId()),
            appointment.getStartAt(),
            appointment.getEndAt(),
            appointment.getAppointmentType(),
            appointment.getStatus(),
            appointment.getReason()
        );
    }

    private void validateBlockWindow(Instant startAt, Instant endAt) {
        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) {
            throw new AppointmentValidationException("El rango del bloqueo no es valido");
        }
    }

    private Professional findActiveProfessional(UUID professionalId) {
        return professionalRepository.findById(professionalId)
            .filter(professional -> "active".equals(professional.getStatus()))
            .orElseThrow(() -> new AppointmentValidationException("El profesional indicado no existe o no esta activo"));
    }

    private boolean overlaps(Instant firstStart, Instant firstEnd, Instant secondStart, Instant secondEnd) {
        return firstStart.isBefore(secondEnd) && firstEnd.isAfter(secondStart);
    }

    private Instant toInstant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    private String requireReason(String value) {
        var cleaned = clean(value);
        if (!StringUtils.hasText(cleaned)) {
            throw new AppointmentValidationException("Debe indicar el motivo del bloqueo");
        }
        return cleaned;
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
