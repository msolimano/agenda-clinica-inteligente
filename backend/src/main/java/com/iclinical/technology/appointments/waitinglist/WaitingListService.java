package com.iclinical.technology.appointments.waitinglist;

import com.iclinical.technology.appointments.AppointmentService;
import com.iclinical.technology.appointments.dto.AppointmentCreateRequest;
import com.iclinical.technology.appointments.waitinglist.dto.WaitingListCreateRequest;
import com.iclinical.technology.appointments.waitinglist.dto.WaitingListRecommendationResponse;
import com.iclinical.technology.appointments.waitinglist.dto.WaitingListResponse;
import com.iclinical.technology.appointments.waitinglist.dto.WaitingListScheduleRequest;
import com.iclinical.technology.appointments.waitinglist.dto.WaitingListStatusRequest;
import com.iclinical.technology.appointments.waitinglist.dto.WaitingListUpdateRequest;
import com.iclinical.technology.patients.Patient;
import com.iclinical.technology.patients.PatientRepository;
import com.iclinical.technology.professionals.Professional;
import com.iclinical.technology.professionals.ProfessionalRepository;
import com.iclinical.technology.professionals.ProfessionalSpecialtyRepository;
import com.iclinical.technology.specialties.Specialty;
import com.iclinical.technology.specialties.SpecialtyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class WaitingListService {

    private static final Set<String> ACTIVE_RECOMMENDATION_STATUSES = Set.of("waiting", "contacted");
    private static final Set<String> ALLOWED_STATUSES = Set.of("waiting", "contacted", "scheduled", "cancelled");

    private final WaitingListRepository waitingListRepository;
    private final PatientRepository patientRepository;
    private final SpecialtyRepository specialtyRepository;
    private final ProfessionalRepository professionalRepository;
    private final ProfessionalSpecialtyRepository professionalSpecialtyRepository;
    private final AppointmentService appointmentService;

    public WaitingListService(
        WaitingListRepository waitingListRepository,
        PatientRepository patientRepository,
        SpecialtyRepository specialtyRepository,
        ProfessionalRepository professionalRepository,
        ProfessionalSpecialtyRepository professionalSpecialtyRepository,
        AppointmentService appointmentService
    ) {
        this.waitingListRepository = waitingListRepository;
        this.patientRepository = patientRepository;
        this.specialtyRepository = specialtyRepository;
        this.professionalRepository = professionalRepository;
        this.professionalSpecialtyRepository = professionalSpecialtyRepository;
        this.appointmentService = appointmentService;
    }

    @Transactional(readOnly = true)
    public List<WaitingListResponse> list(UUID specialtyId, UUID professionalId, String status) {
        var normalizedStatus = clean(status);
        return waitingListRepository.findByStatusNotOrderByPriorityAscCreatedAtAsc("deleted")
            .stream()
            .filter(entry -> specialtyId == null || specialtyId.equals(entry.getSpecialtyId()))
            .filter(entry -> professionalId == null || professionalId.equals(entry.getProfessionalId()))
            .filter(entry -> normalizedStatus == null || normalizedStatus.equals(entry.getStatus()))
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public WaitingListResponse getById(UUID id) {
        return toResponse(findEntry(id));
    }

    @Transactional
    public WaitingListResponse create(WaitingListCreateRequest request) {
        var patient = findActivePatient(request.patientId());
        var specialty = findActiveSpecialty(request.specialtyId());
        var professional = request.professionalId() == null ? null : findActiveProfessional(request.professionalId());
        validateOrganization(patient, specialty, professional);
        validateProfessionalSpecialty(professional, specialty);
        validateDates(request.requestedFrom(), request.requestedTo());

        var entry = new WaitingListEntry();
        entry.setOrganizationId(patient.getOrganizationId());
        entry.setPatientId(patient.getId());
        entry.setSpecialtyId(specialty.getId());
        entry.setProfessionalId(professional == null ? null : professional.getId());
        entry.setRequestedFrom(request.requestedFrom());
        entry.setRequestedTo(request.requestedTo());
        entry.setPriority(resolvePriority(request.priority()));
        entry.setAvailabilityNotes(clean(request.availabilityNotes()));
        entry.setStatus("waiting");

        return toResponse(waitingListRepository.save(entry));
    }

    @Transactional
    public WaitingListResponse update(UUID id, WaitingListUpdateRequest request) {
        var entry = findEntry(id);
        if ("scheduled".equals(entry.getStatus())) {
            throw new WaitingListValidationException("Un registro agendado no puede editarse");
        }

        var patient = findActivePatient(request.patientId());
        var specialty = findActiveSpecialty(request.specialtyId());
        var professional = request.professionalId() == null ? null : findActiveProfessional(request.professionalId());
        validateOrganization(patient, specialty, professional);
        validateProfessionalSpecialty(professional, specialty);
        validateDates(request.requestedFrom(), request.requestedTo());

        entry.setOrganizationId(patient.getOrganizationId());
        entry.setPatientId(patient.getId());
        entry.setSpecialtyId(specialty.getId());
        entry.setProfessionalId(professional == null ? null : professional.getId());
        entry.setRequestedFrom(request.requestedFrom());
        entry.setRequestedTo(request.requestedTo());
        entry.setPriority(resolvePriority(request.priority()));
        entry.setAvailabilityNotes(clean(request.availabilityNotes()));

        return toResponse(entry);
    }

    @Transactional
    public WaitingListResponse updateStatus(UUID id, WaitingListStatusRequest request) {
        var entry = findEntry(id);
        var status = clean(request.status());
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new WaitingListValidationException("Estado permitido: waiting, contacted, scheduled o cancelled");
        }
        if ("scheduled".equals(status) && entry.getScheduledAppointmentId() == null) {
            throw new WaitingListValidationException("Para marcar como scheduled debe agendarse desde la lista de espera");
        }

        entry.setStatus(status);
        if ("contacted".equals(status) && entry.getContactedAt() == null) {
            entry.setContactedAt(Instant.now());
        }
        return toResponse(entry);
    }

    @Transactional(readOnly = true)
    public List<WaitingListRecommendationResponse> recommend(UUID professionalId, UUID specialtyId, OffsetDateTime startAt, OffsetDateTime endAt) {
        if (startAt == null || endAt == null || !startAt.toInstant().isBefore(endAt.toInstant())) {
            throw new WaitingListValidationException("Debe indicar un rango valido para recomendar pacientes");
        }
        if (professionalId == null && specialtyId == null) {
            throw new WaitingListValidationException("Debe indicar profesional o especialidad para recomendar pacientes");
        }

        var professional = professionalId == null ? null : findActiveProfessional(professionalId);
        var specialty = specialtyId == null ? null : findActiveSpecialty(specialtyId);
        var targetDate = startAt.toLocalDate();
        var professionalSpecialtyIds = professional == null ? Set.<UUID>of() : activeSpecialtyIds(professional.getId());

        return waitingListRepository.findByStatusNotOrderByPriorityAscCreatedAtAsc("deleted")
            .stream()
            .filter(entry -> ACTIVE_RECOMMENDATION_STATUSES.contains(entry.getStatus()))
            .filter(entry -> matchesProfessional(entry, professional))
            .filter(entry -> matchesSpecialty(entry, specialty, professionalSpecialtyIds))
            .filter(entry -> matchesRequestedDate(entry, targetDate))
            .map(entry -> toRecommendation(entry, professional, specialty, targetDate))
            .sorted(Comparator.comparing(WaitingListRecommendationResponse::score).reversed()
                .thenComparing(WaitingListRecommendationResponse::priority)
                .thenComparing(WaitingListRecommendationResponse::waitingListId))
            .toList();
    }

    @Transactional
    public WaitingListResponse schedule(UUID id, WaitingListScheduleRequest request) {
        var entry = findEntry(id);
        if (!ACTIVE_RECOMMENDATION_STATUSES.contains(entry.getStatus())) {
            throw new WaitingListValidationException("Solo se pueden agendar registros waiting o contacted");
        }

        var patient = findActivePatient(entry.getPatientId());
        var specialty = findActiveSpecialty(entry.getSpecialtyId());
        var professionalId = resolveScheduleProfessional(entry, request.professionalId());
        var professional = findActiveProfessional(professionalId);
        validateOrganization(patient, specialty, professional);
        validateProfessionalSpecialty(professional, specialty);

        var appointment = appointmentService.create(new AppointmentCreateRequest(
            professional.getId(),
            patient.getId(),
            request.startAt(),
            request.endAt(),
            clean(request.appointmentType()),
            StringUtils.hasText(request.reason()) ? request.reason().trim() : entry.getAvailabilityNotes()
        ));

        entry.setStatus("scheduled");
        entry.setScheduledAppointmentId(appointment.id());
        return toResponse(entry);
    }

    private WaitingListEntry findEntry(UUID id) {
        return waitingListRepository.findById(id)
            .filter(entry -> !"deleted".equals(entry.getStatus()))
            .orElseThrow(() -> new WaitingListNotFoundException("Registro de lista de espera no encontrado"));
    }

    private Patient findActivePatient(UUID patientId) {
        return patientRepository.findById(patientId)
            .filter(patient -> "active".equals(patient.getStatus()))
            .orElseThrow(() -> new WaitingListValidationException("El paciente indicado no existe o no esta activo"));
    }

    private Specialty findActiveSpecialty(UUID specialtyId) {
        return specialtyRepository.findById(specialtyId)
            .filter(specialty -> "active".equals(specialty.getStatus()))
            .orElseThrow(() -> new WaitingListValidationException("La especialidad indicada no existe o no esta activa"));
    }

    private Professional findActiveProfessional(UUID professionalId) {
        return professionalRepository.findById(professionalId)
            .filter(professional -> "active".equals(professional.getStatus()))
            .orElseThrow(() -> new WaitingListValidationException("El profesional indicado no existe o no esta activo"));
    }

    private void validateOrganization(Patient patient, Specialty specialty, Professional professional) {
        if (specialty.getOrganizationId() != null && !patient.getOrganizationId().equals(specialty.getOrganizationId())) {
            throw new WaitingListValidationException("Paciente y especialidad deben pertenecer a la misma organizacion");
        }
        if (professional != null && !patient.getOrganizationId().equals(professional.getOrganizationId())) {
            throw new WaitingListValidationException("Paciente y profesional deben pertenecer a la misma organizacion");
        }
    }

    private void validateProfessionalSpecialty(Professional professional, Specialty specialty) {
        if (professional == null) {
            return;
        }

        var matches = professionalSpecialtyRepository.findByProfessionalIdAndSpecialtyId(professional.getId(), specialty.getId())
            .filter(assignment -> "active".equals(assignment.getStatus()))
            .isPresent();
        if (!matches) {
            throw new WaitingListValidationException("El profesional no posee la especialidad indicada");
        }
    }

    private UUID resolveScheduleProfessional(WaitingListEntry entry, UUID requestedProfessionalId) {
        if (entry.getProfessionalId() != null && requestedProfessionalId != null && !entry.getProfessionalId().equals(requestedProfessionalId)) {
            throw new WaitingListValidationException("El registro esta asociado a otro profesional");
        }
        if (entry.getProfessionalId() != null) {
            return entry.getProfessionalId();
        }
        if (requestedProfessionalId == null) {
            throw new WaitingListValidationException("Debe indicar un profesional para agendar desde lista de espera");
        }
        return requestedProfessionalId;
    }

    private boolean matchesProfessional(WaitingListEntry entry, Professional professional) {
        if (professional == null) {
            return true;
        }
        return entry.getProfessionalId() == null || professional.getId().equals(entry.getProfessionalId());
    }

    private boolean matchesSpecialty(WaitingListEntry entry, Specialty specialty, Set<UUID> professionalSpecialtyIds) {
        if (specialty != null) {
            return specialty.getId().equals(entry.getSpecialtyId());
        }
        return professionalSpecialtyIds.isEmpty() || professionalSpecialtyIds.contains(entry.getSpecialtyId());
    }

    private boolean matchesRequestedDate(WaitingListEntry entry, LocalDate targetDate) {
        return (entry.getRequestedFrom() == null || !targetDate.isBefore(entry.getRequestedFrom()))
            && (entry.getRequestedTo() == null || !targetDate.isAfter(entry.getRequestedTo()));
    }

    private WaitingListRecommendationResponse toRecommendation(WaitingListEntry entry, Professional professional, Specialty specialty, LocalDate targetDate) {
        var resolvedSpecialty = specialty == null || !specialty.getId().equals(entry.getSpecialtyId())
            ? specialtyRepository.findById(entry.getSpecialtyId()).orElse(null)
            : specialty;
        var score = recommendationScore(entry, professional, targetDate);
        return new WaitingListRecommendationResponse(
            entry.getId(),
            entry.getPatientId(),
            patientName(entry.getPatientId()),
            entry.getSpecialtyId(),
            resolvedSpecialty == null ? "Especialidad no disponible" : resolvedSpecialty.getName(),
            entry.getProfessionalId(),
            professionalName(entry.getProfessionalId()),
            entry.getRequestedFrom(),
            entry.getRequestedTo(),
            entry.getPriority(),
            entry.getStatus(),
            score,
            recommendationReason(entry, professional, targetDate)
        );
    }

    private int recommendationScore(WaitingListEntry entry, Professional professional, LocalDate targetDate) {
        var score = 0;
        if (professional != null && professional.getId().equals(entry.getProfessionalId())) {
            score += 40;
        } else if (entry.getProfessionalId() == null) {
            score += 20;
        }
        if (matchesRequestedDate(entry, targetDate)) {
            score += 20;
        }
        score += (6 - entry.getPriority()) * 8;
        if ("waiting".equals(entry.getStatus())) {
            score += 6;
        }
        if (entry.getCreatedAt() != null) {
            score += (int) Math.min(30, Math.max(0, ChronoUnit.DAYS.between(entry.getCreatedAt(), Instant.now())));
        }
        return score;
    }

    private String recommendationReason(WaitingListEntry entry, Professional professional, LocalDate targetDate) {
        var parts = new java.util.ArrayList<String>();
        parts.add("Prioridad " + entry.getPriority());
        if (professional != null && professional.getId().equals(entry.getProfessionalId())) {
            parts.add("coincide con profesional solicitado");
        } else if (entry.getProfessionalId() == null) {
            parts.add("sin profesional preferente");
        }
        if (matchesRequestedDate(entry, targetDate)) {
            parts.add("fecha compatible");
        }
        if ("waiting".equals(entry.getStatus())) {
            parts.add("pendiente de contacto");
        }
        return String.join(", ", parts);
    }

    private Set<UUID> activeSpecialtyIds(UUID professionalId) {
        return professionalSpecialtyRepository.findByProfessionalIdAndStatus(professionalId, "active")
            .stream()
            .map(assignment -> assignment.getSpecialty().getId())
            .collect(java.util.stream.Collectors.toSet());
    }

    private void validateDates(LocalDate requestedFrom, LocalDate requestedTo) {
        if (requestedFrom != null && requestedTo != null && requestedFrom.isAfter(requestedTo)) {
            throw new WaitingListValidationException("El rango de fechas deseadas no es valido");
        }
    }

    private int resolvePriority(Integer priority) {
        if (priority == null) {
            return 3;
        }
        if (priority < 1 || priority > 5) {
            throw new WaitingListValidationException("La prioridad debe estar entre 1 y 5");
        }
        return priority;
    }

    private WaitingListResponse toResponse(WaitingListEntry entry) {
        return new WaitingListResponse(
            entry.getId(),
            entry.getOrganizationId(),
            entry.getPatientId(),
            patientName(entry.getPatientId()),
            entry.getSpecialtyId(),
            specialtyName(entry.getSpecialtyId()),
            entry.getProfessionalId(),
            professionalName(entry.getProfessionalId()),
            entry.getRequestedFrom(),
            entry.getRequestedTo(),
            entry.getPriority(),
            entry.getAvailabilityNotes(),
            entry.getStatus(),
            entry.getScheduledAppointmentId(),
            entry.getContactedAt(),
            entry.getCreatedAt(),
            entry.getUpdatedAt()
        );
    }

    private String patientName(UUID patientId) {
        return patientRepository.findById(patientId)
            .map(patient -> patient.getFirstName() + " " + patient.getLastName())
            .orElse("Paciente no disponible");
    }

    private String specialtyName(UUID specialtyId) {
        return specialtyRepository.findById(specialtyId)
            .map(Specialty::getName)
            .orElse("Especialidad no disponible");
    }

    private String professionalName(UUID professionalId) {
        if (professionalId == null) {
            return null;
        }
        return professionalRepository.findById(professionalId)
            .map(professional -> professional.getFirstName() + " " + professional.getLastName())
            .orElse("Profesional no disponible");
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
