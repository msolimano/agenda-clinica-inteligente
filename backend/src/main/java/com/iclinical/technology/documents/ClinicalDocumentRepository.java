package com.iclinical.technology.documents;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClinicalDocumentRepository extends JpaRepository<ClinicalDocument, UUID> {

    @Query("""
        select d
        from ClinicalDocument d
        where d.status <> 'deleted'
          and (:patientId is null or d.patientId = :patientId)
          and (:documentType is null or d.documentType = :documentType)
        order by d.createdAt desc
        """)
    List<ClinicalDocument> findActiveFiltered(@Param("patientId") UUID patientId, @Param("documentType") String documentType);

    List<ClinicalDocument> findByPatientIdAndStatusNotOrderByCreatedAtDesc(UUID patientId, String status);
}
