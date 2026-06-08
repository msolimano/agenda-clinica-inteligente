package com.iclinical.technology.consents;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AIConsentRepository extends JpaRepository<AIConsent, UUID> {

    Optional<AIConsent> findFirstByOrganizationIdAndPatientIdAndConsentTypeAndGrantedTrueAndStatusOrderByGrantedAtDescCreatedAtDesc(
        UUID organizationId,
        UUID patientId,
        String consentType,
        String status
    );

    Optional<AIConsent> findFirstByOrganizationIdAndPatientIdAndConsentTypeAndStatusNotOrderByCreatedAtDesc(
        UUID organizationId,
        UUID patientId,
        String consentType,
        String status
    );
}
