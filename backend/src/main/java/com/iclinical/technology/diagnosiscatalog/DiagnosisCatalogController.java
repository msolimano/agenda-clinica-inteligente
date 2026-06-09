package com.iclinical.technology.diagnosiscatalog;

import com.iclinical.technology.diagnosiscatalog.dto.DiagnosisCatalogRequest;
import com.iclinical.technology.diagnosiscatalog.dto.DiagnosisCatalogResponse;
import com.iclinical.technology.diagnosiscatalog.dto.DiagnosisCatalogStatusRequest;
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
@RequestMapping("/api/diagnosis-catalog")
public class DiagnosisCatalogController {

    private final DiagnosisCatalogService diagnosisCatalogService;

    public DiagnosisCatalogController(DiagnosisCatalogService diagnosisCatalogService) {
        this.diagnosisCatalogService = diagnosisCatalogService;
    }

    @GetMapping
    public List<DiagnosisCatalogResponse> list(
        @RequestParam(required = false) UUID organizationId,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return diagnosisCatalogService.list(organizationId, search, includeInactive);
    }

    @GetMapping("/search")
    public List<DiagnosisCatalogResponse> search(
        @RequestParam(required = false) UUID organizationId,
        @RequestParam(required = false, name = "q") String query
    ) {
        return diagnosisCatalogService.search(organizationId, query);
    }

    @GetMapping("/{id}")
    public DiagnosisCatalogResponse getById(@PathVariable UUID id) {
        return diagnosisCatalogService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiagnosisCatalogResponse create(@RequestBody DiagnosisCatalogRequest request) {
        return diagnosisCatalogService.create(request);
    }

    @PutMapping("/{id}")
    public DiagnosisCatalogResponse update(@PathVariable UUID id, @RequestBody DiagnosisCatalogRequest request) {
        return diagnosisCatalogService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public DiagnosisCatalogResponse updateStatus(@PathVariable UUID id, @RequestBody DiagnosisCatalogStatusRequest request) {
        return diagnosisCatalogService.updateStatus(id, request);
    }
}
