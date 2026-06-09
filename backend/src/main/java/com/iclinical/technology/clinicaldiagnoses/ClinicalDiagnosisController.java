package com.iclinical.technology.clinicaldiagnoses;

import com.iclinical.technology.clinicaldiagnoses.dto.ClinicalDiagnosisCreateRequest;
import com.iclinical.technology.clinicaldiagnoses.dto.ClinicalDiagnosisResponse;
import com.iclinical.technology.clinicaldiagnoses.dto.ClinicalDiagnosisStatusRequest;
import com.iclinical.technology.clinicaldiagnoses.dto.ClinicalDiagnosisUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ClinicalDiagnosisController {

    private final ClinicalDiagnosisService diagnosisService;

    public ClinicalDiagnosisController(ClinicalDiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @GetMapping("/clinical-records/{recordId}/diagnoses")
    public List<ClinicalDiagnosisResponse> listByRecord(@PathVariable UUID recordId) {
        return diagnosisService.listByRecord(recordId);
    }

    @GetMapping("/clinical-diagnoses/{id}")
    public ClinicalDiagnosisResponse getById(@PathVariable UUID id) {
        return diagnosisService.getById(id);
    }

    @PostMapping("/clinical-records/{recordId}/diagnoses")
    @ResponseStatus(HttpStatus.CREATED)
    public ClinicalDiagnosisResponse create(@PathVariable UUID recordId, @RequestBody ClinicalDiagnosisCreateRequest request) {
        return diagnosisService.create(recordId, request);
    }

    @PutMapping("/clinical-diagnoses/{id}")
    public ClinicalDiagnosisResponse update(@PathVariable UUID id, @RequestBody ClinicalDiagnosisUpdateRequest request) {
        return diagnosisService.update(id, request);
    }

    @PatchMapping("/clinical-diagnoses/{id}/status")
    public ClinicalDiagnosisResponse updateStatus(@PathVariable UUID id, @RequestBody ClinicalDiagnosisStatusRequest request) {
        return diagnosisService.updateStatus(id, request);
    }

    @GetMapping("/patients/{patientId}/diagnoses")
    public List<ClinicalDiagnosisResponse> listByPatient(@PathVariable UUID patientId) {
        return diagnosisService.listByPatient(patientId);
    }
}
