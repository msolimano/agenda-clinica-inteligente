package com.iclinical.technology.patientportal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iclinical.technology.patientportal.dto.PatientPortalAIConsentResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalAppointmentResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalClinicalHistoryResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalDiagnosisResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalDocumentResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalPrescriptionResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalSummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patient-portal/{patientId}")
public class PatientPortalController {

    private final PatientPortalService patientPortalService;

    public PatientPortalController(PatientPortalService patientPortalService) {
        this.patientPortalService = patientPortalService;
    }

    @GetMapping("/summary")
    public PatientPortalSummaryResponse summary(@PathVariable UUID patientId) {
        return patientPortalService.summary(patientId);
    }

    @GetMapping("/appointments")
    public List<PatientPortalAppointmentResponse> appointments(@PathVariable UUID patientId) {
        return patientPortalService.appointments(patientId);
    }

    @GetMapping("/documents")
    public List<PatientPortalDocumentResponse> documents(@PathVariable UUID patientId) {
        return patientPortalService.documents(patientId);
    }

    @GetMapping("/prescriptions")
    public List<PatientPortalPrescriptionResponse> prescriptions(@PathVariable UUID patientId) {
        return patientPortalService.prescriptions(patientId);
    }

    @GetMapping("/diagnoses")
    public List<PatientPortalDiagnosisResponse> diagnoses(@PathVariable UUID patientId) {
        return patientPortalService.diagnoses(patientId);
    }

    @GetMapping("/clinical-history")
    public PatientPortalClinicalHistoryResponse clinicalHistory(@PathVariable UUID patientId) {
        return patientPortalService.clinicalHistory(patientId);
    }

    @GetMapping("/ai-consent")
    public PatientPortalAIConsentResponse aiConsent(@PathVariable UUID patientId) {
        return patientPortalService.aiConsent(patientId);
    }

    @GetMapping("/fhir-bundle/download")
    public ResponseEntity<String> fhirBundleDownload(@PathVariable UUID patientId) throws JsonProcessingException {
        return patientPortalService.fhirBundleDownload(patientId);
    }
}
