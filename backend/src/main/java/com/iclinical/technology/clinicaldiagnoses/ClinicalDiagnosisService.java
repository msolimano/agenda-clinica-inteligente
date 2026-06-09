package com.iclinical.technology.clinicaldiagnoses;

import com.iclinical.technology.clinicaldiagnoses.dto.ClinicalDiagnosisCreateRequest;
import com.iclinical.technology.clinicaldiagnoses.dto.ClinicalDiagnosisResponse;
import com.iclinical.technology.clinicaldiagnoses.dto.ClinicalDiagnosisStatusRequest;
import com.iclinical.technology.clinicaldiagnoses.dto.ClinicalDiagnosisUpdateRequest;
import com.iclinical.technology.clinicalrecords.ClinicalRecord;
import com.iclinical.technology.clinicalrecords.ClinicalRecordRepository;
import com.iclinical.technology.patients.PatientRepository;
import com.iclinical.technology.professionals.ProfessionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ClinicalDiagnosisService {

    private static final Set<String> DIAGNOSIS_STATUSES = Set.of("suspected", "confirmed", "resolved", "ruled_out");

    private final ClinicalDiagnosisRepository diagnosisRepository;
    private final ClinicalRecordRepository recordRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;

    public ClinicalDiagnosisService(
        ClinicalDiagnosisRepository diagnosisRepository,
        ClinicalRecordRepository recordRepository,
        PatientRepository patientRepository,
        ProfessionalRepository professionalRepository
    ) {
        this.diagnosisRepository = diagnosisRepository;
        this.recordRepository = recordRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
    }

    @Transactional(readOnly = true)
    public List<ClinicalDiagnosisResponse> listByRecord(UUID recordId) {
        findRecord(recordId);
        return diagnosisRepository.findByClinicalRecordIdAndStatusOrderByPrimaryDiagnosisDescCreatedAtDesc(recordId, "active")
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ClinicalDiagnosisResponse> listByPatient(UUID patientId) {
        if (patientId == null) {
            throw new ClinicalDiagnosisValidationException("Debe indicar el paciente");
        }
        patientRepository.findById(patientId)
            .filter(patient -> !"deleted".equals(patient.getStatus()))
            .orElseThrow(() -> new ClinicalDiagnosisValidationException("Paciente no encontrado o eliminado"));
        return diagnosisRepository.findByPatientIdAndStatusOrderByCreatedAtDesc(patientId, "active")
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ClinicalDiagnosisResponse getById(UUID id) {
        return toResponse(findDiagnosis(id));
    }

    @Transactional
    public ClinicalDiagnosisResponse create(UUID recordId, ClinicalDiagnosisCreateRequest request) {
        var record = findEditableRecord(recordId);
        var diagnosis = new ClinicalDiagnosis();
        diagnosis.setOrganizationId(record.getOrganizationId());
        diagnosis.setClinicalRecordId(record.getId());
        diagnosis.setPatientId(record.getPatientId());
        diagnosis.setProfessionalId(record.getProfessionalId());
        applyFields(
            diagnosis,
            request == null ? null : request.diagnosisText(),
            request == null ? null : request.primary(),
            request == null ? null : request.diagnosisStatus(),
            request == null ? null : request.observations(),
            "suspected"
        );
        if (diagnosis.isPrimary()) {
            diagnosisRepository.clearPrimaryForRecord(record.getId(), null);
        }
        return toResponse(diagnosisRepository.save(diagnosis));
    }

    @Transactional
    public ClinicalDiagnosisResponse update(UUID id, ClinicalDiagnosisUpdateRequest request) {
        var diagnosis = findDiagnosis(id);
        findEditableRecord(diagnosis.getClinicalRecordId());
        applyFields(
            diagnosis,
            request == null ? null : request.diagnosisText(),
            request == null ? null : request.primary(),
            request == null ? null : request.diagnosisStatus(),
            request == null ? null : request.observations(),
            diagnosis.getDiagnosisStatus()
        );
        if (diagnosis.isPrimary()) {
            diagnosisRepository.clearPrimaryForRecord(diagnosis.getClinicalRecordId(), diagnosis.getId());
        }
        return toResponse(diagnosis);
    }

    @Transactional
    public ClinicalDiagnosisResponse updateStatus(UUID id, ClinicalDiagnosisStatusRequest request) {
        var diagnosis = findDiagnosis(id);
        findEditableRecord(diagnosis.getClinicalRecordId());
        diagnosis.setDiagnosisStatus(resolveDiagnosisStatus(request == null ? null : request.diagnosisStatus(), null));
        return toResponse(diagnosis);
    }

    private void applyFields(
        ClinicalDiagnosis diagnosis,
        String diagnosisText,
        Boolean primary,
        String diagnosisStatus,
        String observations,
        String fallbackDiagnosisStatus
    ) {
        diagnosis.setDiagnosisText(requireDiagnosisText(diagnosisText));
        diagnosis.setPrimary(Boolean.TRUE.equals(primary));
        diagnosis.setDiagnosisStatus(resolveDiagnosisStatus(diagnosisStatus, fallbackDiagnosisStatus));
        diagnosis.setObservations(clean(observations));
    }

    private ClinicalRecord findEditableRecord(UUID recordId) {
        var record = findRecord(recordId);
        if ("closed".equals(record.getStatus())) {
            throw new ClinicalDiagnosisValidationException("No se permite crear ni editar diagnosticos en una ficha clinica cerrada");
        }
        return record;
    }

    private ClinicalRecord findRecord(UUID recordId) {
        if (recordId == null) {
            throw new ClinicalDiagnosisValidationException("Debe indicar la ficha clinica");
        }
        return recordRepository.findById(recordId)
            .filter(record -> !"deleted".equals(record.getStatus()))
            .orElseThrow(() -> new ClinicalDiagnosisValidationException("Ficha clinica no encontrada"));
    }

    private ClinicalDiagnosis findDiagnosis(UUID id) {
        return diagnosisRepository.findById(id)
            .filter(diagnosis -> "active".equals(diagnosis.getStatus()))
            .orElseThrow(() -> new ClinicalDiagnosisNotFoundException("Diagnostico clinico no encontrado"));
    }

    private String requireDiagnosisText(String value) {
        var text = clean(value);
        if (!StringUtils.hasText(text)) {
            throw new ClinicalDiagnosisValidationException("Debe indicar el diagnostico en texto libre");
        }
        return text;
    }

    private String resolveDiagnosisStatus(String value, String fallback) {
        var status = clean(value);
        if (!StringUtils.hasText(status)) {
            if (fallback != null) {
                return fallback;
            }
            throw new ClinicalDiagnosisValidationException("Debe indicar el estado del diagnostico");
        }
        if (!DIAGNOSIS_STATUSES.contains(status)) {
            throw new ClinicalDiagnosisValidationException("Estado de diagnostico permitido: suspected, confirmed, resolved o ruled_out");
        }
        return status;
    }

    private ClinicalDiagnosisResponse toResponse(ClinicalDiagnosis diagnosis) {
        return new ClinicalDiagnosisResponse(
            diagnosis.getId(),
            diagnosis.getOrganizationId(),
            diagnosis.getClinicalRecordId(),
            diagnosis.getPatientId(),
            patientName(diagnosis.getPatientId()),
            diagnosis.getProfessionalId(),
            professionalName(diagnosis.getProfessionalId()),
            diagnosis.getDiagnosisText(),
            diagnosis.isPrimary(),
            diagnosis.getDiagnosisStatus(),
            diagnosis.getObservations(),
            diagnosis.getCodeSystem(),
            diagnosis.getDiagnosisCode(),
            diagnosis.getDiagnosisCodeDisplay(),
            diagnosis.getStatus(),
            diagnosis.getCreatedAt(),
            diagnosis.getUpdatedAt()
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
