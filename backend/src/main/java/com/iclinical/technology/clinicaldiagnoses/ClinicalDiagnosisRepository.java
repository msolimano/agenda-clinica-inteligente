package com.iclinical.technology.clinicaldiagnoses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ClinicalDiagnosisRepository extends JpaRepository<ClinicalDiagnosis, UUID> {

    List<ClinicalDiagnosis> findByClinicalRecordIdAndStatusOrderByPrimaryDiagnosisDescCreatedAtDesc(UUID clinicalRecordId, String status);

    List<ClinicalDiagnosis> findByPatientIdAndStatusOrderByCreatedAtDesc(UUID patientId, String status);


    @Query("""
        select d
        from ClinicalDiagnosis d
        where d.status = 'active'
          and d.clinicalRecordId in :clinicalRecordIds
        order by d.createdAt asc
        """)
    List<ClinicalDiagnosis> findActiveByClinicalRecordIdIn(@Param("clinicalRecordIds") Collection<UUID> clinicalRecordIds);

    @Query("""
        select d
        from ClinicalDiagnosis d
        where d.patientId = :patientId
          and d.status = 'active'
          and (:from is null or d.createdAt >= :from)
          and (:to is null or d.createdAt <= :to)
        order by d.createdAt asc
        """)
    List<ClinicalDiagnosis> findActiveFHIRBundleByPatient(
        @Param("patientId") UUID patientId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );

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
