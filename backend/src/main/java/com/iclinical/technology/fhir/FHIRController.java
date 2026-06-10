package com.iclinical.technology.fhir;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iclinical.technology.clinicaldiagnoses.ClinicalDiagnosisRepository;
import com.iclinical.technology.clinicalprescriptions.ClinicalPrescriptionRepository;
import com.iclinical.technology.clinicalrecords.ClinicalRecordRepository;
import com.iclinical.technology.documents.ClinicalDocumentRepository;
import com.iclinical.technology.fhir.bundle.FHIRBundleRequest;
import com.iclinical.technology.fhir.bundle.FHIRBundleService;
import com.iclinical.technology.fhir.condition.ConditionFHIRMapper;
import com.iclinical.technology.fhir.document.DocumentReferenceFHIRMapper;
import com.iclinical.technology.fhir.encounter.EncounterFHIRMapper;
import com.iclinical.technology.fhir.medication.MedicationRequestFHIRMapper;
import com.iclinical.technology.fhir.patient.PatientFHIRMapper;
import com.iclinical.technology.fhir.practitioner.PractitionerFHIRMapper;
import com.iclinical.technology.patients.PatientRepository;
import com.iclinical.technology.professionals.ProfessionalRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/fhir", produces = FHIRController.FHIR_JSON)
public class FHIRController {

    public static final String FHIR_JSON = "application/fhir+json";
    private static final DateTimeFormatter DOWNLOAD_FILENAME_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC);

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
    private final FHIRBundleService fhirBundleService;
    private final ObjectMapper objectMapper;

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
        DocumentReferenceFHIRMapper documentReferenceFHIRMapper,
        FHIRBundleService fhirBundleService,
        ObjectMapper objectMapper
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
        this.fhirBundleService = fhirBundleService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/patients/{patientId}/bundle/download")
    public ResponseEntity<String> patientBundleDownload(
        @PathVariable UUID patientId,
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(defaultValue = "true") boolean includeDocuments,
        @RequestParam(defaultValue = "true") boolean includePrescriptions,
        @RequestParam(defaultValue = "true") boolean includeDiagnoses,
        @RequestParam(defaultValue = "true") boolean includeEncounters
    ) throws JsonProcessingException {
        var bundle = fhirBundleService.patientBundle(new FHIRBundleRequest(
            patientId,
            from,
            to,
            includeDocuments,
            includePrescriptions,
            includeDiagnoses,
            includeEncounters
        ));
        var json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bundle);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(FHIR_JSON))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFileName(patientId) + "\"")
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .body(json);
    }

    @GetMapping("/patients/{patientId}/bundle")
    public ResponseEntity<Map<String, Object>> patientBundle(
        @PathVariable UUID patientId,
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(defaultValue = "true") boolean includeDocuments,
        @RequestParam(defaultValue = "true") boolean includePrescriptions,
        @RequestParam(defaultValue = "true") boolean includeDiagnoses,
        @RequestParam(defaultValue = "true") boolean includeEncounters
    ) {
        return fhirResponse(fhirBundleService.patientBundle(new FHIRBundleRequest(
            patientId,
            from,
            to,
            includeDocuments,
            includePrescriptions,
            includeDiagnoses,
            includeEncounters
        )));
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

    private String downloadFileName(UUID patientId) {
        return "iclinical-fhir-bundle-patient-" + patientId + "-" + DOWNLOAD_FILENAME_TIMESTAMP.format(Instant.now()) + ".json";
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
                supportedResource("DocumentReference"),
                supportedResource("Bundle")
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
