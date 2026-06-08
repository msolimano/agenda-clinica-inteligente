package com.iclinical.technology.consents;

import com.iclinical.technology.consents.dto.AIConsentAcceptRequest;
import com.iclinical.technology.consents.dto.AIConsentResponse;
import com.iclinical.technology.consents.dto.AIConsentRevokeRequest;
import com.iclinical.technology.patients.Patient;
import com.iclinical.technology.patients.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class AIConsentService {

    public static final String AI_CONSENT_TYPE = "ai_analysis";
    public static final String INITIAL_VERSION = "IA-CONSENT-V1";

    private final AIConsentRepository consentRepository;
    private final PatientRepository patientRepository;

    public AIConsentService(AIConsentRepository consentRepository, PatientRepository patientRepository) {
        this.consentRepository = consentRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public AIConsentResponse getCurrent(UUID patientId) {
        var patient = findPatient(patientId);
        return consentRepository.findFirstByOrganizationIdAndPatientIdAndConsentTypeAndStatusNotOrderByCreatedAtDesc(patient.getOrganizationId(), patient.getId(), AI_CONSENT_TYPE, "deleted")
            .map(this::toResponse)
            .orElseGet(() -> emptyResponse(patient));
    }

    @Transactional
    public AIConsentResponse accept(UUID patientId, AIConsentAcceptRequest request) {
        var patient = findPatient(patientId);
        var active = consentRepository.findFirstByOrganizationIdAndPatientIdAndConsentTypeAndGrantedTrueAndStatusOrderByGrantedAtDescCreatedAtDesc(patient.getOrganizationId(), patient.getId(), AI_CONSENT_TYPE, "active");
        if (active.isPresent()) {
            return toResponse(active.get());
        }

        var consent = new AIConsent();
        consent.setOrganizationId(patient.getOrganizationId());
        consent.setPatientId(patient.getId());
        consent.setConsentType(AI_CONSENT_TYPE);
        consent.setGranted(true);
        consent.setConsentVersion(INITIAL_VERSION);
        consent.setGrantedAt(Instant.now());
        consent.setRevokedAt(null);
        consent.setSource(clean(request == null ? null : request.source(), "documents"));
        consent.setNotes(clean(request == null ? null : request.notes(), null));
        consent.setStatus("active");

        return toResponse(consentRepository.save(consent));
    }

    @Transactional
    public AIConsentResponse revoke(UUID patientId, AIConsentRevokeRequest request) {
        var patient = findPatient(patientId);
        var consent = consentRepository.findFirstByOrganizationIdAndPatientIdAndConsentTypeAndGrantedTrueAndStatusOrderByGrantedAtDescCreatedAtDesc(patient.getOrganizationId(), patient.getId(), AI_CONSENT_TYPE, "active")
            .orElseThrow(() -> new AIConsentValidationException("No existe consentimiento IA activo para revocar"));

        consent.setGranted(false);
        consent.setRevokedAt(Instant.now());
        consent.setSource(clean(request == null ? null : request.source(), consent.getSource()));
        consent.setNotes(clean(request == null ? null : request.notes(), consent.getNotes()));
        consent.setStatus("revoked");

        return toResponse(consent);
    }

    @Transactional(readOnly = true)
    public Optional<AIConsent> findActiveConsent(UUID organizationId, UUID patientId) {
        if (organizationId == null || patientId == null) {
            return Optional.empty();
        }
        return consentRepository.findFirstByOrganizationIdAndPatientIdAndConsentTypeAndGrantedTrueAndStatusOrderByGrantedAtDescCreatedAtDesc(organizationId, patientId, AI_CONSENT_TYPE, "active");
    }

    private Patient findPatient(UUID patientId) {
        if (patientId == null) {
            throw new AIConsentValidationException("Debe indicar el paciente");
        }
        return patientRepository.findById(patientId)
            .filter(patient -> !"deleted".equals(patient.getStatus()))
            .orElseThrow(() -> new AIConsentNotFoundException("Paciente no encontrado"));
    }

    private AIConsentResponse toResponse(AIConsent consent) {
        return new AIConsentResponse(
            consent.getId(),
            consent.getOrganizationId(),
            consent.getPatientId(),
            consent.getConsentType(),
            consent.isGranted() && "active".equals(consent.getStatus()),
            consent.isGranted(),
            consent.getConsentVersion(),
            consent.getGrantedAt(),
            consent.getRevokedAt(),
            consent.getSource(),
            consent.getNotes(),
            consent.getStatus(),
            consent.getCreatedAt(),
            consent.getUpdatedAt()
        );
    }

    private AIConsentResponse emptyResponse(Patient patient) {
        return new AIConsentResponse(
            null,
            patient.getOrganizationId(),
            patient.getId(),
            AI_CONSENT_TYPE,
            false,
            false,
            INITIAL_VERSION,
            null,
            null,
            null,
            null,
            "not_requested",
            null,
            null
        );
    }

    private String clean(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
