package com.iclinical.technology.clinicaldiagnoses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClinicalDiagnosisRepository extends JpaRepository<ClinicalDiagnosis, UUID> {

    List<ClinicalDiagnosis> findByClinicalRecordIdAndStatusOrderByPrimaryDiagnosisDescCreatedAtDesc(UUID clinicalRecordId, String status);

    List<ClinicalDiagnosis> findByPatientIdAndStatusOrderByCreatedAtDesc(UUID patientId, String status);

    @Modifying
    @Query("""
        update ClinicalDiagnosis d
        set d.primaryDiagnosis = false
        where d.clinicalRecordId = :clinicalRecordId
          and d.status = 'active'
          and d.primaryDiagnosis = true
          and (:excludedId is null or d.id <> :excludedId)
        """)
    void clearPrimaryForRecord(@Param("clinicalRecordId") UUID clinicalRecordId, @Param("excludedId") UUID excludedId);
}
