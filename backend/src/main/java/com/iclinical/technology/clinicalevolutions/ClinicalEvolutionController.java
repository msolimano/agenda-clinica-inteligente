package com.iclinical.technology.clinicalevolutions;

import com.iclinical.technology.clinicalevolutions.dto.ClinicalEvolutionCreateRequest;
import com.iclinical.technology.clinicalevolutions.dto.ClinicalEvolutionResponse;
import com.iclinical.technology.clinicalevolutions.dto.ClinicalEvolutionStatusRequest;
import com.iclinical.technology.clinicalevolutions.dto.ClinicalEvolutionUpdateRequest;
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
public class ClinicalEvolutionController {

    private final ClinicalEvolutionService evolutionService;

    public ClinicalEvolutionController(ClinicalEvolutionService evolutionService) {
        this.evolutionService = evolutionService;
    }

    @GetMapping("/clinical-records/{recordId}/evolutions")
    public List<ClinicalEvolutionResponse> listByRecord(@PathVariable UUID recordId) {
        return evolutionService.listByRecord(recordId);
    }

    @GetMapping("/clinical-evolutions/{id}")
    public ClinicalEvolutionResponse getById(@PathVariable UUID id) {
        return evolutionService.getById(id);
    }

    @PostMapping("/clinical-records/{recordId}/evolutions")
    @ResponseStatus(HttpStatus.CREATED)
    public ClinicalEvolutionResponse create(@PathVariable UUID recordId, @RequestBody ClinicalEvolutionCreateRequest request) {
        return evolutionService.create(recordId, request);
    }

    @PutMapping("/clinical-evolutions/{id}")
    public ClinicalEvolutionResponse update(@PathVariable UUID id, @RequestBody ClinicalEvolutionUpdateRequest request) {
        return evolutionService.update(id, request);
    }

    @PatchMapping("/clinical-evolutions/{id}/status")
    public ClinicalEvolutionResponse updateStatus(@PathVariable UUID id, @RequestBody ClinicalEvolutionStatusRequest request) {
        return evolutionService.updateStatus(id, request);
    }

    @GetMapping("/patients/{patientId}/evolutions")
    public List<ClinicalEvolutionResponse> listByPatient(@PathVariable UUID patientId) {
        return evolutionService.listByPatient(patientId);
    }
}
