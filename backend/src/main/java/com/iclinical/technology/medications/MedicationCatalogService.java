package com.iclinical.technology.medications;

import com.iclinical.technology.medications.dto.MedicationCatalogRequest;
import com.iclinical.technology.medications.dto.MedicationCatalogResponse;
import com.iclinical.technology.medications.dto.MedicationCatalogStatusRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class MedicationCatalogService {

    private static final Set<String> STATUSES = Set.of("active", "inactive");

    private final MedicationCatalogRepository medicationRepository;

    public MedicationCatalogService(MedicationCatalogRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    @Transactional(readOnly = true)
    public List<MedicationCatalogResponse> list(UUID organizationId, String search, boolean includeInactive) {
        return medicationRepository.findFiltered(organizationId, normalizeSearch(search), includeInactive)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MedicationCatalogResponse> search(UUID organizationId, String search) {
        return medicationRepository.findFiltered(organizationId, normalizeSearch(search), false)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public MedicationCatalogResponse getById(UUID id) {
        return toResponse(findMedication(id));
    }

    @Transactional
    public MedicationCatalogResponse create(MedicationCatalogRequest request) {
        var medication = new MedicationCatalog();
        applyFields(medication, request, "active");
        return toResponse(medicationRepository.save(medication));
    }

    @Transactional
    public MedicationCatalogResponse update(UUID id, MedicationCatalogRequest request) {
        var medication = findMedication(id);
        applyFields(medication, request, medication.getStatus());
        return toResponse(medication);
    }

    @Transactional
    public MedicationCatalogResponse updateStatus(UUID id, MedicationCatalogStatusRequest request) {
        var medication = findMedication(id);
        medication.setStatus(resolveStatus(request));
        return toResponse(medication);
    }

    private void applyFields(MedicationCatalog medication, MedicationCatalogRequest request, String fallbackStatus) {
        if (request == null) {
            throw new MedicationCatalogValidationException("Debe indicar los datos del medicamento");
        }
        medication.setOrganizationId(request.organizationId());
        medication.setMedicationCode(clean(request.medicationCode()));
        medication.setMedicationCodeSystem(clean(request.medicationCodeSystem()));
        medication.setMedicationName(requireName(request.medicationName()));
        medication.setActiveIngredient(clean(request.activeIngredient()));
        medication.setPresentation(clean(request.presentation()));
        medication.setStrength(clean(request.strength()));
        medication.setPharmaceuticalForm(clean(request.pharmaceuticalForm()));
        medication.setRoute(clean(request.route()));
        medication.setManufacturer(clean(request.manufacturer()));
        medication.setStatus(resolveStatus(request.status(), fallbackStatus));
    }

    private MedicationCatalog findMedication(UUID id) {
        return medicationRepository.findById(id)
            .filter(medication -> !"deleted".equals(medication.getStatus()))
            .orElseThrow(() -> new MedicationCatalogNotFoundException("Medicamento no encontrado"));
    }

    private String resolveStatus(MedicationCatalogStatusRequest request) {
        if (request == null) {
            throw new MedicationCatalogValidationException("Debe indicar el estado del medicamento");
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
            throw new MedicationCatalogValidationException("Debe indicar el estado del medicamento");
        }
        if (!STATUSES.contains(status)) {
            throw new MedicationCatalogValidationException("Estado permitido: active o inactive");
        }
        return status;
    }

    private String requireName(String value) {
        var name = clean(value);
        if (!StringUtils.hasText(name)) {
            throw new MedicationCatalogValidationException("Debe indicar el nombre del medicamento");
        }
        if (name.length() > 255) {
            throw new MedicationCatalogValidationException("El nombre del medicamento no puede superar 255 caracteres");
        }
        return name;
    }

    private String normalizeSearch(String search) {
        return StringUtils.hasText(search) ? search.trim().toLowerCase() : null;
    }

    private MedicationCatalogResponse toResponse(MedicationCatalog medication) {
        return new MedicationCatalogResponse(
            medication.getId(),
            medication.getOrganizationId(),
            medication.getMedicationCode(),
            medication.getMedicationCodeSystem(),
            medication.getMedicationName(),
            medication.getActiveIngredient(),
            medication.getPresentation(),
            medication.getStrength(),
            medication.getPharmaceuticalForm(),
            medication.getRoute(),
            medication.getManufacturer(),
            medication.getStatus(),
            medication.getCreatedAt(),
            medication.getUpdatedAt()
        );
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
