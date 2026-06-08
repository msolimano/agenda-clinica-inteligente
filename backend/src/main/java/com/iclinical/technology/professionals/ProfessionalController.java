package com.iclinical.technology.professionals;

import com.iclinical.technology.professionals.dto.ProfessionalCreateRequest;
import com.iclinical.technology.professionals.dto.ProfessionalResponse;
import com.iclinical.technology.professionals.dto.ProfessionalStatusRequest;
import com.iclinical.technology.professionals.dto.ProfessionalUpdateRequest;
import com.iclinical.technology.professionals.dto.SpecialtyAssignmentRequest;
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
@RequestMapping("/api/professionals")
public class ProfessionalController {

    private final ProfessionalService professionalService;

    public ProfessionalController(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }

    @GetMapping
    public List<ProfessionalResponse> listProfessionals(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) UUID specialtyId,
        @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return professionalService.list(search, specialtyId, includeInactive);
    }

    @GetMapping("/{id}")
    public ProfessionalResponse getProfessional(@PathVariable UUID id) {
        return professionalService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfessionalResponse createProfessional(@Valid @RequestBody ProfessionalCreateRequest request) {
        return professionalService.create(request);
    }

    @PutMapping("/{id}")
    public ProfessionalResponse updateProfessional(@PathVariable UUID id, @Valid @RequestBody ProfessionalUpdateRequest request) {
        return professionalService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public ProfessionalResponse updateProfessionalStatus(@PathVariable UUID id, @RequestBody ProfessionalStatusRequest request) {
        return professionalService.updateStatus(id, request);
    }

    @PostMapping("/{id}/specialties")
    public ProfessionalResponse associateSpecialty(@PathVariable UUID id, @Valid @RequestBody SpecialtyAssignmentRequest request) {
        return professionalService.associateSpecialty(id, request);
    }
}
