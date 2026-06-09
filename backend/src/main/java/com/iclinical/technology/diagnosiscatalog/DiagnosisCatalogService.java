package com.iclinical.technology.diagnosiscatalog;

import com.iclinical.technology.diagnosiscatalog.dto.DiagnosisCatalogRequest;
import com.iclinical.technology.diagnosiscatalog.dto.DiagnosisCatalogResponse;
import com.iclinical.technology.diagnosiscatalog.dto.DiagnosisCatalogStatusRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class DiagnosisCatalogService {

    private static final Set<String> STATUSES = Set.of("active", "inactive");

    private final DiagnosisCatalogRepository catalogRepository;

    public DiagnosisCatalogService(DiagnosisCatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @Transactional(readOnly = true)
    public List<DiagnosisCatalogResponse> list(UUID organizationId, String search, boolean includeInactive) {
        return catalogRepository.findFiltered(organizationId, normalizeSearch(search), includeInactive)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<DiagnosisCatalogResponse> search(UUID organizationId, String query) {
        return catalogRepository.findFiltered(organizationId, normalizeSearch(query), false)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public DiagnosisCatalogResponse getById(UUID id) {
        return toResponse(findDiagnosis(id));
    }

    @Transactional
    public DiagnosisCatalogResponse create(DiagnosisCatalogRequest request) {
        var diagnosis = new DiagnosisCatalog();
        applyFields(diagnosis, request, "active");
        return toResponse(catalogRepository.save(diagnosis));
    }

    @Transactional
    public DiagnosisCatalogResponse update(UUID id, DiagnosisCatalogRequest request) {
        var diagnosis = findDiagnosis(id);
        applyFields(diagnosis, request, diagnosis.getStatus());
        return toResponse(diagnosis);
    }

    @Transactional
    public DiagnosisCatalogResponse updateStatus(UUID id, DiagnosisCatalogStatusRequest request) {
        var diagnosis = findDiagnosis(id);
        diagnosis.setStatus(resolveStatus(request));
        return toResponse(diagnosis);
    }

    private void applyFields(DiagnosisCatalog diagnosis, DiagnosisCatalogRequest request, String fallbackStatus) {
        if (request == null) {
            throw new DiagnosisCatalogValidationException("Debe indicar los datos del diagnostico");
        }
        diagnosis.setOrganizationId(request.organizationId());
        diagnosis.setDiagnosisCode(clean(request.diagnosisCode()));
        diagnosis.setCodeSystem(clean(request.codeSystem()));
        diagnosis.setDiagnosisDisplay(requireDisplay(request.diagnosisDisplay()));
        diagnosis.setCategory(clean(request.category()));
        diagnosis.setDescription(clean(request.description()));
        diagnosis.setStatus(resolveStatus(request.status(), fallbackStatus));
    }

    private DiagnosisCatalog findDiagnosis(UUID id) {
        return catalogRepository.findById(id)
            .filter(diagnosis -> !"deleted".equals(diagnosis.getStatus()))
            .orElseThrow(() -> new DiagnosisCatalogNotFoundException("Diagnostico de catalogo no encontrado"));
    }

    private String resolveStatus(DiagnosisCatalogStatusRequest request) {
        if (request == null) {
            throw new DiagnosisCatalogValidationException("Debe indicar el estado del diagnostico");
        }
        if (request.active() != null) {
            return request.active() ? "active" : "inactive";
        }
        return resolveStatus(request.status(), null);
    }

    private String resolveStatus(String value, String fallback) {
        var status = clean(value);
        if (!StringUtils.hasText(status)) {
            if (fallback != null) {
                return fallback;
            }
            throw new DiagnosisCatalogValidationException("Debe indicar el estado del diagnostico");
        }
        if (!STATUSES.contains(status)) {
            throw new DiagnosisCatalogValidationException("Estado permitido: active o inactive");
        }
        return status;
    }

    private String requireDisplay(String value) {
        var display = clean(value);
        if (!StringUtils.hasText(display)) {
            throw new DiagnosisCatalogValidationException("Debe indicar el texto del diagnostico");
        }
        if (display.length() > 255) {
            throw new DiagnosisCatalogValidationException("El texto del diagnostico no puede superar 255 caracteres");
        }
        return display;
    }

    private String normalizeSearch(String search) {
        return StringUtils.hasText(search) ? search.trim().toLowerCase() : null;
    }

    private DiagnosisCatalogResponse toResponse(DiagnosisCatalog diagnosis) {
        return new DiagnosisCatalogResponse(
            diagnosis.getId(),
            diagnosis.getOrganizationId(),
            diagnosis.getDiagnosisCode(),
            diagnosis.getCodeSystem(),
            diagnosis.getDiagnosisDisplay(),
            diagnosis.getCategory(),
            diagnosis.getDescription(),
            diagnosis.getStatus(),
            diagnosis.getCreatedAt(),
            diagnosis.getUpdatedAt()
        );
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
