package com.iclinical.technology.clinicalprescriptions;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClinicalPrescriptionRepository extends JpaRepository<ClinicalPrescription, UUID> {

    List<ClinicalPrescription> findByClinicalRecordIdAndStatusOrderByCreatedAtDesc(UUID clinicalRecordId, String status);

    List<ClinicalPrescription> findByPatientIdAndStatusOrderByCreatedAtDesc(UUID patientId, String status);
}
