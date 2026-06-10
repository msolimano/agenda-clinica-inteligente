package com.iclinical.technology.fhir.bundle;

import com.iclinical.technology.clinicaldiagnoses.ClinicalDiagnosis;
import com.iclinical.technology.clinicaldiagnoses.ClinicalDiagnosisRepository;
import com.iclinical.technology.clinicalprescriptions.ClinicalPrescription;
import com.iclinical.technology.clinicalprescriptions.ClinicalPrescriptionRepository;
import com.iclinical.technology.clinicalrecords.ClinicalRecord;
import com.iclinical.technology.clinicalrecords.ClinicalRecordRepository;
import com.iclinical.technology.documents.ClinicalDocumentRepository;
import com.iclinical.technology.fhir.FHIRResourceNotFoundException;
import com.iclinical.technology.fhir.condition.ConditionFHIRMapper;
import com.iclinical.technology.fhir.document.DocumentReferenceFHIRMapper;
import com.iclinical.technology.fhir.encounter.EncounterFHIRMapper;
import com.iclinical.technology.fhir.medication.MedicationRequestFHIRMapper;
import com.iclinical.technology.fhir.patient.PatientFHIRMapper;
import com.iclinical.technology.patients.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FHIRBundleService {

    private static final Instant DEFAULT_FROM = Instant.parse("1900-01-01T00:00:00Z");
    private static final Instant DEFAULT_TO = Instant.parse("2999-12-31T23:59:59Z");

    private final PatientRepository patientRepository;
    private final ClinicalRecordRepository recordRepository;
    private final ClinicalDiagnosisRepository diagnosisRepository;
    private final ClinicalPrescriptionRepository prescriptionRepository;
    private final ClinicalDocumentRepository documentRepository;
    private final PatientFHIRMapper patientFHIRMapper;
    private final EncounterFHIRMapper encounterFHIRMapper;
    private final ConditionFHIRMapper conditionFHIRMapper;
    private final MedicationRequestFHIRMapper medicationRequestFHIRMapper;
    private final DocumentReferenceFHIRMapper documentReferenceFHIRMapper;
    private final FHIRBundleBuilder bundleBuilder;

    public FHIRBundleService(
        PatientRepository patientRepository,
        ClinicalRecordRepository recordRepository,
        ClinicalDiagnosisRepository diagnosisRepository,
        ClinicalPrescriptionRepository prescriptionRepository,
        ClinicalDocumentRepository documentRepository,
        PatientFHIRMapper patientFHIRMapper,
        EncounterFHIRMapper encounterFHIRMapper,
        ConditionFHIRMapper conditionFHIRMapper,
        MedicationRequestFHIRMapper medicationRequestFHIRMapper,
        DocumentReferenceFHIRMapper documentReferenceFHIRMapper,
        FHIRBundleBuilder bundleBuilder
    ) {
        this.patientRepository = patientRepository;
        this.recordRepository = recordRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.documentRepository = documentRepository;
        this.patientFHIRMapper = patientFHIRMapper;
        this.encounterFHIRMapper = encounterFHIRMapper;
        this.conditionFHIRMapper = conditionFHIRMapper;
        this.medicationRequestFHIRMapper = medicationRequestFHIRMapper;
        this.documentReferenceFHIRMapper = documentReferenceFHIRMapper;
        this.bundleBuilder = bundleBuilder;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> patientBundle(FHIRBundleRequest request) {
        var patient = patientRepository.findById(request.patientId())
            .filter(item -> !"deleted".equals(item.getStatus()))
            .orElseThrow(() -> new FHIRResourceNotFoundException("Patient no encontrado"));

        var from = request.from() == null ? DEFAULT_FROM : request.from();
        var to = request.to() == null ? DEFAULT_TO : request.to();

        var resources = new ArrayList<Map<String, Object>>();
        resources.add(patientFHIRMapper.toFHIR(patient));

        var records = request.includeEncounters()
            ? recordRepository.findFHIRBundleRecords(request.patientId(), from, to)
            : List.<ClinicalRecord>of();
        var recordIds = records.stream().map(ClinicalRecord::getId).toList();

        if (request.includeEncounters()) {
            records.stream()
                .map(encounterFHIRMapper::toFHIR)
                .forEach(resources::add);
        }

        if (request.includeDiagnoses()) {
            List<ClinicalDiagnosis> diagnoses = request.includeEncounters()
                ? recordIds.isEmpty() ? List.of() : diagnosisRepository.findActiveByClinicalRecordIdIn(recordIds)
                : diagnosisRepository.findActiveFHIRBundleByPatient(request.patientId(), from, to);
            diagnoses.stream()
                .map(conditionFHIRMapper::toFHIR)
                .forEach(resources::add);
        }

        if (request.includePrescriptions()) {
            List<ClinicalPrescription> prescriptions = request.includeEncounters()
                ? recordIds.isEmpty() ? List.of() : prescriptionRepository.findActiveByClinicalRecordIdIn(recordIds)
                : prescriptionRepository.findActiveFHIRBundleByPatient(request.patientId(), from, to);
            prescriptions.stream()
                .map(medicationRequestFHIRMapper::toFHIR)
                .forEach(resources::add);
        }

        if (request.includeDocuments()) {
            documentRepository.findActiveFHIRBundleByPatient(request.patientId(), from, to)
                .stream()
                .map(documentReferenceFHIRMapper::toFHIR)
                .forEach(resources::add);
        }

        return bundleBuilder.collection(resources);
    }
}
