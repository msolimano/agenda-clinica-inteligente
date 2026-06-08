package com.iclinical.technology.clinicalrecords;

import com.iclinical.technology.clinicalrecords.dto.ClinicalRecordCreateRequest;
import com.iclinical.technology.clinicalrecords.dto.ClinicalRecordResponse;
import com.iclinical.technology.clinicalrecords.dto.ClinicalRecordStatusRequest;
import com.iclinical.technology.clinicalrecords.dto.ClinicalRecordSummaryResponse;
import com.iclinical.technology.clinicalrecords.dto.ClinicalRecordUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ClinicalRecordController {

    private final ClinicalRecordService recordService;

    public ClinicalRecordController(ClinicalRecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/clinical-records")
    public List<ClinicalRecordSummaryResponse> list(
        @RequestParam(required = false) UUID patientId,
        @RequestParam(required = false) UUID professionalId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to
    ) {
        return recordService.list(patientId, professionalId, status, from, to);
    }

    @GetMapping("/clinical-records/{id}")
    public ClinicalRecordResponse getById(@PathVariable UUID id) {
        return recordService.getById(id);
    }

    @GetMapping("/patients/{id}/clinical-records")
    public List<ClinicalRecordSummaryResponse> listByPatient(@PathVariable UUID id) {
        return recordService.listByPatient(id);
    }

    @PostMapping("/clinical-records")
    @ResponseStatus(HttpStatus.CREATED)
    public ClinicalRecordResponse create(@RequestBody ClinicalRecordCreateRequest request) {
        return recordService.create(request);
    }

    @PutMapping("/clinical-records/{id}")
    public ClinicalRecordResponse update(@PathVariable UUID id, @RequestBody ClinicalRecordUpdateRequest request) {
        return recordService.update(id, request);
    }

    @PatchMapping("/clinical-records/{id}/status")
    public ClinicalRecordResponse updateStatus(@PathVariable UUID id, @RequestBody ClinicalRecordStatusRequest request) {
        return recordService.updateStatus(id, request);
    }
}
