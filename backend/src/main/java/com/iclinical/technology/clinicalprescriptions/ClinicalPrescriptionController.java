package com.iclinical.technology.clinicalprescriptions;

import com.iclinical.technology.clinicalprescriptions.dto.ClinicalPrescriptionCreateRequest;
import com.iclinical.technology.clinicalprescriptions.dto.ClinicalPrescriptionResponse;
import com.iclinical.technology.clinicalprescriptions.dto.ClinicalPrescriptionStatusRequest;
import com.iclinical.technology.clinicalprescriptions.dto.ClinicalPrescriptionUpdateRequest;
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
public class ClinicalPrescriptionController {

    private final ClinicalPrescriptionService prescriptionService;

    public ClinicalPrescriptionController(ClinicalPrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @GetMapping("/clinical-records/{recordId}/prescriptions")
    public List<ClinicalPrescriptionResponse> listByRecord(@PathVariable UUID recordId) {
        return prescriptionService.listByRecord(recordId);
    }

    @GetMapping("/clinical-prescriptions/{id}")
    public ClinicalPrescriptionResponse getById(@PathVariable UUID id) {
        return prescriptionService.getById(id);
    }

    @PostMapping("/clinical-records/{recordId}/prescriptions")
    @ResponseStatus(HttpStatus.CREATED)
    public ClinicalPrescriptionResponse create(@PathVariable UUID recordId, @RequestBody ClinicalPrescriptionCreateRequest request) {
        return prescriptionService.create(recordId, request);
    }

    @PutMapping("/clinical-prescriptions/{id}")
    public ClinicalPrescriptionResponse update(@PathVariable UUID id, @RequestBody ClinicalPrescriptionUpdateRequest request) {
        return prescriptionService.update(id, request);
    }

    @PatchMapping("/clinical-prescriptions/{id}/status")
    public ClinicalPrescriptionResponse updateStatus(@PathVariable UUID id, @RequestBody ClinicalPrescriptionStatusRequest request) {
        return prescriptionService.updateStatus(id, request);
    }

    @GetMapping("/patients/{patientId}/prescriptions")
    public List<ClinicalPrescriptionResponse> listByPatient(@PathVariable UUID patientId) {
        return prescriptionService.listByPatient(patientId);
    }
}
