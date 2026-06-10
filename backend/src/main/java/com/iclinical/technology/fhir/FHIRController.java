package com.iclinical.technology.fhir;

import com.iclinical.technology.clinicaldiagnoses.ClinicalDiagnosisRepository;
import com.iclinical.technology.clinicalprescriptions.ClinicalPrescriptionRepository;
import com.iclinical.technology.clinicalrecords.ClinicalRecordRepository;
import com.iclinical.technology.documents.ClinicalDocumentRepository;
import com.iclinical.technology.fhir.condition.ConditionFHIRMapper;
import com.iclinical.technology.fhir.document.DocumentReferenceFHIRMapper;
import com.iclinical.technology.fhir.encounter.EncounterFHIRMapper;
import com.iclinical.technology.fhir.medication.MedicationRequestFHIRMapper;
import com.iclinical.technology.fhir.patient.PatientFHIRMapper;
import com.iclinical.technology.fhir.practitioner.PractitionerFHIRMapper;
import com.iclinical.technology.patients.PatientRepository;
import com.iclinical.technology.professionals.ProfessionalRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/fhir", produces = FHIRController.FHIR_JSON)
public class FHIRController {

    public static final String FHIR_JSON = "application/fhir+json";

    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final ClinicalRecordRepository clinicalRecordRepository;
    private final ClinicalDiagnosisRepository diagnosisRepository;
    private final ClinicalPrescriptionRepository prescriptionRepository;
    private final ClinicalDocumentRepository documentRepository;
    private final PatientFHIRMapper patientFHIRMapper;
    private final PractitionerFHIRMapper practitionerFHIRMapper;
    private final EncounterFHIRMapper encounterFHIRMapper;
    private final ConditionFHIRMapper conditionFHIRMapper;
    private final MedicationRequestFHIRMapper medicationRequestFHIRMapper;
    private final DocumentReferenceFHIRMapper documentReferenceFHIRMapper;

    public FHIRController(
        PatientRepository patientRepository,
        ProfessionalRepository professionalRepository,
        ClinicalRecordRepository clinicalRecordRepository,
        ClinicalDiagnosisRepository diagnosisRepository,
        ClinicalPrescriptionRepository prescriptionRepository,
        ClinicalDocumentRepository documentRepository,
        PatientFHIRMapper patientFHIRMapper,
        PractitionerFHIRMapper practitionerFHIRMapper,
        EncounterFHIRMapper encounterFHIRMapper,
        ConditionFHIRMapper conditionFHIRMapper,
        MedicationRequestFHIRMapper medicationRequestFHIRMapper,
        DocumentReferenceFHIRMapper documentReferenceFHIRMapper
    ) {
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
        this.clinicalRecordRepository = clinicalRecordRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.documentRepository = documentRepository;
        this.patientFHIRMapper = patientFHIRMapper;
        this.practitionerFHIRMapper = practitionerFHIRMapper;
        this.encounterFHIRMapper = encounterFHIRMapper;
        this.conditionFHIRMapper = conditionFHIRMapper;
        this.medicationRequestFHIRMapper = medicationRequestFHIRMapper;
        this.documentReferenceFHIRMapper = documentReferenceFHIRMapper;
    }

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> metadata() {
        return fhirResponse(capabilityStatement());
    }

    @GetMapping("/Patient/{id}")
    public ResponseEntity<Map<String, Object>> patient(@PathVariable UUID id) {
        var patient = patientRepository.findById(id)
            .filter(item -> !"deleted".equals(item.getStatus()))
            .orElseThrow(() -> new FHIRResourceNotFoundException("Patient no encontrado"));
        return fhirResponse(patientFHIRMapper.toFHIR(patient));
    }

    @GetMapping("/Practitioner/{id}")
    public ResponseEntity<Map<String, Object>> practitioner(@PathVariable UUID id) {
        var professional = professionalRepository.findById(id)
            .filter(item -> !"deleted".equals(item.getStatus()))
            .orElseThrow(() -> new FHIRResourceNotFoundException("Practitioner no encontrado"));
        return fhirResponse(practitionerFHIRMapper.toFHIR(professional));
    }

    @GetMapping("/Encounter/{id}")
    public ResponseEntity<Map<String, Object>> encounter(@PathVariable UUID id) {
        var record = clinicalRecordRepository.findById(id)
            .filter(item -> !"deleted".equals(item.getStatus()))
            .orElseThrow(() -> new FHIRResourceNotFoundException("Encounter no encontrado"));
        return fhirResponse(encounterFHIRMapper.toFHIR(record));
    }

    @GetMapping("/Condition/{id}")
    public ResponseEntity<Map<String, Object>> condition(@PathVariable UUID id) {
        var diagnosis = diagnosisRepository.findById(id)
            .filter(item -> !"deleted".equals(item.getStatus()))
            .orElseThrow(() -> new FHIRResourceNotFoundException("Condition no encontrada"));
        return fhirResponse(conditionFHIRMapper.toFHIR(diagnosis));
    }

    @GetMapping("/MedicationRequest/{id}")
    public ResponseEntity<Map<String, Object>> medicationRequest(@PathVariable UUID id) {
        var prescription = prescriptionRepository.findById(id)
            .filter(item -> !"deleted".equals(item.getStatus()))
            .orElseThrow(() -> new FHIRResourceNotFoundException("MedicationRequest no encontrada"));
        return fhirResponse(medicationRequestFHIRMapper.toFHIR(prescription));
    }

    @GetMapping("/DocumentReference/{id}")
    public ResponseEntity<Map<String, Object>> documentReference(@PathVariable UUID id) {
        var document = documentRepository.findById(id)
            .filter(item -> !"deleted".equals(item.getStatus()))
            .orElseThrow(() -> new FHIRResourceNotFoundException("DocumentReference no encontrado"));
        return fhirResponse(documentReferenceFHIRMapper.toFHIR(document));
    }

    private ResponseEntity<Map<String, Object>> fhirResponse(Map<String, Object> body) {
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(FHIR_JSON))
            .body(body);
    }

    private Map<String, Object> capabilityStatement() {
        var statement = FHIRJson.resource("CapabilityStatement", UUID.nameUUIDFromBytes("iclinical-fhir-capability".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        statement.put("status", "active");
        statement.put("date", Instant.now().toString());
        statement.put("publisher", "I-Clinical Technology");
        statement.put("kind", "capability");
        statement.put("fhirVersion", "4.0.1");
        statement.put("format", List.of("json"));
        statement.put("rest", List.of(Map.of(
            "mode", "server",
            "resource", List.of(
                supportedResource("Patient"),
                supportedResource("Practitioner"),
                supportedResource("Encounter"),
                supportedResource("Condition"),
                supportedResource("MedicationRequest"),
                supportedResource("DocumentReference")
            )
        )));
        return statement;
    }

    private Map<String, Object> supportedResource(String type) {
        return Map.of(
            "type", type,
            "interaction", List.of(Map.of("code", "read"))
        );
    }
}
