package com.iclinical.technology.patients;

import com.iclinical.technology.patients.dto.PatientCreateRequest;
import com.iclinical.technology.patients.dto.PatientResponse;
import com.iclinical.technology.patients.dto.PatientStatusRequest;
import com.iclinical.technology.patients.dto.PatientSummaryResponse;
import com.iclinical.technology.patients.dto.PatientUpdateRequest;
import jakarta.validation.Valid;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public List<PatientSummaryResponse> listPatients(
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return patientService.list(search, includeInactive);
    }

    @GetMapping("/{id}")
    public PatientResponse getPatient(@PathVariable UUID id) {
        return patientService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponse createPatient(@Valid @RequestBody PatientCreateRequest request) {
        return patientService.create(request);
    }

    @PutMapping("/{id}")
    public PatientResponse updatePatient(@PathVariable UUID id, @Valid @RequestBody PatientUpdateRequest request) {
        return patientService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public PatientResponse updatePatientStatus(@PathVariable UUID id, @RequestBody PatientStatusRequest request) {
        return patientService.updateStatus(id, request);
    }
}
