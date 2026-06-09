package com.iclinical.technology.clinicalevolutions;

import com.iclinical.technology.clinicalevolutions.dto.ClinicalEvolutionCreateRequest;
import com.iclinical.technology.clinicalevolutions.dto.ClinicalEvolutionResponse;
import com.iclinical.technology.clinicalevolutions.dto.ClinicalEvolutionStatusRequest;
import com.iclinical.technology.clinicalevolutions.dto.ClinicalEvolutionUpdateRequest;
import com.iclinical.technology.clinicalrecords.ClinicalRecord;
import com.iclinical.technology.clinicalrecords.ClinicalRecordRepository;
import com.iclinical.technology.patients.PatientRepository;
import com.iclinical.technology.professionals.ProfessionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ClinicalEvolutionService {

    private static final Set<String> EVOLUTION_STATUSES = Set.of("draft", "active", "corrected", "cancelled");

    private final ClinicalEvolutionRepository evolutionRepository;
    private final ClinicalRecordRepository recordRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;

    public ClinicalEvolutionService(
        ClinicalEvolutionRepository evolutionRepository,
        ClinicalRecordRepository recordRepository,
        PatientRepository patientRepository,
        ProfessionalRepository professionalRepository
    ) {
        this.evolutionRepository = evolutionRepository;
        this.recordRepository = recordRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
    }

    @Transactional(readOnly = true)
    public List<ClinicalEvolutionResponse> listByRecord(UUID recordId) {
        findRecord(recordId);
        return evolutionRepository.findByClinicalRecordIdAndStatusOrderByEvolutionDateDescCreatedAtDesc(recordId, "active")
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ClinicalEvolutionResponse> listByPatient(UUID patientId) {
        if (patientId == null) {
            throw new ClinicalEvolutionValidationException("Debe indicar el paciente");
        }
        patientRepository.findById(patientId)
            .filter(patient -> !"deleted".equals(patient.getStatus()))
            .orElseThrow(() -> new ClinicalEvolutionValidationException("Paciente no encontrado o eliminado"));
        return evolutionRepository.findByPatientIdAndStatusOrderByEvolutionDateDescCreatedAtDesc(patientId, "active")
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ClinicalEvolutionResponse getById(UUID id) {
        return toResponse(findEvolution(id));
    }

    @Transactional
    public ClinicalEvolutionResponse create(UUID recordId, ClinicalEvolutionCreateRequest request) {
        var record = findEditableRecord(recordId);
        var evolution = new ClinicalEvolution();
        evolution.setOrganizationId(record.getOrganizationId());
        evolution.setClinicalRecordId(record.getId());
        evolution.setPatientId(record.getPatientId());
        evolution.setProfessionalId(record.getProfessionalId());
        applyFields(
            evolution,
            request == null ? null : request.evolutionDate(),
            request == null ? null : request.subjective(),
            request == null ? null : request.objective(),
            request == null ? null : request.assessment(),
            request == null ? null : request.plan(),
            request == null ? null : request.notes(),
            request == null ? null : request.evolutionStatus(),
            "draft"
        );
        return toResponse(evolutionRepository.save(evolution));
    }

    @Transactional
    public ClinicalEvolutionResponse update(UUID id, ClinicalEvolutionUpdateRequest request) {
        var evolution = findEvolution(id);
        findEditableRecord(evolution.getClinicalRecordId());
        applyFields(
            evolution,
            request == null ? null : request.evolutionDate(),
            request == null ? null : request.subjective(),
            request == null ? null : request.objective(),
            request == null ? null : request.assessment(),
            request == null ? null : request.plan(),
            request == null ? null : request.notes(),
            request == null ? null : request.evolutionStatus(),
            evolution.getEvolutionStatus()
        );
        return toResponse(evolution);
    }

    @Transactional
    public ClinicalEvolutionResponse updateStatus(UUID id, ClinicalEvolutionStatusRequest request) {
        var evolution = findEvolution(id);
        findEditableRecord(evolution.getClinicalRecordId());
        evolution.setEvolutionStatus(resolveEvolutionStatus(request == null ? null : request.evolutionStatus(), null));
        return toResponse(evolution);
    }

    private void applyFields(
        ClinicalEvolution evolution,
        Instant evolutionDate,
        String subjective,
        String objective,
        String assessment,
        String plan,
        String notes,
        String evolutionStatus,
        String fallbackEvolutionStatus
    ) {
        var cleanedSubjective = clean(subjective);
        var cleanedObjective = clean(objective);
        var cleanedAssessment = clean(assessment);
        var cleanedPlan = clean(plan);
        if (!StringUtils.hasText(cleanedSubjective)
            && !StringUtils.hasText(cleanedObjective)
            && !StringUtils.hasText(cleanedAssessment)
            && !StringUtils.hasText(cleanedPlan)) {
            throw new ClinicalEvolutionValidationException("Debe informar al menos un campo SOAP");
        }

        evolution.setEvolutionDate(evolutionDate == null ? Instant.now() : evolutionDate);
        evolution.setSubjective(cleanedSubjective);
        evolution.setObjective(cleanedObjective);
        evolution.setAssessment(cleanedAssessment);
        evolution.setPlan(cleanedPlan);
        evolution.setNotes(clean(notes));
        evolution.setEvolutionStatus(resolveEvolutionStatus(evolutionStatus, fallbackEvolutionStatus));
    }

    private ClinicalRecord findEditableRecord(UUID recordId) {
        var record = findRecord(recordId);
        if ("closed".equals(record.getStatus())) {
            throw new ClinicalEvolutionValidationException("No se permite crear ni editar evoluciones en una ficha clinica cerrada");
        }
        return record;
    }

    private ClinicalRecord findRecord(UUID recordId) {
        if (recordId == null) {
            throw new ClinicalEvolutionValidationException("Debe indicar la ficha clinica");
        }
        return recordRepository.findById(recordId)
            .filter(record -> !"deleted".equals(record.getStatus()))
            .orElseThrow(() -> new ClinicalEvolutionValidationException("Ficha clinica no encontrada"));
    }

    private ClinicalEvolution findEvolution(UUID id) {
        return evolutionRepository.findById(id)
            .filter(evolution -> "active".equals(evolution.getStatus()))
            .orElseThrow(() -> new ClinicalEvolutionNotFoundException("Evolucion clinica no encontrada"));
    }

    private String resolveEvolutionStatus(String value, String fallback) {
        var status = clean(value);
        if (!StringUtils.hasText(status)) {
            if (fallback != null) {
                return fallback;
            }
            throw new ClinicalEvolutionValidationException("Debe indicar el estado de la evolucion");
        }
        if (!EVOLUTION_STATUSES.contains(status)) {
            throw new ClinicalEvolutionValidationException("Estado de evolucion permitido: draft, active, corrected o cancelled");
        }
        return status;
    }

    private ClinicalEvolutionResponse toResponse(ClinicalEvolution evolution) {
        return new ClinicalEvolutionResponse(
            evolution.getId(),
            evolution.getOrganizationId(),
            evolution.getClinicalRecordId(),
            evolution.getPatientId(),
            patientName(evolution.getPatientId()),
            evolution.getProfessionalId(),
            professionalName(evolution.getProfessionalId()),
            evolution.getEvolutionDate(),
            evolution.getSubjective(),
            evolution.getObjective(),
            evolution.getAssessment(),
            evolution.getPlan(),
            evolution.getNotes(),
            evolution.getEvolutionStatus(),
            evolution.getStatus(),
            evolution.getCreatedAt(),
            evolution.getUpdatedAt()
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
