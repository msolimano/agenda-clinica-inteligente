package com.iclinical.technology.clinicalrecords;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ClinicalRecordRepository extends JpaRepository<ClinicalRecord, UUID> {

    @Query("""
        select r
        from ClinicalRecord r
        where r.status <> 'deleted'
          and (:patientId is null or r.patientId = :patientId)
          and (:professionalId is null or r.professionalId = :professionalId)
          and (:status is null or r.status = :status)
          and (:from is null or r.recordDate >= :from)
          and (:to is null or r.recordDate <= :to)
        order by r.recordDate desc, r.createdAt desc
        """)
    List<ClinicalRecord> findFiltered(
        @Param("patientId") UUID patientId,
        @Param("professionalId") UUID professionalId,
        @Param("status") String status,
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    List<ClinicalRecord> findByPatientIdAndStatusNotOrderByRecordDateDescCreatedAtDesc(UUID patientId, String status);


    @Query("""
        select r
        from ClinicalRecord r
        where r.patientId = :patientId
          and r.status <> 'deleted'
          and (:from is null or r.recordDate >= :from)
          and (:to is null or r.recordDate <= :to)
        order by r.recordDate asc, r.createdAt asc
        """)
    List<ClinicalRecord> findFHIRBundleRecords(
        @Param("patientId") UUID patientId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    @Query("""
        select count(r) > 0
        from ClinicalRecord r
        where r.appointmentId = :appointmentId
          and r.status <> 'deleted'
          and (:excludedId is null or r.id <> :excludedId)
        """)
    boolean existsActiveForAppointment(@Param("appointmentId") UUID appointmentId, @Param("excludedId") UUID excludedId);
}
