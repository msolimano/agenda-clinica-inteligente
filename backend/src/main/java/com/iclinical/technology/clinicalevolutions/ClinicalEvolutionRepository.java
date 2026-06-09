package com.iclinical.technology.clinicalevolutions;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClinicalEvolutionRepository extends JpaRepository<ClinicalEvolution, UUID> {

    List<ClinicalEvolution> findByClinicalRecordIdAndStatusOrderByEvolutionDateDescCreatedAtDesc(UUID clinicalRecordId, String status);

    List<ClinicalEvolution> findByPatientIdAndStatusOrderByEvolutionDateDescCreatedAtDesc(UUID patientId, String status);
}
