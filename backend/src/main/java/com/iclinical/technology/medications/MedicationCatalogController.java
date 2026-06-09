package com.iclinical.technology.medications;

import com.iclinical.technology.medications.dto.MedicationCatalogRequest;
import com.iclinical.technology.medications.dto.MedicationCatalogResponse;
import com.iclinical.technology.medications.dto.MedicationCatalogStatusRequest;
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
@RequestMapping("/api/medications")
public class MedicationCatalogController {

    private final MedicationCatalogService medicationService;

    public MedicationCatalogController(MedicationCatalogService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping
    public List<MedicationCatalogResponse> list(
        @RequestParam(required = false) UUID organizationId,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return medicationService.list(organizationId, search, includeInactive);
    }

    @GetMapping("/search")
    public List<MedicationCatalogResponse> search(
        @RequestParam(required = false) UUID organizationId,
        @RequestParam(required = false, name = "q") String query
    ) {
        return medicationService.search(organizationId, query);
    }

    @GetMapping("/{id}")
    public MedicationCatalogResponse getById(@PathVariable UUID id) {
        return medicationService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicationCatalogResponse create(@RequestBody MedicationCatalogRequest request) {
        return medicationService.create(request);
    }

    @PutMapping("/{id}")
    public MedicationCatalogResponse update(@PathVariable UUID id, @RequestBody MedicationCatalogRequest request) {
        return medicationService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public MedicationCatalogResponse updateStatus(@PathVariable UUID id, @RequestBody MedicationCatalogStatusRequest request) {
        return medicationService.updateStatus(id, request);
    }
}
