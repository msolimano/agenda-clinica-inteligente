package com.iclinical.technology.patientportal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iclinical.technology.fhir.bundle.FHIRBundleRequest;
import com.iclinical.technology.fhir.bundle.FHIRBundleService;
import com.iclinical.technology.patientportal.dto.PatientPortalAIConsentResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalAppointmentResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalClinicalHistoryResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalDiagnosisResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalDocumentResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalPrescriptionResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalSummaryResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class PatientPortalService {

    private static final String FHIR_JSON = "application/fhir+json";
    private static final Instant FHIR_EXPORT_FROM = Instant.parse("1900-01-01T00:00:00Z");
    private static final Instant FHIR_EXPORT_TO = Instant.parse("2999-12-31T23:59:59Z");
    private static final DateTimeFormatter DOWNLOAD_FILENAME_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC);

    private final PatientPortalRepository repository;
    private final FHIRBundleService fhirBundleService;
    private final ObjectMapper objectMapper;

    public PatientPortalService(PatientPortalRepository repository, FHIRBundleService fhirBundleService, ObjectMapper objectMapper) {
        this.repository = repository;
        this.fhirBundleService = fhirBundleService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PatientPortalSummaryResponse summary(UUID patientId) {
        var patient = requirePatient(patientId);
        var aiConsent = repository.aiConsent(patientId);
        return new PatientPortalSummaryResponse(
            patient,
            repository.countUpcomingAppointments(patientId),
            repository.countHistoricalAppointments(patientId),
            repository.countActiveDocuments(patientId),
            repository.countActivePrescriptions(patientId),
            repository.countActiveDiagnoses(patientId),
            repository.countClinicalRecords(patientId),
            aiConsent
        );
    }

    @Transactional(readOnly = true)
    public List<PatientPortalAppointmentResponse> appointments(UUID patientId) {
        requirePatient(patientId);
        return repository.appointments(patientId);
    }

    @Transactional(readOnly = true)
    public List<PatientPortalDocumentResponse> documents(UUID patientId) {
        requirePatient(patientId);
        return repository.documents(patientId);
    }

    @Transactional(readOnly = true)
    public List<PatientPortalPrescriptionResponse> prescriptions(UUID patientId) {
        requirePatient(patientId);
        return repository.prescriptions(patientId);
    }

    @Transactional(readOnly = true)
    public List<PatientPortalDiagnosisResponse> diagnoses(UUID patientId) {
        requirePatient(patientId);
        return repository.diagnoses(patientId);
    }

    @Transactional(readOnly = true)
    public PatientPortalClinicalHistoryResponse clinicalHistory(UUID patientId) {
        requirePatient(patientId);
        return new PatientPortalClinicalHistoryResponse(repository.clinicalRecords(patientId));
    }

    @Transactional(readOnly = true)
    public PatientPortalAIConsentResponse aiConsent(UUID patientId) {
        requirePatient(patientId);
        return repository.aiConsent(patientId);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<String> fhirBundleDownload(UUID patientId) throws JsonProcessingException {
        requirePatient(patientId);
        var bundle = fhirBundleService.patientBundle(new FHIRBundleRequest(patientId, FHIR_EXPORT_FROM, FHIR_EXPORT_TO, true, true, true, true));
        var json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bundle);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(FHIR_JSON))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFileName(patientId) + "\"")
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .body(json);
    }

    private com.iclinical.technology.patientportal.dto.PatientPortalPatientResponse requirePatient(UUID patientId) {
        return repository.findActivePatient(patientId)
            .filter(patient -> "active".equals(patient.status()))
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Paciente no encontrado"));
    }

    private String downloadFileName(UUID patientId) {
        return "iclinical-fhir-bundle-patient-" + patientId + "-" + DOWNLOAD_FILENAME_TIMESTAMP.format(Instant.now()) + ".json";
    }
}
