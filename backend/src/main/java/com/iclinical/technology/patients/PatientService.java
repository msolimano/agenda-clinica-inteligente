package com.iclinical.technology.patients;

import com.iclinical.technology.patients.dto.PatientCreateRequest;
import com.iclinical.technology.patients.dto.PatientResponse;
import com.iclinical.technology.patients.dto.PatientStatusRequest;
import com.iclinical.technology.patients.dto.PatientSummaryResponse;
import com.iclinical.technology.patients.dto.PatientUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PatientService {

    private static final Set<String> ALLOWED_SEX_VALUES = Set.of("female", "male", "other", "unknown");

    private final PatientRepository patientRepository;
    private final PatientDefaultOrganizationProvider defaultOrganizationProvider;

    public PatientService(PatientRepository patientRepository, PatientDefaultOrganizationProvider defaultOrganizationProvider) {
        this.patientRepository = patientRepository;
        this.defaultOrganizationProvider = defaultOrganizationProvider;
    }

    @Transactional(readOnly = true)
    public List<PatientSummaryResponse> list(String search, boolean includeInactive) {
        var normalizedSearch = StringUtils.hasText(search) ? search.trim().toLowerCase() : null;
        return patientRepository.findByStatusNotOrderByLastNameAscFirstNameAsc("deleted")
            .stream()
            .filter(patient -> includeInactive || "active".equals(patient.getStatus()))
            .filter(patient -> matchesSearch(patient, normalizedSearch))
            .map(this::toSummaryResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public PatientResponse getById(UUID id) {
        return toResponse(findPatient(id));
    }

    @Transactional
    public PatientResponse create(PatientCreateRequest request) {
        var organizationId = defaultOrganizationProvider.resolve(request.organizationId());
        validateDuplicateDocument(organizationId, request.documentType(), request.documentNumber(), null);

        var patient = new Patient();
        patient.setOrganizationId(organizationId);
        patient.setUserId(request.userId());
        applyBasicData(
            patient,
            request.documentType(),
            request.documentNumber(),
            request.firstName(),
            request.lastName(),
            request.birthDate(),
            request.sex(),
            request.email(),
            request.phone(),
            request.address(),
            request.emergencyContactName(),
            request.emergencyContactPhone(),
            request.emergencyContactRelationship()
        );

        return toResponse(patientRepository.save(patient));
    }

    @Transactional
    public PatientResponse update(UUID id, PatientUpdateRequest request) {
        var patient = findPatient(id);
        validateDuplicateDocument(patient.getOrganizationId(), request.documentType(), request.documentNumber(), id);
        patient.setUserId(request.userId());
        applyBasicData(
            patient,
            request.documentType(),
            request.documentNumber(),
            request.firstName(),
            request.lastName(),
            request.birthDate(),
            request.sex(),
            request.email(),
            request.phone(),
            request.address(),
            request.emergencyContactName(),
            request.emergencyContactPhone(),
            request.emergencyContactRelationship()
        );
        return toResponse(patient);
    }

    @Transactional
    public PatientResponse updateStatus(UUID id, PatientStatusRequest request) {
        var patient = findPatient(id);
        patient.setStatus(resolveStatus(request));
        return toResponse(patient);
    }

    private Patient findPatient(UUID id) {
        return patientRepository.findById(id)
            .filter(patient -> !"deleted".equals(patient.getStatus()))
            .orElseThrow(() -> new PatientNotFoundException("Paciente no encontrado"));
    }

    private boolean matchesSearch(Patient patient, String search) {
        if (search == null) {
            return true;
        }

        return contains(patient.getFirstName(), search)
            || contains(patient.getLastName(), search)
            || contains(patient.getDocumentNumber(), search)
            || contains(patient.getEmail(), search)
            || contains(patient.getPhone(), search);
    }

    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase().contains(search);
    }

    private void validateDuplicateDocument(UUID organizationId, String documentType, String documentNumber, UUID excludedId) {
        if (StringUtils.hasText(documentType) && StringUtils.hasText(documentNumber)
            && patientRepository.existsDuplicateDocument(organizationId, documentType.trim(), documentNumber.trim(), excludedId)) {
            throw new PatientValidationException("Ya existe un paciente con el mismo documento");
        }
    }

    private void applyBasicData(
        Patient patient,
        String documentType,
        String documentNumber,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String sex,
        String email,
        String phone,
        String address,
        String emergencyContactName,
        String emergencyContactPhone,
        String emergencyContactRelationship
    ) {
        validateBirthDate(birthDate);
        patient.setDocumentType(clean(documentType));
        patient.setDocumentNumber(clean(documentNumber));
        patient.setFirstName(cleanRequired(firstName));
        patient.setLastName(cleanRequired(lastName));
        patient.setBirthDate(birthDate);
        patient.setSex(cleanSex(sex));
        patient.setEmail(clean(email));
        patient.setPhone(clean(phone));
        patient.setAddress(clean(address));
        patient.setEmergencyContactName(clean(emergencyContactName));
        patient.setEmergencyContactPhone(clean(emergencyContactPhone));
        patient.setEmergencyContactRelationship(clean(emergencyContactRelationship));
    }

    private void validateBirthDate(LocalDate birthDate) {
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new PatientValidationException("La fecha de nacimiento no puede ser futura");
        }
    }

    private String cleanSex(String value) {
        var cleaned = clean(value);
        if (cleaned == null) {
            return null;
        }

        var normalized = cleaned.toLowerCase();
        if (!ALLOWED_SEX_VALUES.contains(normalized)) {
            throw new PatientValidationException("Sexo permitido: female, male, other o unknown");
        }
        return normalized;
    }

    private String resolveStatus(PatientStatusRequest request) {
        if (request.active() != null) {
            return request.active() ? "active" : "inactive";
        }
        if ("active".equals(request.status()) || "inactive".equals(request.status())) {
            return request.status();
        }
        throw new PatientValidationException("El estado permitido es active o inactive");
    }

    private PatientSummaryResponse toSummaryResponse(Patient patient) {
        return new PatientSummaryResponse(
            patient.getId(),
            patient.getDocumentType(),
            patient.getDocumentNumber(),
            patient.getFirstName(),
            patient.getLastName(),
            patient.getBirthDate(),
            patient.getSex(),
            patient.getEmail(),
            patient.getPhone(),
            patient.getStatus()
        );
    }

    private PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
            patient.getId(),
            patient.getOrganizationId(),
            patient.getUserId(),
            patient.getDocumentType(),
            patient.getDocumentNumber(),
            patient.getFirstName(),
            patient.getLastName(),
            patient.getBirthDate(),
            patient.getSex(),
            patient.getEmail(),
            patient.getPhone(),
            patient.getAddress(),
            patient.getEmergencyContactName(),
            patient.getEmergencyContactPhone(),
            patient.getEmergencyContactRelationship(),
            patient.getStatus(),
            patient.getCreatedAt(),
            patient.getUpdatedAt()
        );
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String cleanRequired(String value) {
        if (!StringUtils.hasText(value)) {
            throw new PatientValidationException("Nombre y apellido son obligatorios");
        }
        return value.trim();
    }
}
