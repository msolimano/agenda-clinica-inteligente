package com.iclinical.technology.clinicalprescriptions;

import com.iclinical.technology.clinicaldiagnoses.ClinicalDiagnosis;
import com.iclinical.technology.clinicaldiagnoses.ClinicalDiagnosisRepository;
import com.iclinical.technology.clinicalprescriptions.dto.ClinicalPrescriptionCreateRequest;
import com.iclinical.technology.clinicalprescriptions.dto.ClinicalPrescriptionResponse;
import com.iclinical.technology.clinicalprescriptions.dto.ClinicalPrescriptionStatusRequest;
import com.iclinical.technology.clinicalprescriptions.dto.ClinicalPrescriptionUpdateRequest;
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
public class ClinicalPrescriptionService {

    private static final Set<String> PRESCRIPTION_STATUSES = Set.of("draft", "active", "suspended", "completed", "cancelled");

    private final ClinicalPrescriptionRepository prescriptionRepository;
    private final ClinicalRecordRepository recordRepository;
    private final ClinicalDiagnosisRepository diagnosisRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;

    public ClinicalPrescriptionService(
        ClinicalPrescriptionRepository prescriptionRepository,
        ClinicalRecordRepository recordRepository,
        ClinicalDiagnosisRepository diagnosisRepository,
        PatientRepository patientRepository,
        ProfessionalRepository professionalRepository
    ) {
        this.prescriptionRepository = prescriptionRepository;
        this.recordRepository = recordRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
    }

    @Transactional(readOnly = true)
    public List<ClinicalPrescriptionResponse> listByRecord(UUID recordId) {
        findRecord(recordId);
        return prescriptionRepository.findByClinicalRecordIdAndStatusOrderByCreatedAtDesc(recordId, "active")
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ClinicalPrescriptionResponse> listByPatient(UUID patientId) {
        if (patientId == null) {
            throw new ClinicalPrescriptionValidationException("Debe indicar el paciente");
        }
        patientRepository.findById(patientId)
            .filter(patient -> !"deleted".equals(patient.getStatus()))
            .orElseThrow(() -> new ClinicalPrescriptionValidationException("Paciente no encontrado o eliminado"));
        return prescriptionRepository.findByPatientIdAndStatusOrderByCreatedAtDesc(patientId, "active")
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ClinicalPrescriptionResponse getById(UUID id) {
        return toResponse(findPrescription(id));
    }

    @Transactional
    public ClinicalPrescriptionResponse create(UUID recordId, ClinicalPrescriptionCreateRequest request) {
        var record = findEditableRecord(recordId);
        var diagnosis = resolveDiagnosis(request == null ? null : request.diagnosisId(), record);

        var prescription = new ClinicalPrescription();
        prescription.setOrganizationId(record.getOrganizationId());
        prescription.setClinicalRecordId(record.getId());
        prescription.setPatientId(record.getPatientId());
        prescription.setProfessionalId(record.getProfessionalId());
        prescription.setDiagnosisId(diagnosis == null ? null : diagnosis.getId());
        applyFields(
            prescription,
            request == null ? null : request.medicationName(),
            request == null ? null : request.dosage(),
            request == null ? null : request.frequency(),
            request == null ? null : request.duration(),
            request == null ? null : request.route(),
            request == null ? null : request.patientInstructions(),
            request == null ? null : request.clinicalNotes(),
            request == null ? null : request.prescriptionStatus(),
            "draft"
        );
        return toResponse(prescriptionRepository.save(prescription));
    }

    @Transactional
    public ClinicalPrescriptionResponse update(UUID id, ClinicalPrescriptionUpdateRequest request) {
        var prescription = findPrescription(id);
        var record = findEditableRecord(prescription.getClinicalRecordId());
        var diagnosis = resolveDiagnosis(request == null ? null : request.diagnosisId(), record);

        prescription.setDiagnosisId(diagnosis == null ? null : diagnosis.getId());
        applyFields(
            prescription,
            request == null ? null : request.medicationName(),
            request == null ? null : request.dosage(),
            request == null ? null : request.frequency(),
            request == null ? null : request.duration(),
            request == null ? null : request.route(),
            request == null ? null : request.patientInstructions(),
            request == null ? null : request.clinicalNotes(),
            request == null ? null : request.prescriptionStatus(),
            prescription.getPrescriptionStatus()
        );
        return toResponse(prescription);
    }

    @Transactional
    public ClinicalPrescriptionResponse updateStatus(UUID id, ClinicalPrescriptionStatusRequest request) {
        var prescription = findPrescription(id);
        findEditableRecord(prescription.getClinicalRecordId());
        prescription.setPrescriptionStatus(resolvePrescriptionStatus(request == null ? null : request.prescriptionStatus(), null));
        return toResponse(prescription);
    }

    private void applyFields(
        ClinicalPrescription prescription,
        String medicationName,
        String dosage,
        String frequency,
        String duration,
        String route,
        String patientInstructions,
        String clinicalNotes,
        String prescriptionStatus,
        String fallbackPrescriptionStatus
    ) {
        prescription.setMedicationName(require(medicationName, "Debe indicar el medicamento"));
        prescription.setDosage(require(dosage, "Debe indicar la dosis"));
        prescription.setFrequency(require(frequency, "Debe indicar la frecuencia"));
        prescription.setDuration(clean(duration));
        prescription.setRoute(clean(route));
        prescription.setPatientInstructions(clean(patientInstructions));
        prescription.setClinicalNotes(clean(clinicalNotes));
        prescription.setPrescriptionStatus(resolvePrescriptionStatus(prescriptionStatus, fallbackPrescriptionStatus));
    }

    private ClinicalDiagnosis resolveDiagnosis(UUID diagnosisId, ClinicalRecord record) {
        if (diagnosisId == null) {
            return null;
        }
        var diagnosis = diagnosisRepository.findById(diagnosisId)
            .filter(item -> "active".equals(item.getStatus()))
            .orElseThrow(() -> new ClinicalPrescriptionValidationException("Diagnostico clinico no encontrado o inactivo"));
        if (!diagnosis.getClinicalRecordId().equals(record.getId())) {
            throw new ClinicalPrescriptionValidationException("El diagnostico debe pertenecer a la misma ficha clinica");
        }
        if ("ruled_out".equals(diagnosis.getDiagnosisStatus())) {
            throw new ClinicalPrescriptionValidationException("No se puede asociar una prescripcion a un diagnostico descartado");
        }
        return diagnosis;
    }

    private ClinicalRecord findEditableRecord(UUID recordId) {
        var record = findRecord(recordId);
        if ("closed".equals(record.getStatus())) {
            throw new ClinicalPrescriptionValidationException("No se permite crear ni editar prescripciones en una ficha clinica cerrada");
        }
        return record;
    }

    private ClinicalRecord findRecord(UUID recordId) {
        if (recordId == null) {
            throw new ClinicalPrescriptionValidationException("Debe indicar la ficha clinica");
        }
        return recordRepository.findById(recordId)
            .filter(record -> !"deleted".equals(record.getStatus()))
            .orElseThrow(() -> new ClinicalPrescriptionValidationException("Ficha clinica no encontrada"));
    }

    private ClinicalPrescription findPrescription(UUID id) {
        return prescriptionRepository.findById(id)
            .filter(prescription -> "active".equals(prescription.getStatus()))
            .orElseThrow(() -> new ClinicalPrescriptionNotFoundException("Prescripcion clinica no encontrada"));
    }

    private String resolvePrescriptionStatus(String value, String fallback) {
        var status = clean(value);
        if (!StringUtils.hasText(status)) {
            if (fallback != null) {
                return fallback;
            }
            throw new ClinicalPrescriptionValidationException("Debe indicar el estado de la prescripcion");
        }
        if (!PRESCRIPTION_STATUSES.contains(status)) {
            throw new ClinicalPrescriptionValidationException("Estado de prescripcion permitido: draft, active, suspended, completed o cancelled");
        }
        return status;
    }

    private String require(String value, String message) {
        var text = clean(value);
        if (!StringUtils.hasText(text)) {
            throw new ClinicalPrescriptionValidationException(message);
        }
        return text;
    }

    private ClinicalPrescriptionResponse toResponse(ClinicalPrescription prescription) {
        return new ClinicalPrescriptionResponse(
            prescription.getId(),
            prescription.getOrganizationId(),
            prescription.getClinicalRecordId(),
            prescription.getPatientId(),
            patientName(prescription.getPatientId()),
            prescription.getProfessionalId(),
            professionalName(prescription.getProfessionalId()),
            prescription.getDiagnosisId(),
            diagnosisText(prescription.getDiagnosisId()),
            prescription.getMedicationName(),
            prescription.getDosage(),
            prescription.getFrequency(),
            prescription.getDuration(),
            prescription.getRoute(),
            prescription.getPatientInstructions(),
            prescription.getClinicalNotes(),
            prescription.getPrescriptionStatus(),
            prescription.getStatus(),
            prescription.getCreatedAt(),
            prescription.getUpdatedAt()
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

    private String diagnosisText(UUID diagnosisId) {
        if (diagnosisId == null) {
            return null;
        }
        return diagnosisRepository.findById(diagnosisId)
            .filter(diagnosis -> "active".equals(diagnosis.getStatus()))
            .map(ClinicalDiagnosis::getDiagnosisText)
            .orElse(null);
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
