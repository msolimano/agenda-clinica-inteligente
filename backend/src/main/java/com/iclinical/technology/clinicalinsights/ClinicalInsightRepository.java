package com.iclinical.technology.clinicalinsights;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ClinicalInsightRepository extends JpaRepository<ClinicalInsight, UUID>, JpaSpecificationExecutor<ClinicalInsight> {

    List<ClinicalInsight> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    List<ClinicalInsight> findByClinicalDocumentIdOrderByCreatedAtDesc(UUID clinicalDocumentId);
}
