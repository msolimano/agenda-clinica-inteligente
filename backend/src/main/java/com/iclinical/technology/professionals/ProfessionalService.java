package com.iclinical.technology.professionals;

import com.iclinical.technology.professionals.dto.ProfessionalCreateRequest;
import com.iclinical.technology.professionals.dto.ProfessionalResponse;
import com.iclinical.technology.professionals.dto.ProfessionalSpecialtyResponse;
import com.iclinical.technology.professionals.dto.ProfessionalStatusRequest;
import com.iclinical.technology.professionals.dto.ProfessionalUpdateRequest;
import com.iclinical.technology.professionals.dto.SpecialtyAssignmentRequest;
import com.iclinical.technology.specialties.SpecialtyRepository;
import com.iclinical.technology.specialties.SpecialtyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;
    private final ProfessionalSpecialtyRepository professionalSpecialtyRepository;
    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyService specialtyService;
    private final DefaultOrganizationProvider defaultOrganizationProvider;

    public ProfessionalService(
        ProfessionalRepository professionalRepository,
        ProfessionalSpecialtyRepository professionalSpecialtyRepository,
        SpecialtyRepository specialtyRepository,
        SpecialtyService specialtyService,
        DefaultOrganizationProvider defaultOrganizationProvider
    ) {
        this.professionalRepository = professionalRepository;
        this.professionalSpecialtyRepository = professionalSpecialtyRepository;
        this.specialtyRepository = specialtyRepository;
        this.specialtyService = specialtyService;
        this.defaultOrganizationProvider = defaultOrganizationProvider;
    }

    @Transactional(readOnly = true)
    public List<ProfessionalResponse> list(String search, UUID specialtyId, boolean includeInactive) {
        var normalizedSearch = StringUtils.hasText(search) ? search.trim().toLowerCase() : null;
        return professionalRepository.findByStatusNotOrderByLastNameAscFirstNameAsc("deleted")
            .stream()
            .filter(professional -> includeInactive || "active".equals(professional.getStatus()))
            .filter(professional -> matchesSearch(professional, normalizedSearch))
            .filter(professional -> matchesSpecialty(professional, specialtyId))
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ProfessionalResponse getById(UUID id) {
        return toResponse(findProfessional(id));
    }

    @Transactional
    public ProfessionalResponse create(ProfessionalCreateRequest request) {
        var organizationId = defaultOrganizationProvider.resolve(request.organizationId());
        validateDuplicateKeys(organizationId, request.documentType(), request.documentNumber(), request.registryNumber(), null);
        var assignments = normalizeAssignments(request.specialties());

        var professional = new Professional();
        professional.setOrganizationId(organizationId);
        professional.setUserId(request.userId());
        applyBasicData(professional, request.documentType(), request.documentNumber(), request.registryNumber(), request.firstName(), request.lastName(), request.email(), request.phone());
        professionalRepository.save(professional);

        for (var assignment : assignments) {
            assignSpecialty(professional, assignment.specialtyId(), Boolean.TRUE.equals(assignment.primary()));
        }

        return toResponse(professionalRepository.findById(professional.getId()).orElseThrow());
    }

    @Transactional
    public ProfessionalResponse update(UUID id, ProfessionalUpdateRequest request) {
        var professional = findProfessional(id);
        validateDuplicateKeys(professional.getOrganizationId(), request.documentType(), request.documentNumber(), request.registryNumber(), id);
        professional.setUserId(request.userId());
        applyBasicData(professional, request.documentType(), request.documentNumber(), request.registryNumber(), request.firstName(), request.lastName(), request.email(), request.phone());
        return toResponse(professional);
    }

    @Transactional
    public ProfessionalResponse updateStatus(UUID id, ProfessionalStatusRequest request) {
        var professional = findProfessional(id);
        professional.setStatus(resolveStatus(request));
        return toResponse(professional);
    }

    @Transactional
    public ProfessionalResponse associateSpecialty(UUID professionalId, SpecialtyAssignmentRequest request) {
        var professional = findProfessional(professionalId);
        assignSpecialty(professional, request.specialtyId(), Boolean.TRUE.equals(request.primary()));
        return toResponse(professionalRepository.findById(professionalId).orElseThrow());
    }

    private void assignSpecialty(Professional professional, UUID specialtyId, boolean primaryRequested) {
        var specialty = specialtyRepository.findById(specialtyId)
            .filter(item -> !"deleted".equals(item.getStatus()))
            .orElseThrow(() -> new ProfessionalValidationException("La especialidad indicada no existe o no esta activa"));

        var activeAssignments = professionalSpecialtyRepository.findByProfessionalIdAndStatus(professional.getId(), "active");
        var shouldBePrimary = primaryRequested || activeAssignments.stream().noneMatch(ProfessionalSpecialty::isPrimary);

        if (shouldBePrimary) {
            activeAssignments.forEach(item -> item.setPrimary(false));
        }

        var assignment = professionalSpecialtyRepository.findByProfessionalIdAndSpecialtyId(professional.getId(), specialty.getId())
            .orElseGet(() -> new ProfessionalSpecialty(professional, specialty));
        assignment.setStatus("active");
        assignment.setPrimary(shouldBePrimary);
        professionalSpecialtyRepository.save(assignment);
        professional.getSpecialties().add(assignment);
    }

    private boolean matchesSearch(Professional professional, String search) {
        if (search == null) {
            return true;
        }

        return contains(professional.getFirstName(), search)
            || contains(professional.getLastName(), search)
            || contains(professional.getRegistryNumber(), search)
            || contains(professional.getDocumentNumber(), search);
    }

    private boolean matchesSpecialty(Professional professional, UUID specialtyId) {
        if (specialtyId == null) {
            return true;
        }

        return professional.getSpecialties()
            .stream()
            .anyMatch(item -> "active".equals(item.getStatus()) && specialtyId.equals(item.getSpecialty().getId()));
    }

    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase().contains(search);
    }

    private Professional findProfessional(UUID id) {
        return professionalRepository.findById(id)
            .filter(professional -> !"deleted".equals(professional.getStatus()))
            .orElseThrow(() -> new ProfessionalNotFoundException("Profesional no encontrado"));
    }

    private void applyBasicData(Professional professional, String documentType, String documentNumber, String registryNumber, String firstName, String lastName, String email, String phone) {
        professional.setDocumentType(clean(documentType));
        professional.setDocumentNumber(clean(documentNumber));
        professional.setRegistryNumber(clean(registryNumber));
        professional.setFirstName(cleanRequired(firstName));
        professional.setLastName(cleanRequired(lastName));
        professional.setEmail(clean(email));
        professional.setPhone(clean(phone));
    }

    private void validateDuplicateKeys(UUID organizationId, String documentType, String documentNumber, String registryNumber, UUID excludedId) {
        if (StringUtils.hasText(documentType) && StringUtils.hasText(documentNumber)
            && professionalRepository.existsDuplicateDocument(organizationId, documentType.trim(), documentNumber.trim(), excludedId)) {
            throw new ProfessionalValidationException("Ya existe un profesional con el mismo documento");
        }

        if (StringUtils.hasText(registryNumber)
            && professionalRepository.existsDuplicateRegistry(organizationId, registryNumber.trim(), excludedId)) {
            throw new ProfessionalValidationException("Ya existe un profesional con el mismo numero de registro");
        }
    }

    private List<SpecialtyAssignmentRequest> normalizeAssignments(List<SpecialtyAssignmentRequest> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            specialtyService.ensureDefaultCatalog();
            var defaultSpecialty = specialtyRepository.findByStatusOrderByNameAsc("active")
                .stream()
                .findFirst()
                .orElseThrow(() -> new ProfessionalValidationException("Debe existir al menos una especialidad para registrar un profesional"));
            return List.of(new SpecialtyAssignmentRequest(defaultSpecialty.getId(), true));
        }

        var primaryCount = assignments.stream().filter(item -> Boolean.TRUE.equals(item.primary())).count();
        if (primaryCount > 1) {
            throw new ProfessionalValidationException("Solo una especialidad puede marcarse como principal");
        }

        if (primaryCount == 0) {
            var first = assignments.get(0);
            return assignments.stream()
                .map(item -> item == first ? new SpecialtyAssignmentRequest(item.specialtyId(), true) : item)
                .toList();
        }

        return assignments;
    }

    private String resolveStatus(ProfessionalStatusRequest request) {
        if (request.active() != null) {
            return request.active() ? "active" : "inactive";
        }
        if ("active".equals(request.status()) || "inactive".equals(request.status())) {
            return request.status();
        }
        throw new ProfessionalValidationException("El estado permitido es active o inactive");
    }

    private ProfessionalResponse toResponse(Professional professional) {
        var specialties = professional.getSpecialties()
            .stream()
            .filter(item -> !"deleted".equals(item.getStatus()))
            .sorted(Comparator.comparing(ProfessionalSpecialty::isPrimary).reversed().thenComparing(item -> item.getSpecialty().getName()))
            .map(this::toSpecialtyResponse)
            .toList();

        return new ProfessionalResponse(
            professional.getId(),
            professional.getOrganizationId(),
            professional.getUserId(),
            professional.getDocumentType(),
            professional.getDocumentNumber(),
            professional.getRegistryNumber(),
            professional.getFirstName(),
            professional.getLastName(),
            professional.getEmail(),
            professional.getPhone(),
            professional.getStatus(),
            specialties,
            professional.getCreatedAt(),
            professional.getUpdatedAt()
        );
    }

    private ProfessionalSpecialtyResponse toSpecialtyResponse(ProfessionalSpecialty assignment) {
        var specialty = assignment.getSpecialty();
        return new ProfessionalSpecialtyResponse(assignment.getId(), specialty.getId(), specialty.getName(), specialty.getCode(), assignment.isPrimary(), assignment.getStatus());
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String cleanRequired(String value) {
        if (!StringUtils.hasText(value)) {
            throw new ProfessionalValidationException("Nombre y apellido son obligatorios");
        }
        return value.trim();
    }
}
