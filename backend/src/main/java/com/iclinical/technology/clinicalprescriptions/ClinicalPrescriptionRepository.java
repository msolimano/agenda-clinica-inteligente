package com.iclinical.technology.clinicalprescriptions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ClinicalPrescriptionRepository extends JpaRepository<ClinicalPrescription, UUID> {

    List<ClinicalPrescription> findByClinicalRecordIdAndStatusOrderByCreatedAtDesc(UUID clinicalRecordId, String status);

    List<ClinicalPrescription> findByPatientIdAndStatusOrderByCreatedAtDesc(UUID patientId, String status);


    @Query("""
        select p
        from ClinicalPrescription p
        where p.status = 'active'
          and p.clinicalRecordId in :clinicalRecordIds
        order by p.createdAt asc
        """)
    List<ClinicalPrescription> findActiveByClinicalRecordIdIn(@Param("clinicalRecordIds") Collection<UUID> clinicalRecordIds);

    @Query("""
        select p
        from ClinicalPrescription p
        where p.patientId = :patientId
          and p.status = 'active'
          and p.createdAt >= :from
          and p.createdAt <= :to
        order by p.createdAt asc
        """)
    List<ClinicalPrescription> findActiveFHIRBundleByPatient(
        @Param("patientId") UUID patientId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );
}
