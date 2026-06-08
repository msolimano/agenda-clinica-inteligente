package com.iclinical.technology.clinicalrecords;

import com.iclinical.technology.appointments.Appointment;
import com.iclinical.technology.appointments.AppointmentRepository;
import com.iclinical.technology.clinicalrecords.dto.ClinicalRecordCreateRequest;
import com.iclinical.technology.clinicalrecords.dto.ClinicalRecordResponse;
import com.iclinical.technology.clinicalrecords.dto.ClinicalRecordStatusRequest;
import com.iclinical.technology.clinicalrecords.dto.ClinicalRecordSummaryResponse;
import com.iclinical.technology.clinicalrecords.dto.ClinicalRecordUpdateRequest;
import com.iclinical.technology.patients.Patient;
import com.iclinical.technology.patients.PatientRepository;
import com.iclinical.technology.professionals.Professional;
import com.iclinical.technology.professionals.ProfessionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ClinicalRecordService {

    private static final Set<String> UI_STATUSES = Set.of("draft", "open", "closed");

    private final ClinicalRecordRepository recordRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final AppointmentRepository appointmentRepository;

    public ClinicalRecordService(
        ClinicalRecordRepository recordRepository,
        PatientRepository patientRepository,
        ProfessionalRepository professionalRepository,
        AppointmentRepository appointmentRepository
    ) {
        this.recordRepository = recordRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public List<ClinicalRecordSummaryResponse> list(UUID patientId, UUID professionalId, String status, Instant from, Instant to) {
        var resolvedStatus = resolveOptionalStatus(status);
        if (from != null && to != null && from.isAfter(to)) {
            throw new ClinicalRecordValidationException("Rango de fechas invalido");
        }
        return recordRepository.findFiltered(patientId, professionalId, resolvedStatus, from, to)
            .stream()
            .map(this::toSummary)
            .toList();
    }

    @Transactional(readOnly = true)
    public ClinicalRecordResponse getById(UUID id) {
        return toResponse(findRecord(id));
    }

    @Transactional(readOnly = true)
    public List<ClinicalRecordSummaryResponse> listByPatient(UUID patientId) {
        findPatient(patientId);
        return recordRepository.findByPatientIdAndStatusNotOrderByRecordDateDescCreatedAtDesc(patientId, "deleted")
            .stream()
            .map(this::toSummary)
            .toList();
    }

    @Transactional
    public ClinicalRecordResponse create(ClinicalRecordCreateRequest request) {
        var patient = findPatient(request == null ? null : request.patientId());
        var professional = findActiveProfessional(request == null ? null : request.professionalId());
        validateSameOrganization(patient, professional);
        var appointment = resolveAppointment(request == null ? null : request.appointmentId(), patient, professional, null);

        var record = new ClinicalRecord();
        record.setOrganizationId(patient.getOrganizationId());
        applyEditableFields(record, patient, professional, appointment, request == null ? null : request.recordDate(), request == null ? null : request.chiefComplaint(), request == null ? null : request.anamnesis(), request == null ? null : request.physicalExam(), request == null ? null : request.assessment(), request == null ? null : request.plan(), request == null ? null : request.notes());
        applyStatus(record, resolveStatus(request == null ? null : request.status(), "draft"));

        return toResponse(recordRepository.save(record));
    }

    @Transactional
    public ClinicalRecordResponse update(UUID id, ClinicalRecordUpdateRequest request) {
        var record = findRecord(id);
        if ("closed".equals(record.getStatus())) {
            throw new ClinicalRecordValidationException("No se permite editar una ficha clinica cerrada");
        }

        var patient = findPatient(request == null ? null : request.patientId());
        var professional = findActiveProfessional(request == null ? null : request.professionalId());
        validateSameOrganization(patient, professional);
        var appointment = resolveAppointment(request == null ? null : request.appointmentId(), patient, professional, record.getId());

        applyEditableFields(record, patient, professional, appointment, request == null ? null : request.recordDate(), request == null ? null : request.chiefComplaint(), request == null ? null : request.anamnesis(), request == null ? null : request.physicalExam(), request == null ? null : request.assessment(), request == null ? null : request.plan(), request == null ? null : request.notes());
        if (request != null && StringUtils.hasText(request.status())) {
            applyStatus(record, resolveStatus(request.status(), null));
        }
        return toResponse(record);
    }

    @Transactional
    public ClinicalRecordResponse updateStatus(UUID id, ClinicalRecordStatusRequest request) {
        var record = findRecord(id);
        var status = resolveStatus(request == null ? null : request.status(), null);
        if ("closed".equals(record.getStatus()) && !"closed".equals(status)) {
            throw new ClinicalRecordValidationException("No se permite reapertura de ficha clinica en Sprint 7A");
        }
        applyStatus(record, status);
        return toResponse(record);
    }

    private void applyEditableFields(
        ClinicalRecord record,
        Patient patient,
        Professional professional,
        Appointment appointment,
        Instant recordDate,
        String chiefComplaint,
        String anamnesis,
        String physicalExam,
        String assessment,
        String plan,
        String notes
    ) {
        record.setOrganizationId(patient.getOrganizationId());
        record.setPatientId(patient.getId());
        record.setProfessionalId(professional.getId());
        record.setAppointmentId(appointment == null ? null : appointment.getId());
        record.setRecordDate(recordDate == null ? Instant.now() : recordDate);
        record.setChiefComplaint(clean(chiefComplaint));
        record.setAnamnesis(clean(anamnesis));
        record.setPhysicalExam(clean(physicalExam));
        record.setAssessment(clean(assessment));
        record.setPlan(clean(plan));
        record.setNotes(clean(notes));
    }

    private Appointment resolveAppointment(UUID appointmentId, Patient patient, Professional professional, UUID excludedRecordId) {
        if (appointmentId == null) {
            return null;
        }
        var appointment = appointmentRepository.findById(appointmentId)
            .filter(item -> !"deleted".equals(item.getStatus()))
            .orElseThrow(() -> new ClinicalRecordValidationException("La cita indicada no existe o fue eliminada"));
        if (!appointment.getOrganizationId().equals(patient.getOrganizationId())) {
            throw new ClinicalRecordValidationException("La cita debe pertenecer a la misma organizacion del paciente");
        }
        if (!patient.getId().equals(appointment.getPatientId())) {
            throw new ClinicalRecordValidationException("La cita indicada no corresponde al paciente");
        }
        if (!professional.getId().equals(appointment.getProfessionalId())) {
            throw new ClinicalRecordValidationException("La cita indicada no corresponde al profesional");
        }
        if (recordRepository.existsActiveForAppointment(appointment.getId(), excludedRecordId)) {
            throw new ClinicalRecordValidationException("La cita ya tiene una ficha clinica asociada");
        }
        return appointment;
    }

    private Patient findPatient(UUID patientId) {
        if (patientId == null) {
            throw new ClinicalRecordValidationException("Debe indicar el paciente");
        }
        return patientRepository.findById(patientId)
            .filter(patient -> !"deleted".equals(patient.getStatus()))
            .orElseThrow(() -> new ClinicalRecordValidationException("Paciente no encontrado o eliminado"));
    }

    private Professional findActiveProfessional(UUID professionalId) {
        if (professionalId == null) {
            throw new ClinicalRecordValidationException("Debe indicar el profesional");
        }
        return professionalRepository.findById(professionalId)
            .filter(professional -> "active".equals(professional.getStatus()))
            .orElseThrow(() -> new ClinicalRecordValidationException("Profesional no encontrado o inactivo"));
    }

    private ClinicalRecord findRecord(UUID id) {
        return recordRepository.findById(id)
            .filter(record -> !"deleted".equals(record.getStatus()))
            .orElseThrow(() -> new ClinicalRecordNotFoundException("Ficha clinica no encontrada"));
    }

    private void validateSameOrganization(Patient patient, Professional professional) {
        if (!patient.getOrganizationId().equals(professional.getOrganizationId())) {
            throw new ClinicalRecordValidationException("Paciente y profesional deben pertenecer a la misma organizacion");
        }
    }

    private void applyStatus(ClinicalRecord record, String status) {
        record.setStatus(status);
        if ("closed".equals(status) && record.getFinalizedAt() == null) {
            record.setFinalizedAt(Instant.now());
        }
    }

    private String resolveOptionalStatus(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return resolveStatus(value, null);
    }

    private String resolveStatus(String value, String fallback) {
        var status = clean(value);
        if (!StringUtils.hasText(status)) {
            if (fallback != null) {
                return fallback;
            }
            throw new ClinicalRecordValidationException("Debe indicar un estado de ficha clinica");
        }
        if (!UI_STATUSES.contains(status)) {
            throw new ClinicalRecordValidationException("Estado permitido: draft, open o closed");
        }
        return status;
    }

    private ClinicalRecordResponse toResponse(ClinicalRecord record) {
        return new ClinicalRecordResponse(
            record.getId(),
            record.getOrganizationId(),
            record.getPatientId(),
            patientName(record.getPatientId()),
            record.getProfessionalId(),
            professionalName(record.getProfessionalId()),
            record.getAppointmentId(),
            record.getRecordDate(),
            record.getChiefComplaint(),
            record.getAnamnesis(),
            record.getPhysicalExam(),
            record.getAssessment(),
            record.getPlan(),
            record.getNotes(),
            record.getStatus(),
            record.getFinalizedAt(),
            record.getCreatedAt(),
            record.getUpdatedAt()
        );
    }

    private ClinicalRecordSummaryResponse toSummary(ClinicalRecord record) {
        return new ClinicalRecordSummaryResponse(
            record.getId(),
            record.getPatientId(),
            patientName(record.getPatientId()),
            record.getProfessionalId(),
            professionalName(record.getProfessionalId()),
            record.getAppointmentId(),
            record.getRecordDate(),
            record.getChiefComplaint(),
            record.getAssessment(),
            record.getStatus(),
            record.getCreatedAt(),
            record.getUpdatedAt()
        );
    }

    private String patientName(UUID patientId) {
        return patientRepository.findById(patientId)
            .map(patient -> patient.getFirstName() + " " + patient.getLastName())
            .orElse("Paciente no disponible");
    }

    private String professionalName(UUID professionalId) {
        return professionalRepository.findById(professionalId)
            .map(professional -> professional.getFirstName() + " " + professional.getLastName())
            .orElse("Profesional no disponible");
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
